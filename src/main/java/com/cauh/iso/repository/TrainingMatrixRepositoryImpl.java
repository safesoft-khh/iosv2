package com.cauh.iso.repository;

import com.cauh.common.entity.*;
import com.cauh.common.entity.constant.JobDescriptionStatus;
import com.cauh.common.entity.constant.UserType;
import com.cauh.common.repository.UserRepository;
import com.cauh.iso.domain.*;
import com.cauh.iso.domain.constant.*;
import com.cauh.iso.utils.DateUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.xpath.operations.Bool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static com.cauh.iso.domain.QISOTrainingMatrix.iSOTrainingMatrix;
import static com.cauh.iso.domain.QTrainingMatrix.trainingMatrix;
import static com.querydsl.sql.SQLExpressions.min;

@Slf4j
@RequiredArgsConstructor
public class TrainingMatrixRepositoryImpl implements TrainingMatrixRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    private final UserRepository userRepository;

    @Value("${sop.prefix}")
    private String sopPrefix;

    public Page<MyTrainingMatrix> getMyTrainingMatrix(Pageable pageable, List<UserJobDescription> userJobDescriptions) {

        BooleanBuilder builder = new BooleanBuilder();
        if(ObjectUtils.isEmpty(userJobDescriptions)) {
            builder.and(trainingMatrix.trainingAll.eq(true));
        } else {
            List<Integer> roleAccountIds = userJobDescriptions.stream().map(d -> d.getJobDescription().getId()).collect(Collectors.toList());
            builder.and(trainingMatrix.trainingAll.eq(true).or(trainingMatrix.jobDescription.id.in(roleAccountIds)));
        }

        QueryResults<MyTrainingMatrix> results = queryFactory.selectDistinct(Projections.constructor(MyTrainingMatrix.class, trainingMatrix.documentVersion.document, trainingMatrix.documentVersion))
                .from(trainingMatrix)
                .where(trainingMatrix.documentVersion.document.type.eq(DocumentType.SOP))
                .where(trainingMatrix.documentVersion.status.eq(DocumentStatus.EFFECTIVE))
                .where(builder)
                .orderBy(trainingMatrix.documentVersion.document.docId.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        return new PageImpl<>(results.getResults(), pageable, results.getTotal());
    }

    public List<MyTrainingMatrix> getMyTrainingMatrix(List<UserJobDescription> userJobDescriptions) {
        if(!ObjectUtils.isEmpty(userJobDescriptions)) {
            List<Integer> jobDescriptionIds = userJobDescriptions.stream().map(d -> d.getJobDescription().getId()).collect(Collectors.toList());
            List<MyTrainingMatrix> results = queryFactory.selectDistinct(Projections.constructor(MyTrainingMatrix.class, trainingMatrix.documentVersion.document, trainingMatrix.documentVersion))
                    .from(trainingMatrix)
                    .where(trainingMatrix.documentVersion.document.type.eq(DocumentType.SOP))
                    .where(trainingMatrix.documentVersion.status.eq(DocumentStatus.EFFECTIVE))
                    .where(trainingMatrix.trainingAll.eq(true).or(trainingMatrix.jobDescription.id.in(jobDescriptionIds)))
                    .orderBy(trainingMatrix.documentVersion.document.docId.asc())
                    .fetch();

            return results;
        } else {
            return Collections.emptyList();
        }
    }

    public Page<MyTraining> getMyTraining(TrainingRequirement requirement, Pageable pageable, Account user) {
        if(requirement == TrainingRequirement.optional) {
            List<Integer> jobDescriptionIds = new ArrayList<>();
            if(ObjectUtils.isEmpty(user.getJobDescriptions()) == false) {
                jobDescriptionIds.addAll(user.getJobDescriptions().stream().map(d -> d.getJobDescription().getId()).collect(Collectors.toList()));
            }
            /**
             * Case 확인
             * 1. 입사 8주 이내..
             * 2. 입사 8주 이상..
             */
            QTrainingPeriod qTrainingPeriod = QTrainingPeriod.trainingPeriod;
            QTrainingLog qTrainingLog = QTrainingLog.trainingLog;
            QDocumentVersion qDocumentVersion = QDocumentVersion.documentVersion;

            BooleanBuilder builder = new BooleanBuilder();

            if (requirement == TrainingRequirement.mandatory) {
                builder.and(trainingMatrix.trainingAll.eq(true));
                if(ObjectUtils.isEmpty(jobDescriptionIds) == false) {
                    //수정 필요
//                    builder.or(trainingMatrix.jobDescription.id.in(jobDescriptionIds));
                }
            } else {//optional
                BooleanBuilder optionalBuilder = new BooleanBuilder();
                optionalBuilder.and(trainingMatrix.trainingAll.eq(true));
                if(ObjectUtils.isEmpty(jobDescriptionIds) == false) {
                    //수정 필요
//                    optionalBuilder.or(trainingMatrix.jobDescription.id.in(jobDescriptionIds));
                }
                builder.and(trainingMatrix.documentVersion.id.notIn(
                        JPAExpressions.selectDistinct(trainingMatrix.documentVersion.id)
                                .from(trainingMatrix)
                                .where(optionalBuilder)
                        )
                );
            }

            QueryResults<MyTraining> results = queryFactory
                    .selectDistinct(Projections.constructor(MyTraining.class,
                            trainingMatrix.documentVersion.document,
                            trainingMatrix.documentVersion,
                            qTrainingPeriod,
                            qTrainingLog,
                            qTrainingPeriod.startDate,
                            qTrainingPeriod.endDate)
                    )
                    .from(trainingMatrix)
                    .where(builder)
                    .join(qTrainingPeriod)
                    .on(trainingMatrix.documentVersion.id.eq(qTrainingPeriod.documentVersion.id)
                            .and(qTrainingPeriod.trainingType.eq(TrainingType.SELF))
                            .and(qTrainingPeriod.startDate.loe(new Date()))
                            .or(trainingMatrix.documentVersion.id.eq(qTrainingPeriod.documentVersion.id)
                                    .and(qTrainingPeriod.trainingType.eq(TrainingType.REFRESH))
                                    .and(qTrainingPeriod.startDate.goe(user.getIndate())))
                    )
                    .leftJoin(qTrainingLog)
                    .on(qTrainingPeriod.id.eq(qTrainingLog.trainingPeriod.id)
                            .and(qTrainingLog.user.id.eq(user.getId()))
                            .and(qTrainingLog.reportStatus.ne(DeviationReportStatus.REJECTED)))
                    .where(trainingMatrix.documentVersion.document.type.eq(DocumentType.SOP))
                    .where(trainingMatrix.documentVersion.status.in(DocumentStatus.EFFECTIVE, DocumentStatus.APPROVED))
                    .where(trainingMatrix.documentVersion.id.notIn(
                            JPAExpressions.selectDistinct(qDocumentVersion.parentVersion.id)
                                    .from(qDocumentVersion)
                                    .where(qDocumentVersion.document.type.eq(DocumentType.SOP)
                                            .and(qDocumentVersion.status.eq(DocumentStatus.APPROVED))
                                            .and(qDocumentVersion.parentVersion.isNotNull())
                                            .and(qDocumentVersion.effectiveDate.loe(DateUtils.addDay(user.getIndate(), 56))))
                    ))
                    .orderBy(trainingMatrix.documentVersion.effectiveDate.asc()) // 2021-03-04 :: 요구사항 내 SOP Effective Date 임박 순으로 조회로 명시.
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .fetchResults();

            return new PageImpl<>(results.getResults(), pageable, results.getTotal());
        } else {
            log.info("@MyTraining(Mandatory) : {}", user.getUsername());
            BooleanBuilder docStatus = new BooleanBuilder();
            QDocumentVersion qDocumentVersion = QDocumentVersion.documentVersion;
            docStatus.and(qDocumentVersion.status.in(DocumentStatus.APPROVED, DocumentStatus.EFFECTIVE));

            BooleanBuilder completeStatus = new BooleanBuilder();
            QTrainingLog qTrainingLog = QTrainingLog.trainingLog;
            completeStatus.and(qTrainingLog.status.notIn(TrainingStatus.COMPLETED).or(qTrainingLog.status.isNull()));

            return getMyMandatoryTrainingList(user.getDepartment(), user.getId(), null, null, pageable, docStatus, completeStatus);
        }
    }

    /**
     * 필수 트레이닝 이며, Training을 완료한 경우는 제외하고 리스트를 가져온다.
     */
    private JPAQuery getMyMandatoryTrainingListJpaQuery(Department department, Integer userId, String docId, Account loginUser, BooleanBuilder docStatus, BooleanBuilder completeStatus) {
        QUserJobDescription qUserJobDescription = QUserJobDescription.userJobDescription;
        QDocumentVersion qDocumentVersion = QDocumentVersion.documentVersion;
        QTrainingPeriod qTrainingPeriod = QTrainingPeriod.trainingPeriod;
        QAccount qUser = QAccount.account;
        QTrainingLog qTrainingLog = QTrainingLog.trainingLog;
        QUserMatrix qUserMatrix = QUserMatrix.userMatrix;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qUser.enabled.eq(true));
        builder.and(qUser.userType.eq(UserType.USER));
        builder.and(qUser.indate.isNotNull());

        if (!StringUtils.isEmpty(department)) {
            builder.and(qUser.department.eq(department));
        }
        if(!ObjectUtils.isEmpty(userId)) {
            builder.and(qUser.id.eq(userId));
        }

        if(!StringUtils.isEmpty(docId)) {
            log.info("@DocId : {}", docId);
            if(!docId.toUpperCase().startsWith(sopPrefix)) {
                docId = sopPrefix + docId;
            }
            builder.and(qDocumentVersion.document.docId.like(docId.toUpperCase() + "%"));
        }

        JPAQuery<MyTraining> jpaQuery = queryFactory.select(Projections.constructor(MyTraining.class,
                qUser,
                qDocumentVersion.document,
                qDocumentVersion,
                qTrainingPeriod,
                qTrainingLog,
                qTrainingPeriod.startDate,
                qTrainingPeriod.endDate,
                ExpressionUtils.as(JPAExpressions.select(min(qUserJobDescription.assignDate))
                        .from(qUserJobDescription)
                        .where(qUserJobDescription.user.username.eq(qUser.username)
                        .and(qUserJobDescription.status.eq(JobDescriptionStatus.APPROVED))), "assignDate")
        ))
                .from(qUser)
                .join(qUserMatrix)
                    .on(qUser.id.eq(qUserMatrix.userId))
                .join(qTrainingPeriod)
                    .on(qUserMatrix.trainingPeriodId.eq(qTrainingPeriod.id))
                .join(qDocumentVersion)
                    .on(qTrainingPeriod.documentVersion.id.eq(qDocumentVersion.id)
                        .and(docStatus))
                .leftJoin(qTrainingLog)
                    .on(qTrainingLog.trainingPeriod.id.eq(qUserMatrix.trainingPeriodId)
                        .and(qTrainingLog.user.id.eq(qUserMatrix.userId)))
                    .on(qTrainingLog.id.in(
                        JPAExpressions.select(qTrainingLog.id).from(qTrainingLog)
                        .where(qTrainingLog.reportStatus
                                .notIn(DeviationReportStatus.REJECTED, DeviationReportStatus.DELETED))))
        .where(builder)
        .where(completeStatus)
        .orderBy(qDocumentVersion.effectiveDate.asc()); //2021-03-04 :: Effective가 임박한 순으로 조회 되어야 함.

        return jpaQuery;
    }

    private JPAQuery getTrainingListJpaQuery(Department department, Integer userId, String docId, Account loginUser, BooleanBuilder docStatus, List<JobDescriptionStatus> statusList) {
        QUserJobDescription qUserJobDescription = QUserJobDescription.userJobDescription;
        QDocumentVersion qDocumentVersion = QDocumentVersion.documentVersion;
        QTrainingPeriod qTrainingPeriod = QTrainingPeriod.trainingPeriod;
        QAccount qUser = QAccount.account;
        QTrainingLog qTrainingLog = QTrainingLog.trainingLog;

//        User user = userRepository.findById(userId).get();
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qUser.enabled.eq(true));
        builder.and(qUser.indate.isNotNull());
//        builder.and(qUser.empNo.isNotNull());
        if(ObjectUtils.isEmpty(loginUser) == false) builder.and(qUser.id.ne(loginUser.getId()));
        if (!StringUtils.isEmpty(department)) {
            builder.and(qUser.department.eq(department));
        }
//        if (!StringUtils.isEmpty(teamCode)) {
//            builder.and(qUser.teamCode.eq(teamCode));
//        }
        if(!ObjectUtils.isEmpty(userId)) {
            builder.and(qUser.id.eq(userId));
        }

        if(!StringUtils.isEmpty(docId)) {
            log.info("@DocId : {}", docId);
            if(!docId.toUpperCase().startsWith(sopPrefix)) {
                docId = sopPrefix + docId;
            }
            builder.and(trainingMatrix.documentVersion.document.docId.like(docId.toUpperCase() + "%"));
        }

        //TODO:: 20210115 - 수정
        JPAQuery<MyTraining> jpaQuery = queryFactory.selectDistinct(Projections.constructor(MyTraining.class,
                    qUser,
                trainingMatrix.documentVersion.document,
                trainingMatrix.documentVersion,
                    qTrainingPeriod,
                    qTrainingLog,
                    qTrainingPeriod.startDate,
                    qTrainingPeriod.endDate,
                    ExpressionUtils.as(JPAExpressions.select(min(qUserJobDescription.assignDate))
                    .from(qUserJobDescription).where(qUserJobDescription.user.username.eq(qUser.username).and(qUserJobDescription.status.eq(JobDescriptionStatus.APPROVED))), "assignDate")
                ))
                .from(qUser)
                .leftJoin(qUserJobDescription).on(qUser.username.eq(qUserJobDescription.user.username).and(qUserJobDescription.status.in(statusList)))
//                .leftJoin(trainingMatrix).on(trainingMatrix.trainingAll.eq(true).or(qUserJobDescription.jobDescriptionVersion.jobDescription.id.eq(trainingMatrix.jobDescription.id)))
                .join(qDocumentVersion).on(trainingMatrix.documentVersion.id.eq(qDocumentVersion.id).and(docStatus))
                .join(qTrainingPeriod).on(qTrainingPeriod.documentVersion.id.eq(qDocumentVersion.id))
//                        .and(qTrainingPeriod.trainingType.eq(TrainingType.SELF))
//                        .and(qTrainingPeriod.startDate.loe(new Date()))
//                        .or(trainingMatrix.documentVersion.id.eq(qTrainingPeriod.documentVersion.id)
//                                .and(qTrainingPeriod.trainingType.eq(TrainingType.REFRESH))
//                                .and(qTrainingPeriod.startDate.goe(qUser.inDate))))

                .on(
                        qTrainingPeriod.trainingType.eq(TrainingType.SELF).and(qTrainingPeriod.startDate.loe(new Date()))
                                .or(qTrainingPeriod.trainingType.eq(TrainingType.REFRESH).and(qTrainingPeriod.startDate.goe(qUser.indate)))
                                .or(qTrainingPeriod.trainingType.eq(TrainingType.RE_TRAINING).and(qTrainingPeriod.retrainingUser.eq(qUser)))
                )
                .leftJoin(qTrainingLog).on(qTrainingLog.trainingPeriod.id.eq(qTrainingPeriod.id).and(qTrainingLog.user.id.eq(qUser.id)))
                .where(qUser.training.eq(true))
                .where(builder)
                .orderBy(trainingMatrix.documentVersion.effectiveDate.desc());

        return jpaQuery;
    }

    public Page<MyTraining> getMyMandatoryTrainingList(Department department, Integer userId, String docId, Account user, Pageable pageable, BooleanBuilder docStatus, BooleanBuilder completeStatus) {

        JPAQuery<MyTraining> jpaQuery = getMyMandatoryTrainingListJpaQuery(department, userId, docId, user, docStatus, completeStatus);
        QueryResults<MyTraining> results = null;

        try{
            results = jpaQuery.offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .fetchResults();
        }catch(Exception e) {
            log.error("Query 동작 중 에러 발생 : {}", e.getMessage());
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }

        return new PageImpl<>(results.getResults(), pageable, results.getTotal());
    }

    /**
     * 필수 트레이닝 목록
     * @param department
     * @param userId
     * @param docId
     * @param user
     * @param pageable
     * @param docStatus
     * @return
     */
    public Page<MyTraining> getTrainingList(Department department, Integer userId, String docId, Account user, Pageable pageable, BooleanBuilder docStatus, BooleanBuilder completeStatus) {

//        JPAQuery<MyTraining> jpaQuery = getTrainingListJpaQuery(deptCode, teamCode, userId, docId, user, docStatus, statusList);
//        QueryResults<MyTraining> results = jpaQuery.offset(pageable.getOffset())
//        .limit(pageable.getPageSize())
//                .fetchResults();

        JPAQuery<MyTraining> jpaQuery = getMyMandatoryTrainingListJpaQuery(department, userId, docId, user, docStatus, completeStatus);

        try{
            QueryResults<MyTraining> results = jpaQuery.offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .fetchResults();

            return new PageImpl<>(results.getResults(), pageable, results.getTotal());
        }catch(Exception e) {
            log.error("@Training Log List 동작 중 에러 발생 : {}", e.getMessage());
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    public List<MyTraining> getDownloadTrainingList(Department department, Integer userId, String docId, Account user, BooleanBuilder completeStatus) {
        BooleanBuilder docStatus = new BooleanBuilder();
        QDocumentVersion qDocumentVersion = QDocumentVersion.documentVersion;
        docStatus.and(qDocumentVersion.status.in(DocumentStatus.APPROVED, DocumentStatus.EFFECTIVE));

        JPAQuery<MyTraining> jpaQuery = getMyMandatoryTrainingListJpaQuery(department, userId, docId, user, docStatus, completeStatus);
//        BooleanBuilder docStatus = new BooleanBuilder();
//        QDocumentVersion qDocumentVersion = QDocumentVersion.documentVersion;
//        docStatus.and(qDocumentVersion.status.notIn(DocumentStatus.REVISION, DocumentStatus.DEVELOPMENT));
//
//        JPAQuery<MyTraining> jpaQuery = getTrainingListJpaQuery(deptCode, teamCode, userId, docId, user, docStatus, statusList);

        try{
            return jpaQuery.fetch();
        }catch(Exception e) {
            log.error("@Training Log List 동작 중 에러 발생 : {}", e.getMessage());
            return new ArrayList<>();
        }
    }


    @Override // My Training List를 불러온다.
    public Page<MyTraining> getISOMyTraining(Pageable pageable, Account user) {

        QISOTrainingPeriod qIsoTrainingPeriod = QISOTrainingPeriod.iSOTrainingPeriod;
        QISO qISO = QISO.iSO;
        QISOTrainingLog qIsoTrainingLog = QISOTrainingLog.iSOTrainingLog;
        QISOAttachFile qIsoAttachFile = QISOAttachFile.iSOAttachFile;

        BooleanBuilder builder = new BooleanBuilder();

        builder.and(iSOTrainingMatrix.trainingAll.eq(true));
        builder.or(iSOTrainingMatrix.trainingAll.eq(false).and(iSOTrainingMatrix.user.id.eq(user.getId())));

        QueryResults<MyTraining> results = queryFactory
                .selectDistinct(Projections.constructor(MyTraining.class,
                        iSOTrainingMatrix.iso,
                        qIsoAttachFile,
                        qIsoTrainingPeriod,
                        qIsoTrainingLog,
                        qIsoTrainingPeriod.startDate,
                        qIsoTrainingPeriod.endDate)
                )
                .from(iSOTrainingMatrix)
                .where(builder)
                .join(qISO)
                    .on(iSOTrainingMatrix.iso.id.eq(qISO.id))
                .join(qIsoAttachFile)
                    .on(qISO.id.eq(qIsoAttachFile.iso.id))
                .join(qIsoTrainingPeriod)
                    .on(qISO.id.eq(qIsoTrainingPeriod.iso.id)
                        .and(qISO.training.eq(true)
                                .and(qISO.active.eq(true))
                                .and(qIsoTrainingPeriod.trainingType.eq(TrainingType.SELF)
                                        .and(qIsoTrainingPeriod.startDate.loe(new Date()))
                                )
                        )
                    )
                .leftJoin(qIsoTrainingLog).on(qISO.id.eq(qIsoTrainingLog.iso.id)
                        .and(qIsoTrainingLog.user.id.eq(user.getId())))
                .where(qIsoTrainingLog.status.ne(TrainingStatus.COMPLETED)
                        .or(qIsoTrainingLog.isNull()))
                .orderBy(qIsoTrainingPeriod.endDate.asc()) //완료일자가 임박한 순으로 정렬.
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        return new PageImpl<>(results.getResults(), pageable, results.getTotal());
    }

    /**
     * ISO 트레이닝 이며, Training을 완료한 경우는 제외하고 리스트를 가져온다.
     */
    private JPAQuery getMyISOTrainingListJpaQuery(Department department, Integer userId, ISOType isoType, String title, BooleanBuilder completeStatus) {
        QISO qISO = QISO.iSO;
        QISOTrainingPeriod qISOTrainingPeriod = QISOTrainingPeriod.iSOTrainingPeriod;
        QISOTrainingLog qISOTrainingLog = QISOTrainingLog.iSOTrainingLog;
        QAccount qUser = QAccount.account;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qUser.enabled.eq(true));
        builder.and(qUser.indate.isNotNull());
        builder.and(qUser.userType.eq(UserType.USER));

        if (!ObjectUtils.isEmpty(department)) {
            builder.and(qUser.department.eq(department));
            
            if (!ObjectUtils.isEmpty(department.getChildDepartments())){
                builder.or(qUser.department.in(department.getChildDepartments()));
            }
        }

        if(!ObjectUtils.isEmpty(userId)) {
            builder.and(qUser.id.eq(userId));
        }

        if(!ObjectUtils.isEmpty(title)) {
            builder.and(qISO.title.startsWithIgnoreCase(title));
        }


        if(!ObjectUtils.isEmpty(isoType)) {
            log.info("@isoType : {}", isoType);
            builder.and(qISO.isoType.eq(isoType));
            builder.and(qISO.training.eq(true).and(qISO.active.eq(true)));
        }

        JPAQuery<MyTraining> jpaQuery = queryFactory.select(Projections.constructor(MyTraining.class,
                qUser,
                qISO,
                qISOTrainingPeriod,
                qISOTrainingLog,
                qISOTrainingPeriod.startDate,
                qISOTrainingPeriod.endDate))
                .from(qISO)
                .join(qISOTrainingPeriod)
                .on(qISO.id.eq(qISOTrainingPeriod.iso.id)
                        .and(qISO.training.eq(true)
                                .and(qISO.active.eq(true))
                                .and(qISOTrainingPeriod.trainingType.eq(TrainingType.SELF))
                        )
                )
                .join(iSOTrainingMatrix)
                    .on(qISO.id.eq(iSOTrainingMatrix.iso.id))
                .join(qUser)
                    .on(iSOTrainingMatrix.user.id.eq(qUser.id)
                            .or((iSOTrainingMatrix.user.id.isNull().and(iSOTrainingMatrix.trainingAll.eq(true)))))
                .leftJoin(qISOTrainingLog)
                    .on(qISOTrainingLog.iso.id.eq(qISO.id)
                            .and(qISOTrainingLog.user.id.eq(qUser.id)))
                .where(builder)
                .where(completeStatus)
                .orderBy(qISO.id.asc());

        return jpaQuery;
    }

    @Override
    public Page<MyTraining> getISOTrainingList(Department department, Integer userId, ISOType isoType, String title, Pageable pageable, BooleanBuilder completeStatus) {

        JPAQuery<MyTraining> jpaQuery = getMyISOTrainingListJpaQuery(department, userId, isoType, title, completeStatus);
        QueryResults<MyTraining> results;

        try{
            results = jpaQuery.offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .fetchResults();
        }catch(Exception e) {
            log.error("@Training Log List 동작 중 에러 발생 : {}", e.getMessage());
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }

        return new PageImpl<>(results.getResults(), pageable, results.getTotal());
    }

    @Override
    public List<MyTraining> getDownloadISOTrainingList(Department department, Integer userId, ISOType isoType, String title, BooleanBuilder completeStatus) {
        JPAQuery<MyTraining> jpaQuery = getMyISOTrainingListJpaQuery(department, userId, isoType, title, completeStatus);

        try{
            return jpaQuery.fetch();
        }catch(Exception e) {
            log.error("@ISO Training Log Download List 동작 중 에러 발생 : {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
