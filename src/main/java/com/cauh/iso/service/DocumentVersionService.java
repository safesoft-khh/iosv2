package com.cauh.iso.service;

import com.cauh.common.entity.Account;
import com.cauh.common.entity.QAccount;
import com.cauh.common.repository.UserRepository;
import com.cauh.common.service.UserService;
import com.cauh.iso.admin.domain.constant.SOPAction;
import com.cauh.iso.component.DocumentAssembly;
import com.cauh.iso.component.DocumentViewer;
import com.cauh.iso.domain.*;
import com.cauh.iso.domain.constant.DocumentStatus;
import com.cauh.iso.domain.constant.DocumentType;
import com.cauh.iso.domain.constant.PostStatus;
import com.cauh.iso.domain.constant.TrainingStatus;
import com.cauh.iso.domain.report.RetirementDocument;
import com.cauh.iso.repository.DocumentRepository;
import com.cauh.iso.repository.DocumentVersionRepository;
import com.cauh.iso.repository.TrainingMatrixRepository;
import com.cauh.iso.security.annotation.IsAllowedRF;
import com.cauh.iso.security.annotation.IsAllowedSOP;
import com.cauh.iso.utils.DateUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.groupdocs.assembly.DataSourceInfo;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
@Service
@Slf4j
public class DocumentVersionService {
    private final FileStorageService fileStorageService;
    private final DocumentService documentService;
    private final DocumentViewer documentViewer;
    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final CategoryService categoryService;
    private final JDService jdService;
    private final MailService mailService;
    private final NoticeService noticeService;
    private final TrainingPeriodService trainingPeriodService;
    private final TrainingMatrixRepository trainingMatrixRepository;
    private final UserRepository userRepository;
    private final RetirementDocumentService retirementDocumentService;
    private final UserService userService;

    @Value("${sop.prefix}")
    private String sopPrefix;

    private QDocumentVersion qDocumentVersion = QDocumentVersion.documentVersion;

    public DocumentVersion findById(String id) {
        return documentVersionRepository.findById(id).get();
    }
    public Optional<DocumentVersion> findOne(BooleanBuilder builder) {
        return documentVersionRepository.findOne(builder);
    }

    public DocumentVersion save(DocumentVersion documentVersion) {
        if (documentVersion.getDocument().getType() == DocumentType.SOP) {
            documentVersion.getDocument().setCategory(categoryService.findByShortName(documentVersion.getDocument().getCategory().getShortName()).get());
        } else if (documentVersion.getDocument().getType() == DocumentType.RF) {
            documentVersion.getDocument().setSop(documentService.findByDocId(documentVersion.getDocument().getSop().getDocId()).get());
        }

        /**
         * 신규 Version 등록
         */
        if(StringUtils.isEmpty(documentVersion.getId())) {
            Document savedDocument = documentRepository.save(documentVersion.getDocument());
            documentVersion.setDocument(savedDocument);
        } else {
            Document updatedDocument = documentRepository.save(documentVersion.getDocument());
            log.info("@Document 정보 수정 : {}", updatedDocument.getId());
        }

        return saveDocumentVersion(documentVersion);
    }

    @Transactional
    public DocumentVersion saveQuiz(String docVerId, Quiz quiz) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        DocumentVersion documentVersion = findById(docVerId);
        documentVersion.setQuiz(objectMapper.writeValueAsString(quiz));
        log.info("=> docVerId : {}, Quiz 수정!", docVerId);
        return documentVersionRepository.save(documentVersion);
    }

    public DocumentVersion revision(DocumentVersion documentVersion) throws Exception {
        DocumentVersion supersededDocument = documentVersionRepository.findById(documentVersion.getId()).get();
        if(supersededDocument.getStatus() == DocumentStatus.EFFECTIVE) {
            supersededDocument.setStatus(DocumentStatus.SUPERSEDED);
            DocumentVersion savedSupersededDocument = documentVersionRepository.save(supersededDocument);
            log.debug(" => 이전 버전의 상태를 Superseded 상태로 변경한다. - {}", documentVersion.getId());

            documentVersion.setParentVersion(savedSupersededDocument);//parentVersion 설정
        } else {
            documentVersion.setParentVersion(supersededDocument);
        }

        documentVersion.setId(null);
        return saveDocumentVersion(documentVersion);
    }

    public void documentNotification(SOPAction sopAction, DocumentVersion documentVersion) {

        //계정 Mail 전송 구간.
        HashMap<String, Object> model = new HashMap<>();
        String title = "", templateTitle = "";

        //CASE 1. 신규 Document 생성
        if(ObjectUtils.isEmpty(sopAction)){
            title = "신규 SOP/RF가 등록 되었습니다.";
            model.put("title", String.format("[%s] %s가 등록 되었습니다.", documentVersion.getStatus().getLabel(), documentVersion.getDocument().getType().name()));
        }else if(sopAction == SOPAction.edit) {
            title = "기존 SOP/RF의 정보가 수정 되었습니다.";
            model.put("title", String.format("[%s] %s가 수정되었습니다.", documentVersion.getStatus().getLabel(), documentVersion.getDocument().getType().name()));

        }else if(sopAction == SOPAction.revision) {
            title = "기존 SOP/RF가 개정되었습니다.";
            model.put("title", String.format("[%s] %s가 개정되었습니다.", documentVersion.getStatus().getLabel(), documentVersion.getDocument().getType().name()));
        }

        model.put("documentVersion", documentVersion);


        List<String> toList = mailService.getReceiveEmails();

        Mail mail = Mail.builder()
                .to(toList.toArray(new String[toList.size()]))
                .subject(String.format("[ISO-MS/System] %s", title))
                .model(model)
                .templateName("document-version-notification")
                .build();

        mailService.sendMail(mail);
        log.debug("Document 정보가 발송되었습니다. {}", mail);
    }


    @Transactional
    public void remove(String id) {
        log.warn("==> SOP/RF 삭제 요청 ID : {}", id);
        Optional<DocumentVersion> optionalDocumentVersion = documentVersionRepository.findById(id);
        if(optionalDocumentVersion.isPresent()) {
            DocumentVersion documentVersion = optionalDocumentVersion.get();

            if(documentVersion.getStatus() == DocumentStatus.DEVELOPMENT) {
                Document document = documentVersion.getDocument();
                String documentId = document.getId();
                if(document.getType() == DocumentType.SOP) {
                    log.info("@Development 상태이고 삭제하는 문서가 SOP 인 경우 관련 RF 까지 삭제 : {}", documentId);
                    QDocument qDocument = QDocument.document;
                    BooleanBuilder builder = new BooleanBuilder();
                    builder.and(qDocument.sop.id.eq(documentId));
                    Iterable<Document> documents = documentRepository.findAll(builder);
                    StreamSupport.stream(documents.spliterator(), false)
                            .forEach(doc -> {
                                QDocumentVersion qDocumentVersion = QDocumentVersion.documentVersion;
                                BooleanBuilder vbuilder = new BooleanBuilder();
                                vbuilder.and(qDocumentVersion.document.id.eq(doc.getId()));

                                Iterable<DocumentVersion> versions = documentVersionRepository.findAll(vbuilder);
                                StreamSupport.stream(versions.spliterator(), false)
                                        .forEach(v -> {
                                            log.info("==> RF DocumentVersion Delete By Id : {}", v.getId());
                                            documentVersionRepository.deleteById(v.getId());
                                            log.info("<== RF DocumentVersion  Delete By Id : {}", v.getId());
                                        });

                                log.info("==> RF Document Delete By Id : {}", doc.getId());
                                documentRepository.deleteById(doc.getId());
                                log.info("<== RF Document Delete By Id : {}", doc.getId());
                            });
                }
                log.info("@Development 상태인 경우 DocumentVersion 삭제 : {}", id);
                documentVersionRepository.deleteById(id);
                log.info("@Development 상태인 경우 Document 삭제 : {}", documentId);
                documentRepository.deleteById(documentId);
            } else {
                log.info("@Revision 상태인 경우는 현재 문서만 삭제 : {}", id);
                documentVersionRepository.deleteById(id);
            }
        }
        log.info("<== SOP/RF 삭제 완료 ID : {}", id);
    }

    public DocumentVersion retirement(String docVerId, Date retirementDate) {
        log.debug("Retirement Id : {}", docVerId);

        //Retirement Form 미사용으로 RetirementDocumentVersion은 더 이상 사용되지 않음.
        //retirementDate가 없으면 바로 적용, 있으면 날짜만 지정
        //바로 적용
        DocumentVersion retirementDocumentVersion = findById(docVerId);

        if(ObjectUtils.isEmpty(retirementDate)) {
            retirementDocumentVersion.setStatus(DocumentStatus.SUPERSEDED);
            retirementDocumentVersion.setRetirement(true);
            retirementDocumentVersion.setRetirementDate(new Date());
        } else { //Retirement 날짜를 지정
            retirementDocumentVersion.setRetirementDate(retirementDate);
        }

        return documentVersionRepository.save(retirementDocumentVersion);
    }

//    public DocumentVersion retirement(DocumentVersion documentVersion) {
//        log.debug("Retirement Id : {}", documentVersion.getId());
//        documentVersion.setRetirement(true);
//
//        return documentVersionRepository.save(documentVersion);
//    }

    public DocumentVersion approved(String docVerId) {
        log.debug("Retirement Id : {}", docVerId);
        DocumentVersion documentVersion = findById(docVerId);
        documentVersion.setStatus(DocumentStatus.APPROVED);
        documentVersion.setApprovedDate(DateUtils.truncate(new Date()));

        return documentVersionRepository.save(documentVersion);
    }

    protected DocumentVersion saveDocumentVersion(DocumentVersion documentVersion) {
        if(StringUtils.isEmpty(documentVersion.getId())) {
            documentVersion.setId(UUID.randomUUID().toString());
            log.debug("Doc Ver Id. 생성 : {}", documentVersion.getId());
        }

        //SOP
        if(!ObjectUtils.isEmpty(documentVersion.getUploadSopDocFile()) && !documentVersion.getUploadSopDocFile().isEmpty()) {
            String fileName = fileStorageService.storeFile(documentVersion.getUploadSopDocFile(), documentVersion.getId());
            String ext = fileName.substring(fileName.lastIndexOf(".") + 1);

            Integer totalPage = fileStorageService.conversionPdf2Img(documentVersion.getUploadSopDocFile(), documentVersion.getId());
            documentVersion.setTotalPage(totalPage);

            documentVersion.setFileName(fileName);
            documentVersion.setOriginalFileName(documentVersion.getUploadSopDocFile().getOriginalFilename());
            documentVersion.setFileType(documentVersion.getUploadSopDocFile().getContentType());
            documentVersion.setFileSize(documentVersion.getUploadSopDocFile().getSize());
            documentVersion.setExt(ext);
        }
        //RF(KOR)
        if(!ObjectUtils.isEmpty(documentVersion.getUploadRfKorFile()) && !documentVersion.getUploadRfKorFile().isEmpty()) {
            String fileName = fileStorageService.storeFile(documentVersion.getUploadRfKorFile(), documentVersion.getId()+"_KOR");
            String ext = fileName.substring(fileName.lastIndexOf(".") + 1);

            documentVersion.setRfKorFileName(fileName);
            documentVersion.setRfKorOriginalFileName(documentVersion.getUploadRfKorFile().getOriginalFilename());
            documentVersion.setRfKorFileType(documentVersion.getUploadRfKorFile().getContentType());
            documentVersion.setRfKorFileSize(documentVersion.getUploadRfKorFile().getSize());
            documentVersion.setRfKorExt(ext);
        }
        //RF(ENG)
        if(!ObjectUtils.isEmpty(documentVersion.getUploadRfEngFile()) && !documentVersion.getUploadRfEngFile().isEmpty()) {
            String fileName = fileStorageService.storeFile(documentVersion.getUploadRfEngFile(), documentVersion.getId()+"_ENG");
            String ext = fileName.substring(fileName.lastIndexOf(".") + 1);

            documentVersion.setRfEngFileName(fileName);
            documentVersion.setRfEngOriginalFileName(documentVersion.getUploadRfEngFile().getOriginalFilename());
            documentVersion.setRfEngFileType(documentVersion.getUploadRfEngFile().getContentType());
            documentVersion.setRfEngFileSize(documentVersion.getUploadRfEngFile().getSize());
            documentVersion.setRfEngExt(ext);
        }



        DocumentVersion savedDocumentVersion = documentVersionRepository.save(documentVersion);

        if(documentVersion.getDocument().getType() == DocumentType.SOP) {
            trainingPeriodService.saveOrUpdateSelfTrainingPeriod(savedDocumentVersion);

            jdService.saveAll(savedDocumentVersion, documentVersion.isTrainingAll(), documentVersion.getJdIds());
        }



        return savedDocumentVersion;
    }

    @IsAllowedSOP
    public Iterable<DocumentVersion> findAll(BooleanBuilder builder) {
        QDocumentVersion qDocumentVersion = QDocumentVersion.documentVersion;
        return documentVersionRepository.findAll(builder, qDocumentVersion.document.docId.asc());
    }

    @IsAllowedRF
    public Iterable<DocumentVersion> findRFBySopId(BooleanBuilder builder) {
        QDocumentVersion qDocumentVersion = QDocumentVersion.documentVersion;
        return documentVersionRepository.findAll(builder, qDocumentVersion.document.docId.asc());
    }

    public Iterable<DocumentVersion> findAllCurrentRFList(List<String> rfIds) {
        BooleanBuilder builder = new BooleanBuilder();
        QDocumentVersion qDocumentVersion = QDocumentVersion.documentVersion;
        builder.and(qDocumentVersion.document.type.eq(DocumentType.RF));
        builder.and(qDocumentVersion.document.id.in(rfIds));
        builder.and(qDocumentVersion.status.eq(DocumentStatus.EFFECTIVE));
        return documentVersionRepository.findAll(builder, qDocumentVersion.document.docId.asc());
    }

    public Page<DocumentVersion> findAll(BooleanBuilder builder, Pageable pageable) {
        return documentVersionRepository.findAll(builder, pageable);
    }

    public BooleanBuilder getMainSOPPredicate(DocumentStatus status, String categoryId, String sopId, String verId) {
        BooleanBuilder builder = new BooleanBuilder();
//        if(status == DocumentStatus.SUPERSEDED) {
//            builder.and(qDocumentVersion.status.in(status, DocumentStatus.EFFECTIVE));
//            if (StringUtils.isEmpty(sopId)) {
//                builder.and(qDocumentVersion.parentVersion.id.isNull());
//            }
//        } else if(status == DocumentStatus.APPROVED) {
//            builder.and(qDocumentVersion.status.in(status, DocumentStatus.EFFECTIVE));
//        } else {
            builder.and(qDocumentVersion.status.eq(status));
//        }
        builder.and(qDocumentVersion.document.type.eq(DocumentType.SOP));
        if(StringUtils.isEmpty(categoryId) == false) {
            builder.and(qDocumentVersion.document.category.eq(Category.builder().id(categoryId).build()));
        }

        if(StringUtils.isEmpty(sopId) == false) {
            builder.and(qDocumentVersion.document.id.eq(sopId));
        }

        if(StringUtils.isEmpty(verId) == false) {
            builder.and(qDocumentVersion.id.eq(verId));
        }

        return builder;
    }

    public BooleanBuilder getMainSOPPredicate(DocumentType type, DocumentStatus status, String categoryId, String sopId, String verId) {
        BooleanBuilder builder = new BooleanBuilder();
        if(status == DocumentStatus.SUPERSEDED) {
            builder.and(qDocumentVersion.status.in(status, DocumentStatus.EFFECTIVE));
            if (StringUtils.isEmpty(sopId)) {
                builder.and(qDocumentVersion.parentVersion.id.isNull());
            }
        } else {
            builder.and(qDocumentVersion.status.eq(status));
        }
        builder.and(qDocumentVersion.document.type.eq(type));
        if(StringUtils.isEmpty(categoryId) == false) {
            builder.and(qDocumentVersion.document.category.eq(Category.builder().id(categoryId).build()));
        }

        if(StringUtils.isEmpty(sopId) == false) {
            builder.and(qDocumentVersion.document.id.eq(sopId));
        }

        if(StringUtils.isEmpty(verId) == false) {
            builder.and(qDocumentVersion.id.eq(verId));
        }

        return builder;
    }

    public BooleanBuilder getMainRFPredicate(DocumentStatus status, List<String> sopIdList) {
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(qDocumentVersion.status.eq(status));
//        }
        builder.and(qDocumentVersion.document.type.eq(DocumentType.RF));
        if(!ObjectUtils.isEmpty(sopIdList)) {
            builder.and(qDocumentVersion.document.sop.id.in(sopIdList));
        }

        return builder;
    }

    public BooleanBuilder getAdminSOPPredicate(DocumentType type, DocumentStatus status, String categoryId, String docId) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qDocumentVersion.status.eq(status));
        builder.and(qDocumentVersion.document.type.eq(type));
        if(StringUtils.isEmpty(categoryId) == false) {
            builder.and(qDocumentVersion.document.category.eq(Category.builder().id(categoryId).build()));
        }

        if(StringUtils.isEmpty(docId) == false) {
            if(!docId.toUpperCase().startsWith(sopPrefix)) {
                docId = sopPrefix + docId;
            }
            builder.and(qDocumentVersion.document.docId.startsWith(docId));
        }

        return builder;
    }

    public BooleanBuilder getPredicate(DocumentType type, DocumentStatus status, String categoryId, String id, String verId) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qDocumentVersion.status.eq(status));
        builder.and(qDocumentVersion.document.type.eq(type));
        if(StringUtils.isEmpty(categoryId) == false) {
            builder.and(qDocumentVersion.document.category.eq(Category.builder().id(categoryId).build()));
        }

        if(StringUtils.isEmpty(id) == false) {
            builder.and(qDocumentVersion.document.id.eq(id));
        }

        if(StringUtils.isEmpty(verId) == false) {
            builder.and(qDocumentVersion.id.eq(verId));
        }

        return builder;
    }

    @Async("threadPoolTaskExecutor")
    @Transactional
    public void approvedToEffective() {
        log.info("==> Approved 상태인 SOP/RF를 Effective 상태로 변경 처리한다.");
        Date now = DateUtils.truncate(new Date());

        List<DocumentVersion> effectiveSOPs = getApprovedToEffectiveDocuments(DocumentType.SOP, now);
        List<DocumentVersion> effectiveRFs = getApprovedToEffectiveDocuments(DocumentType.RF, now);
        boolean hasEffectiveSOPs = ObjectUtils.isEmpty(effectiveSOPs) == false;
        boolean hasEffectiveRFs = ObjectUtils.isEmpty(effectiveRFs) == false;

        log.debug("현재 기준 approved -> effective 되어야 하는 대상 SOP:{}/RF:{}", effectiveSOPs.size(), effectiveRFs.size());

//        StringBuilder sb = new StringBuilder("[SOP 공지]");
        String subject = "[ISO MS] SOPs(RFs) Notification";
        if(hasEffectiveSOPs) {
//            String categoryNames = effectiveSOPs.stream().map(d -> d.getDocument().getCategory().getShortName()).distinct().sorted().collect(Collectors.joining(","));
//            sb.append(categoryNames).append(" SOP");
            log.debug("-> SOP effective 처리 시작");
            updateDocumentVersionStatus(effectiveSOPs);
            log.debug("-> SOP effective 처리 완료");
            //[DtnSM_QA 공지] SOP-DM Training_by 20Sep2019 및 폐기 예정 SOP 공지의 건
            // [DtnSM_QA 공지] MW & CO SOP 및 RD Effective 공지
//            log.info("--> [DtnSM_QA 공지] {} SOP 및 RD Effective 공지", categoryNames);
        }

        if(hasEffectiveRFs) {
//            sb.append(" 및 RF");
            log.debug("-> RF effective 처리 시작");
            updateDocumentVersionStatus(effectiveRFs);
            log.debug("<- RF effective 처리 완료");
        }

//        Iterable<RetirementDocument> retirementSOPDocuments = retirementDocumentService.findRetirementDocs(now, DocumentType.SOP);
//        Iterable<RetirementDocument> retirementRFDocuments = retirementDocumentService.findRetirementDocs(now, DocumentType.RF);

        //2021-02-04 : Retirement 작업
        List<DocumentVersion> retirementSOPs = getEffectiveToSupersededDocuments(DocumentType.SOP, now);
        List<DocumentVersion> retirementRFs = getEffectiveToSupersededDocuments(DocumentType.RF, now);
        log.debug("현재 기준 effective -> superseded 되어야 하는 대상 SOP:{}/RF:{}", retirementSOPs.size(), retirementRFs.size());

        boolean hasRetirementSOPs = ObjectUtils.isEmpty(retirementSOPs) == false;
        boolean hasRetirementRFs = ObjectUtils.isEmpty(retirementRFs) == false;


        if(hasRetirementSOPs) {
            log.debug("-> SOP retirement 처리 시작");
            retirementDocumentStatus(retirementSOPs);
            log.debug("-> SOP retirement 처리 완료");
        }

        if(hasRetirementRFs) {
            log.debug("-> RF retirement 처리 시작");
            retirementDocumentStatus(retirementRFs);
            log.debug("-> RF retirement 처리 완료");
        }

        if(hasRetirementSOPs || hasRetirementRFs || hasRetirementSOPs || hasRetirementRFs) {
//            if(!ObjectUtils.isEmpty(effectiveSOPs) || !ObjectUtils.isEmpty(effectiveRDs)) {
//                sb.append(" Effective");
//            }
//
//            if(hasRetirementSOPs || hasRetirementRFs) {
//                if(!ObjectUtils.isEmpty(effectiveSOPs) || !ObjectUtils.isEmpty(effectiveRFs)) {
//                    sb.append(" 및");
//                }
//                sb.append(" 폐기 SOP");
//            }
//
//            sb.append(" 공지의 건");

            HashMap<String, Object> model = new HashMap<>();
            model.put("title", subject);
            model.put("effectiveSOPs", effectiveSOPs);
            model.put("effectiveRFs", effectiveRFs);
            model.put("retirementSOPs", retirementSOPs);
            model.put("retirementRFs", retirementRFs);
//            Mail mail = Mail.builder()
//                    .to(new String[]{"jhseo@dtnsm.com"})
//                    .subject(sb.toString())
//                    .templateName("alert-sop-effective")
//                    .model(model)
//                    .build();
//
//            mailService.sendMail(mail);

            Notice notice = Notice.builder()
                    .title(subject)
                    .content(mailService.processTemplate("sop-effective-body.ftlh", model, null))
                    .postStatus(PostStatus.NONE)
                    .build();


            Notice savedNotice = noticeService.save(notice, null);
            log.debug("SOP Effective 알림 공지 등록, Notice ID : {}", savedNotice.getId());
            noticeService.sendMail(savedNotice.getId());


            /**
             * SOP/RF 공지사항 등록
             */
            if(!ObjectUtils.isEmpty(effectiveSOPs)) {
                Iterable<DocumentVersion> iterable = findAll(getPredicate(DocumentType.SOP, DocumentStatus.EFFECTIVE, null, null, null));
                List<DocumentVersion> documentVersions = StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
                model.clear();
                model.put("currentSOPs", documentVersions);
                Notice sopNotice = Notice.builder()
                        .title("Current SOP List("+ DateUtils.format(new Date(), "yyyy.MM.dd") +"일자)")
                        .content(mailService.processTemplate("sop-index-notice.ftlh", model, null))
                        .postStatus(PostStatus.NONE)
                        .topViewEndDate(DateUtils.addDay(new Date(), 10))
                        .build();

                Notice savedSopNotice = noticeService.save(sopNotice, null);
                log.info("=> SOP Current Index 공지 등록 : {}", savedSopNotice);
            }
            if(!ObjectUtils.isEmpty(effectiveRFs)) {
                Iterable<DocumentVersion> iterable = findAll(getPredicate(DocumentType.RF, DocumentStatus.EFFECTIVE, null, null, null));
                List<DocumentVersion> documentVersions = StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
                model.clear();
                model.put("currentRFs", documentVersions);
                Notice sopNotice = Notice.builder()
                        .title("Current RF List("+ DateUtils.format(new Date(), "yyyy.MM.dd") +"일자)")
                        .content(mailService.processTemplate("rf-index-notice.ftlh", model, null))
                        .postStatus(PostStatus.NONE)
                        .topViewEndDate(DateUtils.addDay(new Date(), 10))
                        .build();

                Notice savedSopNotice = noticeService.save(sopNotice, null);
                log.info("=> RF Current Index 공지 등록 : {}", savedSopNotice);
            }
        }
    }

    protected List<DocumentVersion> getApprovedToEffectiveDocuments(DocumentType documentType, Date now) {

        QDocumentVersion qDocumentVersion = QDocumentVersion.documentVersion;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qDocumentVersion.document.type.eq(documentType));
        builder.and(qDocumentVersion.status.eq(DocumentStatus.APPROVED));
        builder.and(qDocumentVersion.effectiveDate.eq(now));
        Iterable<DocumentVersion> documentVersions = documentVersionRepository.findAll(builder);

        List<DocumentVersion> effectiveDocuments = StreamSupport.stream(documentVersions.spliterator(), false)
                .collect(Collectors.toList());

        return effectiveDocuments;
    }

    protected List<DocumentVersion> getEffectiveToSupersededDocuments(DocumentType documentType, Date now) {

        QDocumentVersion qDocumentVersion = QDocumentVersion.documentVersion;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qDocumentVersion.document.type.eq(documentType));
        builder.and(qDocumentVersion.status.eq(DocumentStatus.EFFECTIVE));
        builder.and(qDocumentVersion.retirement.eq(false));
        builder.and(qDocumentVersion.retirementDate.eq(now));
        Iterable<DocumentVersion> documentVersions = documentVersionRepository.findAll(builder);

        List<DocumentVersion> retirementDocuments = StreamSupport.stream(documentVersions.spliterator(), false)
                .collect(Collectors.toList());

        return retirementDocuments;
    }

    protected void updateDocumentVersionStatus(List<DocumentVersion> approvedToEffectiveDocuments) {
        for (DocumentVersion documentVersion : approvedToEffectiveDocuments) {
            if (!ObjectUtils.isEmpty(documentVersion.getParentVersion())) {
                log.debug("** 상위 버전 ->  : {} -> Superseded 상태로 변경!", documentVersion.getParentVersion().getId());
                documentVersion.getParentVersion().setStatus(DocumentStatus.SUPERSEDED);
                documentVersionRepository.save(documentVersion.getParentVersion());
            }

            log.debug("** Effective 상태로 변경 : {}", documentVersion.getId());
            documentVersion.setStatus(DocumentStatus.EFFECTIVE);
            documentVersionRepository.save(documentVersion);
        }
    }

    protected void retirementDocumentStatus(List<DocumentVersion> effectiveToSupersededDocuments) {
        for(DocumentVersion documentVersion : effectiveToSupersededDocuments) {
            documentVersion.setRetirement(true);
            documentVersion.setStatus(DocumentStatus.SUPERSEDED);
            documentVersionRepository.save(documentVersion);
        }
    }


    @Async("threadPoolTaskExecutor")
    @Transactional(readOnly = true)
    public void sopTrainingAlert() {
        QAccount qUser = QAccount.account;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qUser.enabled.eq(true));
        builder.and(qUser.training.eq(true));
        builder.and(qUser.receiveEmail.eq(true));
        Iterable<Account> iterable = userRepository.findAll(builder);

        Date currentDate = DateUtils.truncate(new Date());
        int[] diffArr = {0, 2, 6, 9}; // 1일전, 3일전, 7일전, 10일전

        //트레이닝 완료 되지 않았을 경우,
        BooleanBuilder completeStatus = new BooleanBuilder();
        QTrainingLog qTrainingLog = QTrainingLog.trainingLog;
        completeStatus.and(qTrainingLog.status.notIn(TrainingStatus.COMPLETED).or(qTrainingLog.status.isNull()));
        
        iterable.forEach(user -> {
            log.info("@SOP 트레이닝 이메일 알림을 전송한다.");
            List<MyTraining> trainingList = trainingMatrixRepository.getDownloadTrainingList(null, user.getId(), null, null, completeStatus);

            for(int compareDiff : diffArr) {
                log.info("@Diff : {}", compareDiff);
                List<MyTraining> filtered = trainingList.stream()
                        .filter(t -> {
                            long diff = DateUtils.diff(currentDate, t.getUserEndDate());
                            log.debug("=> Current Date : {}, Training End Date : {}, diff : {}", currentDate, t.getUserEndDate(), diff);
                            return (diff == compareDiff) && (ObjectUtils.isEmpty(t.getTrainingLog()) || ObjectUtils.isEmpty(t.getTrainingLog().getCompleteDate()));
                        })
                        .distinct().collect(Collectors.toList());

                if (ObjectUtils.isEmpty(filtered) == false) {
                    log.info("=> @User : {} 사용자 Training 진행 해야하는 SOP 존재함.", user.getUsername());
                    HashMap<String, Object> model = new HashMap<>();
                    model.put("trainings", filtered);
                    model.put("diff", (compareDiff + 1));

                    Mail mail = Mail.builder()
                            .to(new String[]{user.getEmail()})
                            .subject("SOP Training 공지 (" + (compareDiff + 1) + ")일 전")
                            .model(model)
                            .templateName("training-alert-template")
                            .build();
                    mailService.sendMail(mail);
                }
            }
        });
    }
}
