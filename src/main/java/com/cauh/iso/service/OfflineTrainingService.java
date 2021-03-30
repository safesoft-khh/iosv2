package com.cauh.iso.service;

import com.cauh.common.entity.QAccount;
import com.cauh.common.entity.Account;
import com.cauh.common.entity.QUserJobDescription;
import com.cauh.common.entity.UserJobDescription;
import com.cauh.common.entity.constant.UserStatus;
import com.cauh.common.repository.UserJobDescriptionRepository;
import com.cauh.common.repository.UserRepository;
import com.cauh.iso.domain.*;
import com.cauh.iso.domain.constant.DeviationReportStatus;
import com.cauh.iso.domain.constant.OfflineTrainingStatus;
import com.cauh.iso.domain.constant.TrainingStatus;
import com.cauh.iso.domain.constant.TrainingType;
import com.cauh.iso.repository.OfflineTrainingAttendeeRepository;
import com.cauh.iso.repository.OfflineTrainingDocumentRepository;
import com.cauh.iso.repository.OfflineTrainingRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class OfflineTrainingService {
    private final OfflineTrainingRepository offlineTrainingRepository;
    private final OfflineTrainingDocumentRepository offlineTrainingDocumentRepository;
    private final OfflineTrainingAttendeeRepository offlineTrainingAttendeeRepository;
    private final TrainingLogService trainingLogService;
    private final UserJobDescriptionRepository userJobDescriptionRepository;
    private final MailService mailService;
    private final UserRepository userRepository;

    @Value("${role.receive-email}")
    private String receiveEmailRole;

    public Optional<OfflineTraining> findById(Integer id) {
        return offlineTrainingRepository.findById(id);
    }

    @Transactional
    public OfflineTraining save(OfflineTraining offlineTraining) {
        if(ObjectUtils.isEmpty(offlineTraining.getId()) == false) {
            log.debug("==> 이미 등록된 offline training document 삭제 처리");
            BooleanBuilder docBuilder = new BooleanBuilder();
            QOfflineTrainingDocument qOfflineTrainingDocument = QOfflineTrainingDocument.offlineTrainingDocument;
            docBuilder.and(qOfflineTrainingDocument.offlineTraining.id.eq(offlineTraining.getId()));

            Iterable<OfflineTrainingDocument> offlineTrainingDocuments = offlineTrainingDocumentRepository.findAll(docBuilder);
            offlineTrainingDocumentRepository.deleteAll(offlineTrainingDocuments);
            log.debug("==> 이미 등록된 offline training attendee 삭제 처리");
            BooleanBuilder atdBuilder = new BooleanBuilder();
            QOfflineTrainingAttendee qOfflineTrainingAttendee = QOfflineTrainingAttendee.offlineTrainingAttendee;
            atdBuilder.and(qOfflineTrainingAttendee.offlineTraining.id.eq(offlineTraining.getId()));

            Iterable<OfflineTrainingAttendee> offlineTrainingAttendees = offlineTrainingAttendeeRepository.findAll(atdBuilder);
            offlineTrainingAttendeeRepository.deleteAll(offlineTrainingAttendees);
        }
        OfflineTraining savedOfflineTraining =  offlineTrainingRepository.save(offlineTraining);

        log.debug("=> offline training document 등록");
        for(OfflineTrainingDocument doc : offlineTraining.getOfflineTrainingDocuments()) {
            doc.setOfflineTraining(savedOfflineTraining);
            offlineTrainingDocumentRepository.save(doc);
        }

        log.debug("=> offline training attendee 등록");
        for(String userId : offlineTraining.getAttendees()) {
            OfflineTrainingAttendee attendee = new OfflineTrainingAttendee();
            attendee.setOfflineTraining(savedOfflineTraining);
            attendee.setAccount(Account.builder().id(Integer.valueOf(userId)).build());
            offlineTrainingAttendeeRepository.save(attendee);
        }
        return savedOfflineTraining;
    }

    public void sendSubmittedEmail(Account requester, OfflineTraining offlineTraining) {
        try {
            log.debug("==> 오프라인 교육 알림 메일 전송 시작");
//            OfflineTraining offlineTraining = findById(id).get();
            QUserJobDescription qUserJobDescription = QUserJobDescription.userJobDescription;
            BooleanBuilder aBuilder = new BooleanBuilder();

            //속한 Role이 Enabled 상태이면서 Manager 권한을 갖고있는 경우
            aBuilder.and(qUserJobDescription.jobDescription.manager.eq(true))
                    .and(qUserJobDescription.jobDescription.enabled.eq(true));

            Iterable<UserJobDescription> userJobDescriptions = userJobDescriptionRepository.findAll(aBuilder);
            List<String> toUserList = StreamSupport.stream(userJobDescriptions.spliterator(), false)
                    .map(u -> u.getUser().getUsername())
                    .distinct()
                    .collect(Collectors.toList());
            QAccount qUser = QAccount.account;
            BooleanBuilder inBuilder = new BooleanBuilder();
            inBuilder.and(qUser.username.in(toUserList));
            inBuilder.and(qUser.userStatus.eq(UserStatus.ACTIVE));
            Iterable<Account> iterable = userRepository.findAll(inBuilder);

            if(ObjectUtils.isEmpty(iterable) == false) {
                List<String> toList = StreamSupport.stream(iterable.spliterator(), false)
                        .filter(u -> StringUtils.isEmpty(u.getEmail()) == false)
                        .map(Account::getEmail)
                        .collect(Collectors.toList());
                BooleanBuilder uBuilder = new BooleanBuilder();
                List<Integer> ccUserIds = Arrays.stream(offlineTraining.getAttendees()).map(a -> Integer.valueOf(a)).collect(Collectors.toList());
                uBuilder.and(qUser.id.in(ccUserIds));
                uBuilder.and(qUser.enabled.eq(true));
                uBuilder.and(qUser.receiveEmail.eq(true));
                uBuilder.and(qUser.userStatus.eq(UserStatus.ACTIVE));
                Iterable<Account> users = userRepository.findAll(uBuilder);
                List<String> ccList = StreamSupport.stream(users.spliterator(), false).filter(a -> StringUtils.isEmpty(a.getEmail()) == false).map(a -> a.getEmail()).collect(Collectors.toList());
                log.info("=> Off-line 등록 메일 to : {}, cc : {}", toList, ccList);
//                log.info("=> Off-line 등록 요청 알림 메일 to : {}", toList);
                HashMap<String, Object> model = new HashMap<>();
                model.put("offlineTraining", offlineTraining);

                if(!ObjectUtils.isEmpty(offlineTraining.getAttendees())) {
                    List<Integer> userIds = Arrays.stream(offlineTraining.getAttendees()).map(userId -> Integer.parseInt(userId)).collect(Collectors.toList());
                    BooleanBuilder builder = new BooleanBuilder();
                    QAccount q = QAccount.account;
                    builder.and(q.id.in(userIds));
                    builder.and(qUser.userStatus.eq(UserStatus.ACTIVE));
                    model.put("attendees", userRepository.findAll(builder));
                }

                model.put("requester", requester);
                Mail mail = Mail.builder()
                        .subject("[ISO MS] SOP Off-line Training 등록 요청")
                        .to(toList.toArray(new String[toList.size()]))
                        .cc(ccList.toArray(new String[ccList.size()]))
                        .templateName("offline-training-submitted")
                        .model(model)
                        .build();

                mailService.sendMail(mail);
            } else {
                log.debug("QA - JD 등록된 유저가 없습니다.");
            }
            log.debug("==> 오프라인 교육 알림 메일 전송 종료");
        } catch (Exception error) {
            log.error("Offline 교육 등록 알림 메일 전송 중 오류 : {}", error);
        }
    }

    @Transactional
    public OfflineTraining offlineTrainingApply(Integer id) {
        OfflineTraining offlineTraining = findById(id).get();
        offlineTraining.setStatus(OfflineTrainingStatus.APPROVED);

        for(OfflineTrainingDocument doc : offlineTraining.getOfflineTrainingDocuments()) {
            double time = Double.parseDouble(doc.getHour()) * 3600;
//            log.debug("=> {} -> 초로 변환 : {}", doc.getHour(), time);


            for(OfflineTrainingAttendee attendee : offlineTraining.getOfflineTrainingAttendees()) {
                TrainingLog trainingLog = new TrainingLog();
                trainingLog.setDocumentVersion(doc.getDocumentVersion());
                trainingLog.setOfflineTraining(offlineTraining);
                trainingLog.setCompleteDate(offlineTraining.getTrainingDate());
                trainingLog.setOrganizationOther(offlineTraining.getOrganization());
                trainingLog.setReportStatus(DeviationReportStatus.NA);
                trainingLog.setStatus(TrainingStatus.COMPLETED);
                trainingLog.setType(TrainingType.OTHER);
                trainingLog.setTrainingTime((int)time);
                trainingLog.setUser(attendee.getAccount());

                log.debug("=> ID : {} Training 이력 추가 : {}", attendee.getAccount().getUsername(), doc.getDocumentVersion().getId());
                TrainingLog savedTrainingLog = trainingLogService.saveOrUpdate(trainingLog, null);
                log.info("<== Offline Training Log : {}, savedTrainingLog Id : {}", attendee.getAccount().getUsername(), savedTrainingLog.getId());
            }
        }


        OfflineTraining savedOfflineTraining = offlineTrainingRepository.save(offlineTraining);//상태 업데이트

        return savedOfflineTraining;
    }

    @Transactional(readOnly = true)
    public void sendApplyEmail(OfflineTraining offlineTraining) {
        try {
            log.debug("==> 오프라인 교육 Apply 알림 메일 전송 시작");
//            QUserJobDescription qUserJobDescription = QUserJobDescription.userJobDescription;
//            BooleanBuilder aBuilder = new BooleanBuilder();
//            aBuilder.and(qUserJobDescription.jobDescription.shortName.eq("QAA"));
//
//            Iterable<UserJobDescription> userJobDescriptions = userJobDescriptionRepository.findAll(aBuilder);
//            List<String> toList = offlineTraining.getOfflineTrainingAttendees().stream().filter(a -> StringUtils.isEmpty(a.getUser().getEmail()) == false).map(a -> a.getUser().getEmail()).collect(Collectors.toList());
            List<String> toList = offlineTraining.getOfflineTrainingAttendees().stream()
                    .filter(a -> a.getAccount().isEnabled() && a.getAccount().isReceiveEmail())
                    .map(u -> u.getAccount().getEmail())
                    .collect(Collectors.toList());

            if(ObjectUtils.isEmpty(toList) == false) {
//                List<String> ccList = StreamSupport.stream(userJobDescriptions.spliterator(), false)
//                        .filter(u -> StringUtils.isEmpty(u.getUser().getEmail()) == false)
//                        .map(u -> u.getUser().getEmail()).collect(Collectors.toList());
                log.info("=> Off-line 등록 메일 to : {}", toList);
                HashMap<String, Object> model = new HashMap<>();
                model.put("offlineTraining", offlineTraining);
                Mail mail = Mail.builder()
                        .subject("[ISO MS] SOP Off-line Training 반영 알림")
                        .to(toList.toArray(new String[toList.size()]))
//                        .cc(ccList.toArray(new String[ccList.size()]))
                        .templateName("offline-training-apply")
                        .model(model)
                        .build();

                mailService.sendMail(mail);
            } else {
                log.debug("<= Offline Training 등록 완료 수신대상자가 없습니다.");
            }
            log.debug("==> 오프라인 교육 Apply 알림 메일 전송 종료");
        } catch (Exception error) {
            log.error("Offline 교육 Apply 알림 메일 전송 중 오류 : {}", error);
        }
    }

    public Page<OfflineTraining> findAll(Predicate predicate, Pageable pageable) {
        return offlineTrainingRepository.findAll(predicate, pageable);
    }
    public Page<OfflineTraining> findAll(Pageable pageable) {
        return offlineTrainingRepository.findAll(pageable);
    }

    public void delete(Integer id) {
        Optional<OfflineTraining> optionalOfflineTraining = findById(id);
        if(optionalOfflineTraining.isPresent()) {
            OfflineTraining offlineTraining = optionalOfflineTraining.get();
            offlineTraining.setStatus(OfflineTrainingStatus.DELETED);
//            offlineTraining.setDeleted(true);
            offlineTrainingRepository.save(offlineTraining);

            log.info("Offline Training Id : {} 삭제 처리 완료.", id);
        } else {
            log.warn("Offline Training Id : {} 존재하지 않는 ID.", id);
        }
    }
}
