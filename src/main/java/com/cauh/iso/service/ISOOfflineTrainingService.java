package com.cauh.iso.service;

import com.cauh.common.entity.Account;
import com.cauh.common.entity.QAccount;
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
import com.cauh.iso.repository.ISOOfflineTrainingAttendeeRepository;
import com.cauh.iso.repository.ISOOfflineTrainingDocumentRepository;
import com.cauh.iso.repository.ISOOfflineTrainingRepository;
import com.querydsl.core.BooleanBuilder;
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

    public Optional<ISOOfflineTraining> findById(Integer id){
        return isoOfflineTrainingRepository.findById(id);
    }

    public Page<ISOOfflineTraining> findAll(Pageable pageable){
        return isoOfflineTrainingRepository.findAll(pageable);
    }

    @Transactional
    public ISOOfflineTraining isoOfflineTrainingApply(Integer id) {
        ISOOfflineTraining isoOfflineTraining = findById(id).get();
        isoOfflineTraining.setStatus(OfflineTrainingStatus.APPROVED);

        for(ISOOfflineTrainingDocument doc : isoOfflineTraining.getIsoOfflineTrainingDocuments()) {
            double time = Double.parseDouble(doc.getHour()) * 3600;
//            log.debug("=> {} -> ?????? ?????? : {}", doc.getHour(), time);

            for(ISOOfflineTrainingAttendee attendee : isoOfflineTraining.getIsoOfflineTrainingAttendees()) {
                ISOTrainingLog trainingLog = new ISOTrainingLog();
                trainingLog.setIso(doc.getIso());
                trainingLog.setIsoOfflineTraining(isoOfflineTraining);
                trainingLog.setCompleteDate(isoOfflineTraining.getTrainingDate());
                trainingLog.setOrganizationOther(isoOfflineTraining.getOrganization());
                trainingLog.setStatus(TrainingStatus.COMPLETED);
                trainingLog.setType(TrainingType.OTHER);
                trainingLog.setTrainingTime((int)time);
                trainingLog.setUser(attendee.getAccount());

                log.debug("=> EmpNo : {} Training ?????? ?????? : {}", attendee.getAccount().getEmpNo(), doc.getIso().getId());
                ISOTrainingLog savedTrainingLog = isoTrainingLogService.saveOrUpdate(trainingLog, null);
                log.info("<== Offline Training Log : {}, savedTrainingLog Id : {}", attendee.getAccount().getEmpNo(), savedTrainingLog.getId());
            }
        }


        ISOOfflineTraining savedOfflineTraining = isoOfflineTrainingRepository.save(isoOfflineTraining);//?????? ????????????

        return savedOfflineTraining;
    }

    @Transactional(readOnly = true)
    public void sendApplyEmail(ISOOfflineTraining isoOfflineTraining) {
        try {
            log.debug("==> ???????????? ?????? Apply ?????? ?????? ?????? ??????");
//            QUserJobDescription qUserJobDescription = QUserJobDescription.userJobDescription;
//            BooleanBuilder aBuilder = new BooleanBuilder();
//            aBuilder.and(qUserJobDescription.jobDescription.shortName.eq("QAA"));
//
//            Iterable<UserJobDescription> userJobDescriptions = userJobDescriptionRepository.findAll(aBuilder);
//            List<String> toList = offlineTraining.getOfflineTrainingAttendees().stream().filter(a -> StringUtils.isEmpty(a.getUser().getEmail()) == false).map(a -> a.getUser().getEmail()).collect(Collectors.toList());
            List<String> toList = isoOfflineTraining.getIsoOfflineTrainingAttendees().stream()
                    .filter(a -> a.getAccount().isEnabled() && a.getAccount().isReceiveEmail())
                    .map(u -> u.getAccount().getEmail())
                    .collect(Collectors.toList());

            if(ObjectUtils.isEmpty(toList) == false) {
//                List<String> ccList = StreamSupport.stream(userJobDescriptions.spliterator(), false)
//                        .filter(u -> StringUtils.isEmpty(u.getUser().getEmail()) == false)
//                        .map(u -> u.getUser().getEmail()).collect(Collectors.toList());
                log.info("=> Off-line ?????? ?????? to : {}", toList);
                HashMap<String, Object> model = new HashMap<>();
                model.put("isoOfflineTraining", isoOfflineTraining);
                Mail mail = Mail.builder()
                        .subject("[ISO-MS] ISO Off-line Training ?????? ??????")
                        .to(toList.toArray(new String[toList.size()]))
//                        .cc(ccList.toArray(new String[ccList.size()]))
                        .templateName("iso-offline-training-apply")
                        .model(model)
                        .build();

                mailService.sendMail(mail);
            } else {
                log.debug("<= ISO Offline Training ?????? ?????? ?????????????????? ????????????.");
            }
            log.debug("==> ???????????? ?????? Apply ?????? ?????? ?????? ??????");
        } catch (Exception error) {
            log.error("Offline ?????? Apply ?????? ?????? ?????? ??? ?????? : {}", error);
        }
    }

    @Transactional
    public ISOOfflineTraining save(ISOOfflineTraining isoOfflineTraining) {
        if(ObjectUtils.isEmpty(isoOfflineTraining.getId()) == false) {
            log.debug("==> ?????? ????????? offline training document ?????? ??????");
            BooleanBuilder docBuilder = new BooleanBuilder();
            QISOOfflineTrainingDocument qisoOfflineTrainingDocument = QISOOfflineTrainingDocument.iSOOfflineTrainingDocument;
            docBuilder.and(qisoOfflineTrainingDocument.isoOfflineTraining.id.eq(isoOfflineTraining.getId()));

            Iterable<ISOOfflineTrainingDocument> offlineTrainingDocuments = isoOfflineTrainingDocumentRepository.findAll(docBuilder);
            isoOfflineTrainingDocumentRepository.deleteAll(offlineTrainingDocuments);
            log.debug("==> ?????? ????????? offline training attendee ?????? ??????");
            BooleanBuilder atdBuilder = new BooleanBuilder();
            QOfflineTrainingAttendee qOfflineTrainingAttendee = QOfflineTrainingAttendee.offlineTrainingAttendee;
            atdBuilder.and(qOfflineTrainingAttendee.offlineTraining.id.eq(isoOfflineTraining.getId()));

            Iterable<ISOOfflineTrainingAttendee> offlineTrainingAttendees = isoOfflineTrainingAttendeeRepository.findAll(atdBuilder);
            isoOfflineTrainingAttendeeRepository.deleteAll(offlineTrainingAttendees);
        }
        ISOOfflineTraining savedIsoOfflineTraining =  isoOfflineTrainingRepository.save(isoOfflineTraining);

        log.debug("=> offline training document ??????");
        for(ISOOfflineTrainingDocument doc : isoOfflineTraining.getIsoOfflineTrainingDocuments()) {
            doc.setIsoOfflineTraining(savedIsoOfflineTraining);
            isoOfflineTrainingDocumentRepository.save(doc);
        }

        log.debug("=> offline training attendee ??????");
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
            log.debug("==> ???????????? ?????? ?????? ?????? ?????? ??????");
//            OfflineTraining offlineTraining = findById(id).get();
            QUserJobDescription qUserJobDescription = QUserJobDescription.userJobDescription;
            BooleanBuilder aBuilder = new BooleanBuilder();

            //?????? Role??? Enabled ??????????????? Manager ????????? ???????????? ??????
            aBuilder.and(qUserJobDescription.jobDescription.manager.eq(true))
                    .and(qUserJobDescription.jobDescription.enabled.eq(true));

            Iterable<UserJobDescription> userJobDescriptions = userJobDescriptionRepository.findAll(aBuilder);
            List<String> toUserList = StreamSupport.stream(userJobDescriptions.spliterator(), false)
                    .map(u -> u.getUser().getUsername())
                    .distinct().collect(Collectors.toList());
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
                List<Integer> ccUserIds = Arrays.stream(isoOfflineTraining.getAttendees()).map(a -> Integer.valueOf(a)).collect(Collectors.toList());
                uBuilder.and(qUser.id.in(ccUserIds));
                uBuilder.and(qUser.enabled.eq(true));
                uBuilder.and(qUser.receiveEmail.eq(true));
                uBuilder.and(qUser.userStatus.eq(UserStatus.ACTIVE));

                Iterable<Account> users = userRepository.findAll(uBuilder);
                List<String> ccList = StreamSupport.stream(users.spliterator(), false).filter(a -> StringUtils.isEmpty(a.getEmail()) == false).map(a -> a.getEmail()).collect(Collectors.toList());
                log.info("=> Off-line ?????? ?????? to : {}, cc : {}", toList, ccList);
//                log.info("=> Off-line ?????? ?????? ?????? ?????? to : {}", toList);
                HashMap<String, Object> model = new HashMap<>();
                model.put("isoOfflineTraining", isoOfflineTraining);

                if(!ObjectUtils.isEmpty(isoOfflineTraining.getAttendees())) {
                    List<Integer> userIds = Arrays.stream(isoOfflineTraining.getAttendees()).map(userId -> Integer.parseInt(userId)).collect(Collectors.toList());
                    BooleanBuilder builder = new BooleanBuilder();
                    QAccount q = QAccount.account;
                    builder.and(q.id.in(userIds));
                    builder.and(q.userStatus.eq(UserStatus.ACTIVE));

                    model.put("attendees", userRepository.findAll(builder));
                }

                model.put("requester", requester);
                Mail mail = Mail.builder()
                        .subject("[ISO-MS] ISO Off-line Training ?????? ??????")
                        .to(toList.toArray(new String[toList.size()]))
                        .cc(ccList.toArray(new String[ccList.size()]))
                        .templateName("iso-offline-training-submitted")
                        .model(model)
                        .build();

                mailService.sendMail(mail);
            } else {
                log.debug("QA - JD ????????? ????????? ????????????.");
            }
            log.debug("==> ???????????? ?????? ?????? ?????? ?????? ??????");
        } catch (Exception error) {
            log.error("Offline ?????? ?????? ?????? ?????? ?????? ??? ?????? : {}", error);
        }
    }

    public void delete(Integer id) {
        Optional<ISOOfflineTraining> optionalIsoOfflineTraining = findById(id);
        if(optionalIsoOfflineTraining.isPresent()) {
            ISOOfflineTraining offlineTraining = optionalIsoOfflineTraining.get();
            offlineTraining.setStatus(OfflineTrainingStatus.DELETED);
//            offlineTraining.setDeleted(true);
            isoOfflineTrainingRepository.save(offlineTraining);

            log.info("Offline Training Id : {} ?????? ?????? ??????.", id);
        } else {
            log.warn("Offline Training Id : {} ???????????? ?????? ID.", id);
        }
    }

}
