package com.cauh.iso.service;

import com.cauh.common.entity.Account;
import com.cauh.common.entity.QAccount;
import com.cauh.common.entity.QUserJobDescription;
import com.cauh.common.entity.UserJobDescription;
import com.cauh.common.repository.UserJobDescriptionRepository;
import com.cauh.common.repository.UserRepository;
import com.cauh.iso.domain.*;
import com.cauh.iso.repository.ISOOfflineTrainingAttendeeRepository;
import com.cauh.iso.repository.ISOOfflineTrainingDocumentRepository;
import com.cauh.iso.repository.ISOOfflineTrainingRepository;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class ISOOfflineTrainingService {

    private final ISOOfflineTrainingRepository isoOfflineTrainingRepository;
    private final ISOOfflineTrainingDocumentRepository isoOfflineTrainingDocumentRepository;
    private final ISOOfflineTrainingAttendeeRepository isoOfflineTrainingAttendeeRepository;

    private final ISOTrainingLogService isoTrainingLogService;
    private final UserJobDescriptionRepository userJobDescriptionRepository;
    private final MailService mailService;
    private final UserRepository userRepository;

    @Value("${role.receive-email}")
    private String receiveEmailRole;

    @Transactional
    public ISOOfflineTraining save(ISOOfflineTraining isoOfflineTraining) {
        if(ObjectUtils.isEmpty(isoOfflineTraining.getId()) == false) {
            log.debug("==> 이미 등록된 offline training document 삭제 처리");
            BooleanBuilder docBuilder = new BooleanBuilder();
            QISOOfflineTrainingDocument qisoOfflineTrainingDocument = QISOOfflineTrainingDocument.iSOOfflineTrainingDocument;
            docBuilder.and(qisoOfflineTrainingDocument.isoOfflineTraining.id.eq(isoOfflineTraining.getId()));

            Iterable<ISOOfflineTrainingDocument> offlineTrainingDocuments = isoOfflineTrainingDocumentRepository.findAll(docBuilder);
            isoOfflineTrainingDocumentRepository.deleteAll(offlineTrainingDocuments);
            log.debug("==> 이미 등록된 offline training attendee 삭제 처리");
            BooleanBuilder atdBuilder = new BooleanBuilder();
            QOfflineTrainingAttendee qOfflineTrainingAttendee = QOfflineTrainingAttendee.offlineTrainingAttendee;
            atdBuilder.and(qOfflineTrainingAttendee.offlineTraining.id.eq(isoOfflineTraining.getId()));

            Iterable<ISOOfflineTrainingAttendee> offlineTrainingAttendees = isoOfflineTrainingAttendeeRepository.findAll(atdBuilder);
            isoOfflineTrainingAttendeeRepository.deleteAll(offlineTrainingAttendees);
        }
        ISOOfflineTraining savedIsoOfflineTraining =  isoOfflineTrainingRepository.save(isoOfflineTraining);

        log.debug("=> offline training document 등록");
        for(ISOOfflineTrainingDocument doc : isoOfflineTraining.getIsoOfflineTrainingDocuments()) {
            doc.setIsoOfflineTraining(savedIsoOfflineTraining);
            isoOfflineTrainingDocumentRepository.save(doc);
        }

        log.debug("=> offline training attendee 등록");
        for(String userId : isoOfflineTraining.getAttendees()) {
            ISOOfflineTrainingAttendee attendee = new ISOOfflineTrainingAttendee();
            attendee.setIsoOfflineTraining(savedIsoOfflineTraining);
            attendee.setAccount(Account.builder().id(Integer.valueOf(userId)).build());
            isoOfflineTrainingAttendeeRepository.save(attendee);
        }
        return savedIsoOfflineTraining;
    }

    public void sendSubmittedEmail(Account requester, ISOOfflineTraining isoOfflineTraining) {
        try {
            log.debug("==> 오프라인 교육 알림 메일 전송 시작");
//            OfflineTraining offlineTraining = findById(id).get();
            QUserJobDescription qUserJobDescription = QUserJobDescription.userJobDescription;
            BooleanBuilder aBuilder = new BooleanBuilder();
            aBuilder.and(qUserJobDescription.jobDescription.shortName.in(Arrays.asList(receiveEmailRole.split(","))));

            Iterable<UserJobDescription> userJobDescriptions = userJobDescriptionRepository.findAll(aBuilder);
            List<String> toUserList = StreamSupport.stream(userJobDescriptions.spliterator(), false)
                    .map(u -> u.getUser().getUsername())
                    .distinct()
                    .collect(Collectors.toList());
            QAccount qUser = QAccount.account;
            BooleanBuilder inBuilder = new BooleanBuilder();
            inBuilder.and(qUser.username.in(toUserList));
            Iterable<Account> iterable = userRepository.findAll(inBuilder);

            if(ObjectUtils.isEmpty(iterable) == false) {
                List<String> toList = StreamSupport.stream(iterable.spliterator(), false)
                        .filter(u -> StringUtils.isEmpty(u.getEmail()) == false)
                        .map(Account::getEmail)
                        .collect(Collectors.toList());
                BooleanBuilder uBuilder = new BooleanBuilder();
                List<Integer> ccUserIds = Arrays.stream(isoOfflineTraining.getAttendees()).map(a -> Integer.valueOf(a)).collect(Collectors.toList());
                uBuilder.and(qUser.id.in(ccUserIds));
                uBuilder.and(qUser.enabled.eq(true));
                uBuilder.and(qUser.receiveEmail.eq(true));
                Iterable<Account> users = userRepository.findAll(uBuilder);
                List<String> ccList = StreamSupport.stream(users.spliterator(), false).filter(a -> StringUtils.isEmpty(a.getEmail()) == false).map(a -> a.getEmail()).collect(Collectors.toList());
                log.info("=> Off-line 등록 메일 to : {}, cc : {}", toList, ccList);
//                log.info("=> Off-line 등록 요청 알림 메일 to : {}", toList);
                HashMap<String, Object> model = new HashMap<>();
                model.put("offlineTraining", isoOfflineTraining);

                if(!ObjectUtils.isEmpty(isoOfflineTraining.getAttendees())) {
                    List<Integer> userIds = Arrays.stream(isoOfflineTraining.getAttendees()).map(userId -> Integer.parseInt(userId)).collect(Collectors.toList());
                    BooleanBuilder builder = new BooleanBuilder();
                    QAccount q = QAccount.account;
                    builder.and(q.id.in(userIds));

                    model.put("attendees", userRepository.findAll(builder));
                }

                model.put("requester", requester);
                Mail mail = Mail.builder()
                        .subject("[ISO-MS] ISO Off-line Training 등록 요청")
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

}
