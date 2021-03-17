package com.cauh.iso.service;

import com.cauh.common.entity.Account;
import com.cauh.common.repository.SignatureRepository;
import com.cauh.common.service.UserService;
import com.cauh.iso.domain.*;
import com.cauh.iso.domain.constant.*;
import com.cauh.iso.domain.report.*;
import com.cauh.iso.repository.*;
import com.cauh.iso.xdocreport.dto.TrainingDeviationLogDTO;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalService {
    private final ApprovalRepository approvalRepository;
    private final ApprovalLineRepository approvalLineRepository;
    private final TrainingPeriodService trainingPeriodService;
    private final TrainingLogService trainingLogService;
    private final SOPDeviationReportRepository sopDeviationReportRepository;
    private final SopRfRequestFormRepository sopRfRequestFormRepository;
    private final SopRfRevisionDocRepository sopRfRevisionDocRepository;
    private final SopRfDevelopmentDocRepository sopRfDevelopmentDocRepository;
//    private final RdApprovalFormRepository rdApprovalFormRepository;
    private final SOPWaiverApprovalFormRepository sopWaiverApprovalFormRepository;
    private final SOPDisclosureRequestFormRepository sopDisclosureRequestFormRepository;
    private final RequestedDocumentRepository requestedDocumentRepository;
    private final RetirementApprovalFormRepository retirementApprovalFormRepository;
    private final RetirementDocumentRepository retirementDocumentRepository;
    private final DocumentVersionService documentVersionService;
    private final DocumentVersionRepository documentVersionRepository;
    private final MailService mailService;
    private final ExternalCustomerRepository externalCustomerRepository;
    private final UserService userService;
    private final SignatureRepository signatureRepository;
    private final CategoryService categoryService;
    private final DocumentRepository documentRepository;
    private final DocumentService documentService;
    private final DisclosureSOPTrainingLogRepository disclosureSOPTrainingLogRepository;
    private final DisclosureISOTrainingLogRepository disclosureISOTrainingLogRepository;
//    @PersistenceContext
//    private EntityManager entityManager;

    @Value("${site.company-title}")
    private String siteCompanyTitle;

    @Value("${site.code}")
    private String siteCode;

    public void delete(Integer id) {
        Approval approval = findById(id).get();
        log.info("Approval 삭제 : {}", id);
        if(approval.getType() == ReportType.SOP_Training_Deviation_Report) {
            if(!ObjectUtils.isEmpty(approval.getSopDeviationReport().getTrainingLogId())) {
                log.info("삭제 문서 SOP Deviation Report Training Log Id 존재함 : {}", approval.getSopDeviationReport().getTrainingLogId());
                TrainingLog trainingLog = trainingLogService.findById(approval.getSopDeviationReport().getTrainingLogId()).get();
                trainingLog.setReportStatus(DeviationReportStatus.DELETED);

                trainingLogService.saveOrUpdate(trainingLog, null);
            }
        }
        approval.setDeleted(true);

        approvalRepository.save(approval);
    }

    public Optional<Approval> findById(Integer id) {
        return approvalRepository.findById(id);
    }

    public Page<Approval> findAll(ApprovalLineType type, ApprovalStatus status, Account user, Pageable pageable) {
        return approvalLineRepository.findAll(type, status, user, pageable);
    }

    public List<TrainingDeviationLogDTO> findAllByReportType(ReportType reportType) {
        return approvalLineRepository.findAllByReportType(reportType);
    }

    public List<TrainingDeviationLogDTO> findAllByReportTypeAndStatus(ReportType reportType, ApprovalStatus status) {
        return approvalLineRepository.findAllByReportTypeAndStatus(reportType, status);
    }

    public void saveOrUpdate(Approval approval, Account user, boolean sendEmail) throws Exception {
        if (ObjectUtils.isEmpty(approval.getApprovalLines())) {
            log.info("@ApprovalLine 생성");
            approval.setApprovalLines(new ArrayList<>());
        }
//        log.info("approval.lines size : {}", approval.getApprovalLines().size());
//        log.info("approval.lines : {}", approval.getApprovalLines());

        if(ObjectUtils.isEmpty(approval.getId())) {
            if(ObjectUtils.isEmpty(approval.getApprovalLines())) {
                approval.setApprovalLines(new ArrayList<>());
                approval.getApprovalLines().add(ApprovalLine.builder().username(user.getUsername())
                        .lineType(ApprovalLineType.requester)
                        .status(ApprovalStatus.approved)
                        .base64signature(signatureRepository.findById(user.getUsername()).get().getBase64signature())
                        .displayName(user.getName() + (StringUtils.isEmpty(user.getPosition()) ? "" : "(" + user.getPosition() + ")"))
                        .build());
            } else {
                log.info("신규 요청");

                approval.getApprovalLines().set(0, ApprovalLine.builder().username(user.getUsername())
                        .lineType(ApprovalLineType.requester)
                        .status(ApprovalStatus.approved)
                        .base64signature(signatureRepository.findById(user.getUsername()).get().getBase64signature())
                        .displayName(user.getName() + (StringUtils.isEmpty(user.getPosition()) ? "" : "(" + user.getPosition() + ")"))
                        .build());
            }
        }


        //Report 별로 커스텀 로직 구현
        reportCustomProcess(approval, user);
        approval.setUsername(user.getUsername());
        Approval savedApproval = approvalRepository.save(approval);


        if(approval.isRenew() == false && ObjectUtils.isEmpty(approval.getId()) == false && approval.getApprovalLines().stream().filter(l -> ObjectUtils.isEmpty(l.getId())).count() > 0) {
            log.debug("==> Approval ID : {} - 결제 라인을 초기화 한다.", approval.getId());
            removeAllLines(approval);
        }
        //첫번째 결재 유저
        Optional<ApprovalLine> optionalApprovalLine = approval.getApprovalLines().stream().filter(l -> l.getLineType() != ApprovalLineType.requester).findFirst();
        if(optionalApprovalLine.isPresent()) {
            ApprovalLine approvalLine = optionalApprovalLine.get();
            //임시저장인 경우 temp로 상태지정
            approvalLine.setStatus(approval.getStatus()==ApprovalStatus.temp?ApprovalStatus.temp:ApprovalStatus.request);
            log.info("** 첫번째 결재할 유저 확인 됨. {}", approvalLine.getDisplayName());
        }

        for(ApprovalLine approvalLine : approval.getApprovalLines()) {
            if(ObjectUtils.isEmpty(approvalLine.getId())) {
                log.info("add approval[{}] line : {}", approval.getId(), approvalLine.getDisplayName());
                approvalLine.setApproval(savedApproval);
                approvalLineRepository.save(approvalLine);
            }
        }

        if(sendEmail) {
            /**
             * 검토 or 승인 요청 이메일 전송
             */
            Optional<Account> optionalUser = userService.findByUsername(approval.getApprovalLines().get(1).getUsername());
            if(optionalUser.isPresent()) {
                Account fromUser = userService.findByUsername(approval.getUsername()).get();
                sendReviewApprovalEmail(optionalUser.get(), fromUser, savedApproval, approval.getApprovalLines().get(1));
            }
        }

    }

    /**
     * 검토, 승인 자에게 메일 전송
     * @param toUser
     * @param fromUser
     * @param approval
     * @param approvalLine
     */
    private void sendReviewApprovalEmail(Account toUser, Account fromUser, Approval approval, ApprovalLine approvalLine) {
        HashMap<String, Object> model = new HashMap<>();
        String strLineType = (approvalLine.getLineType() == ApprovalLineType.reviewer ? "검토" : "승인");
//        Approval app = approvalRepository.findById(approval.getId()).get();
        model.put("approval", approval);
        addModel(approval, model);
        model.put("message", String.format("%s 에 대한 %s 요청이 있습니다.", approval.getType().getLabel(), strLineType));

        Mail mail = Mail.builder()
                .to(new String[]{toUser.getEmail()})
                .subject(String.format("[ISO MS/전자결재/%s] %s %s 요청", fromUser.getName(), approval.getType().getLabel(), strLineType))
                .model(model)
                .templateName("approval-request")
                .build();

        mailService.sendMail(mail);
    }

    private void addModel(Approval approval, HashMap<String, Object> model) {
        if(approval.getType() == ReportType.SOP_Training_Deviation_Report) {
            DocumentVersion deviatedSOPDocument = documentVersionRepository.findById(approval.getSopDeviationReport().getDeviatedSOPDocument().getId()).get();
            model.put("deviatedSOPDocument", deviatedSOPDocument);
        } else if(approval.getType() == ReportType.SOP_RF_Request_Form) {
            SopRfRequestForm sopRfRequestForm = approval.getSopRfRequestForm();
            if(sopRfRequestForm.isSopRevision()) {
                QDocumentVersion qDocumentVersion = QDocumentVersion.documentVersion;
                BooleanBuilder builder = new BooleanBuilder();
                if(ObjectUtils.isEmpty(sopRfRequestForm.getSopRevisionIds())) {
                    List<String> sopIds = sopRfRequestForm.getSopRevisionDocs().stream().map(s -> s.getDocumentVersion().getId()).collect(Collectors.toList());
                    builder.and(qDocumentVersion.id.in(sopIds));
                } else {
                    builder.and(qDocumentVersion.id.in(sopRfRequestForm.getSopRevisionIds()));
                }
                model.put("sopRevisionDocs", documentVersionRepository.findAll(builder));
            }

            if(sopRfRequestForm.isRfRevision()) {
                QDocumentVersion qDocumentVersion = QDocumentVersion.documentVersion;
                BooleanBuilder builder = new BooleanBuilder();
                if(ObjectUtils.isEmpty(sopRfRequestForm.getRfRevisionIds())) {
                    List<String> rdIds = sopRfRequestForm.getRfRevisionDocs().stream().map(s -> s.getDocumentVersion().getId()).collect(Collectors.toList());
                    builder.and(qDocumentVersion.id.in(rdIds));
                } else {
                    builder.and(qDocumentVersion.id.in(sopRfRequestForm.getRfRevisionIds()));
                }

                model.put("rdRevisionDocs", documentVersionRepository.findAll(builder));
            }
        } else if(approval.getType() == ReportType.SOP_Waiver_Approval_Form) {
            model.put("deviatedSOP", documentVersionRepository.findById(approval.getSopWaiverApprovalForm().getDeviatedSOPDocument().getId()).get());
        } else if(approval.getType() == ReportType.SOP_RF_Retirement_Form) {
            if(!ObjectUtils.isEmpty(approval.getRetirementApprovalForm().getRetirementDocumentSOPs()) || !ObjectUtils.isEmpty(approval.getRetirementApprovalForm().getSopIds())) {
                QDocumentVersion qDocumentVersion = QDocumentVersion.documentVersion;
                BooleanBuilder builder = new BooleanBuilder();
                if (!ObjectUtils.isEmpty(approval.getRetirementApprovalForm().getRetirementDocumentSOPs())) {
                    List<String> sopIds = approval.getRetirementApprovalForm().getRetirementDocumentSOPs().stream().map(s -> s.getDocumentVersion().getId()).collect(Collectors.toList());
                    builder.and(qDocumentVersion.id.in(sopIds));
                } else {
                    builder.and(qDocumentVersion.id.in(approval.getRetirementApprovalForm().getSopIds()));
                }

                model.put("retirementSOPs", documentVersionRepository.findAll(builder));
            }
            if(!ObjectUtils.isEmpty(approval.getRetirementApprovalForm().getRetirementDocumentRFs()) || !ObjectUtils.isEmpty(approval.getRetirementApprovalForm().getRfIds())) {
                QDocumentVersion qDocumentVersion = QDocumentVersion.documentVersion;
                BooleanBuilder builder = new BooleanBuilder();
                if (!ObjectUtils.isEmpty(approval.getRetirementApprovalForm().getRetirementDocumentRFs())) {
                    List<String> rdIds = approval.getRetirementApprovalForm().getRetirementDocumentRFs().stream().map(s -> s.getDocumentVersion().getId()).collect(Collectors.toList());
                    builder.and(qDocumentVersion.id.in(rdIds));
                } else {
                    builder.and(qDocumentVersion.id.in(approval.getRetirementApprovalForm().getRfIds()));
                }

                model.put("retirementRFs", documentVersionRepository.findAll(builder));
            }
        }

    }


    /**
     * 승인, 반려시 발송
     * @param toUser
     * @param approval
     * @param resultStr
     */
    private void sendApprovalResultEmail(Account toUser, Approval approval, String resultStr) {
        HashMap<String, Object> model = new HashMap<>();
//        String strLineType = (approvalLine.getLineType() == ApprovalLineType.reviewer ? "검토" : "승인");
        model.put("approval", approval);
        addModel(approval, model);
        model.put("message", String.format("%s 에 대한 결재가 %s되었습니다.", approval.getType().getLabel(), resultStr));

        Mail mail = Mail.builder()
                .to(new String[]{toUser.getEmail()})
                .subject(String.format("[ISO-MS/전자결재/%s] %s", resultStr, approval.getType().getLabel()))
                .model(model)
                .templateName("approval-response")
                .build();

        mailService.sendMail(mail);
    }

    public void reportCustomProcess(Approval approval, Account user) {
        switch (approval.getType()) {
            case SOP_Training_Deviation_Report:
                    SOPDeviationReport sopDeviationReport = approval.getSopDeviationReport();
                    sopDeviationReport.setApproval(approval);

                    /**
                     * Training 기간을 위반한 경우
                     */
                    if (ObjectUtils.isEmpty(sopDeviationReport.getTrainingPeriodId()) == false) {
                        BooleanBuilder builder = new BooleanBuilder();
                        QTrainingLog qTrainingLog = QTrainingLog.trainingLog;
                        builder.and(qTrainingLog.user.id.eq(user.getId()));
                        builder.and(qTrainingLog.status.eq(TrainingStatus.PROGRESS).or(qTrainingLog.reportStatus.eq(DeviationReportStatus.REPORTED)));
                        builder.and(qTrainingLog.trainingPeriod.id.eq(sopDeviationReport.getTrainingPeriodId()));
                        Optional<TrainingLog> optionalTrainingLog = trainingLogService.findOne(builder);

                        TrainingLog trainingLog;
                        if(optionalTrainingLog.isPresent()) {
                            trainingLog = optionalTrainingLog.get();
                            log.info("@교육 진행중인 로그가 존재함. Username : {}, TrainingLogId : {}", user.getUsername(), trainingLog.getId());
                        } else {
                            trainingLog = new TrainingLog();
                        }
                        trainingLog.setDocumentVersion(sopDeviationReport.getDeviatedSOPDocument());
                        trainingLog.setUser(user);
                        trainingLog.setReportStatus(DeviationReportStatus.REPORTED);
                        trainingLog.setStatus(TrainingStatus.NOT_STARTED);
                        trainingLog.setType(TrainingType.SELF);
                        trainingLog.setTrainingPeriod(trainingPeriodService.findById(sopDeviationReport.getTrainingPeriodId()).get());

                        TrainingLog savedTrainingLog = trainingLogService.saveOrUpdate(trainingLog, null);

//                        approval.getSopDeviationReport().getTrainingLogId()
//                        approval.setTrainingLogId(savedTrainingLog.getId());
//                        approval.setTrainingPeriodId(sopDeviationReport.getTrainingPeriodId());
                        sopDeviationReport.setTrainingLogId(savedTrainingLog.getId());
                    }

                    sopDeviationReportRepository.save(sopDeviationReport);
                    break;
//            case SOP_RD_Request_Form:
//                SopRdRequestForm sopRdRequestForm = approval.getSopRdRequestForm();
//                sopRdRequestForm.setApproval(approval);
//
//                if (ObjectUtils.isEmpty(sopRdRequestForm.getId()) == false) {
//                    if (ObjectUtils.isEmpty(sopRdRequestForm.getSopDevelopmentDocs()) == false) {
//                        log.debug("sopRdRequestForm size : {}", sopRdRequestForm.getSopDevelopmentDocs().size());
//                        List<Integer> sopDevIds = sopRdRequestForm.getSopDevelopmentDocs().stream()
//                                .filter(s -> !StringUtils.isEmpty(s.getId()))
//                                .map(s -> s.getId()).collect(Collectors.toList());
//                        for (Integer id : sopDevIds) {
//                            log.debug("sopRdRequestForm deleteById : {}", id);
//                            sopRdDevelopmentDocRepository.deleteById(id);
//                        }
//                    }
//                    if (sopRdRequestForm.isNewSOPDevelopment() && ObjectUtils.isEmpty(sopRdRequestForm.getRdDevelopmentDocs()) == false) {
//                        List<Integer> rdDevIds = sopRdRequestForm.getRdDevelopmentDocs().stream()
//                                .filter(s -> !StringUtils.isEmpty(s.getId()))
//                                .map(s -> s.getId()).collect(Collectors.toList());
//                        for (Integer id : rdDevIds) {
//                            sopRdDevelopmentDocRepository.deleteById(id);
//                        }
//                    }
//                    if (sopRdRequestForm.isNewRDDevelopment() && ObjectUtils.isEmpty(sopRdRequestForm.getSopRevisionDocs()) == false) {
//                        List<Integer> sopRevIds = sopRdRequestForm.getSopRevisionDocs().stream().map(s -> s.getId()).collect(Collectors.toList());
//                        for (Integer id : sopRevIds) {
//                            sopRdRevisionDocRepository.deleteById(id);
//                        }
//                    }
//                    if (ObjectUtils.isEmpty(sopRdRequestForm.getRdRevisionDocs()) == false) {
//                        List<Integer> rdRevIds = sopRdRequestForm.getRdRevisionDocs().stream().map(s -> s.getId()).collect(Collectors.toList());
//                        for (Integer id : rdRevIds) {
//                            sopRdRevisionDocRepository.deleteById(id);
//                        }
//                    }
//                }
//
//                if (sopRdRequestForm.isNewSOPDevelopment()) {
//                    for (SopRdDevelopmentDoc sop : sopRdRequestForm.getSopDevelopmentDocs()) {
//                        sop.setSopRdRequestForm(sopRdRequestForm);
//                        sop.setDocumentType(DocumentType.SOP);
//
//                        sopRdDevelopmentDocRepository.save(sop);
//                    }
//                }
//                if (sopRdRequestForm.isNewSOPDevelopment()) {
//                    for (SopRdDevelopmentDoc rd : sopRdRequestForm.getRdDevelopmentDocs()) {
//                        rd.setSopRdRequestForm(sopRdRequestForm);
//                        rd.setDocumentType(DocumentType.RF);
//
//                        sopRdDevelopmentDocRepository.save(rd);
//                    }
//                }
//
//                if (sopRdRequestForm.isSopRevision()) {
//                    for (String sopId : sopRdRequestForm.getSopRevisionIds()) {
//                        SopRdRevisionDoc sopRdRevisionDoc = new SopRdRevisionDoc();
//                        sopRdRevisionDoc.setSopRdRequestForm(sopRdRequestForm);
//                        sopRdRevisionDoc.setDocumentType(DocumentType.SOP);
//                        sopRdRevisionDoc.setDocumentVersion(DocumentVersion.builder().id(sopId).build());
//
//                        sopRdRevisionDocRepository.save(sopRdRevisionDoc);
//                    }
//                }
//
//                if (sopRdRequestForm.isRdRevision()) {
//                    for (String rdId : sopRdRequestForm.getRdRevisionIds()) {
//                        SopRdRevisionDoc sopRdRevisionDoc = new SopRdRevisionDoc();
//                        sopRdRevisionDoc.setSopRdRequestForm(sopRdRequestForm);
//                        sopRdRevisionDoc.setDocumentType(DocumentType.RD);
//                        sopRdRevisionDoc.setDocumentVersion(DocumentVersion.builder().id(rdId).build());
//
//                        sopRdRevisionDocRepository.save(sopRdRevisionDoc);
//                    }
//                }
//
//                sopRdRequestFormRepository.save(sopRdRequestForm);
//                break;
//            case RD_Approval_Form:
//                RDApprovalForm rdApprovalForm = approval.getRdApprovalForm();
//                rdApprovalForm.setApproval(approval);
//
//                rdApprovalFormRepository.save(rdApprovalForm);
//                break;
//            case SOP_Waiver_Approval_Form:
//                log.debug("#approvalId : {}, SOP Waiver Approval Form Save or Update.", approval.getId());
//                SOPWaiverApprovalForm sopWaiverApprovalForm = approval.getSopWaiverApprovalForm();
//                sopWaiverApprovalForm.setApproval(approval);
//                sopWaiverApprovalFormRepository.save(sopWaiverApprovalForm);
//                break;
//
            case SOP_Disclosure_Request_Form:
                SOPDisclosureRequestForm sopDisclosureRequestForm = approval.getSopDisclosureRequestForm();
                sopDisclosureRequestForm.setApproval(approval);


                SOPDisclosureRequestForm savedSopDisclosureRequestForm = sopDisclosureRequestFormRepository.save(sopDisclosureRequestForm);
                List<String> sopList = ObjectUtils.isEmpty(sopDisclosureRequestForm.getSopIds())?null:Arrays.asList(sopDisclosureRequestForm.getSopIds());

                //RF가 없을경우 null삽입
                List<String> rfList = ObjectUtils.isEmpty(sopDisclosureRequestForm.getRfIds())?null:Arrays.asList(sopDisclosureRequestForm.getRfIds());


                //수정인 경우
                if(ObjectUtils.isEmpty(sopDisclosureRequestForm.getId()) == false) {
                    if(ObjectUtils.isEmpty(sopDisclosureRequestForm.getRequestedDocumentSOPs()) == false) {
                        for(RequestedDocument requestedDocument : sopDisclosureRequestForm.getRequestedDocumentSOPs()) {
                            if(!sopList.contains(requestedDocument.getDocumentVersion().getId())) {
                                requestedDocumentRepository.delete(requestedDocument);
                            }
                        }
                    }
                    if(ObjectUtils.isEmpty(sopDisclosureRequestForm.getRequestedDocumentRFs()) == false) {
                        if(ObjectUtils.isEmpty(sopDisclosureRequestForm.getRfIds())) {
                            requestedDocumentRepository.deleteAll(sopDisclosureRequestForm.getRequestedDocumentRFs());
                        } else {
//                            List<String> rfList = Arrays.asList(sopDisclosureRequestForm.getRfIds());
                            for(RequestedDocument requestedDocument : sopDisclosureRequestForm.getRequestedDocumentRFs()) {
                                if(!rfList.contains(requestedDocument.getDocumentVersion().getId())) {
                                    requestedDocumentRepository.delete(requestedDocument);
                                }
                            }
                        }
                    }

                    for(ExternalCustomer externalCustomer : sopDisclosureRequestForm.getExternalCustomers()) {
                        externalCustomer.setSopDisclosureRequestForm(savedSopDisclosureRequestForm);
                        externalCustomerRepository.save(externalCustomer);
                    }

                    if(!ObjectUtils.isEmpty(sopDisclosureRequestForm.getDisclosureSOPTrainingLog())) {
                        for(DisclosureSOPTrainingLog sop : sopDisclosureRequestForm.getDisclosureSOPTrainingLog()) {
                            disclosureSOPTrainingLogRepository.delete(sop);
                        }
                    }

                    if(!ObjectUtils.isEmpty(sopDisclosureRequestForm.getDisclosureISOTrainingLog())) {
                        for(DisclosureISOTrainingLog iso : sopDisclosureRequestForm.getDisclosureISOTrainingLog()) {
                            disclosureISOTrainingLogRepository.delete(iso);
                        }
                    }
                }

                List<String> savedSopIds = sopDisclosureRequestForm.getRequestedDocumentSOPs()
                        .stream()
                        .filter(r -> !ObjectUtils.isEmpty(r.getId()))
                        .map(r -> r.getDocumentVersion().getId())
                        .collect(Collectors.toList());
                List<String> savedRfIds = sopDisclosureRequestForm.getRequestedDocumentRFs()
                        .stream()
                        .filter(r -> !ObjectUtils.isEmpty(r.getId()))
                        .map(r -> r.getDocumentVersion().getId())
                        .collect(Collectors.toList());
                for(String sopId : sopDisclosureRequestForm.getSopIds()) {
                    if(savedSopIds.contains(sopId) == false) {
                        DocumentVersion documentVersion = DocumentVersion.builder().id(sopId).build();
                        RequestedDocument requestedDocument = new RequestedDocument();
                        requestedDocument.setSopDisclosureRequestForm(savedSopDisclosureRequestForm);
                        requestedDocument.setDocumentType(DocumentType.SOP);
                        requestedDocument.setDocumentVersion(documentVersion);

                        requestedDocumentRepository.save(requestedDocument);
                    }
                }
                if(ObjectUtils.isEmpty(sopDisclosureRequestForm.getRfIds()) == false) {
                    for(String rdId : sopDisclosureRequestForm.getRfIds()) {
                        if(savedRfIds.contains(rdId) == false) {
                            DocumentVersion documentVersion = DocumentVersion.builder().id(rdId).build();
                            RequestedDocument requestedDocument = new RequestedDocument();
                            requestedDocument.setSopDisclosureRequestForm(savedSopDisclosureRequestForm);
                            requestedDocument.setDocumentType(DocumentType.RF);
                            requestedDocument.setDocumentVersion(documentVersion);

                            requestedDocumentRepository.save(requestedDocument);
                        }
                    }
                }

                if(!ObjectUtils.isEmpty(sopDisclosureRequestForm.getSopUserIds())) {
                    for(String sopUserId : sopDisclosureRequestForm.getSopUserIds()) {
                        DisclosureSOPTrainingLog sop = new DisclosureSOPTrainingLog();
                        sop.setSopDisclosureRequestForm(savedSopDisclosureRequestForm);
                        sop.setUser(Account.builder().id(Integer.parseInt(sopUserId)).build());
                        disclosureSOPTrainingLogRepository.save(sop);
                    }
                }

                if(!ObjectUtils.isEmpty(sopDisclosureRequestForm.getIsoUserIds())) {
                    for(String isoUserId : sopDisclosureRequestForm.getIsoUserIds()) {
                        DisclosureISOTrainingLog iso = new DisclosureISOTrainingLog();
                        iso.setSopDisclosureRequestForm(savedSopDisclosureRequestForm);
                        iso.setUser(Account.builder().id(Integer.parseInt(isoUserId)).build());
                        disclosureISOTrainingLogRepository.save(iso);
                    }
                }

                break;
//
//            case SOP_RD_Retirement_Form:
//                RetirementApprovalForm retirementApprovalForm = approval.getRetirementApprovalForm();
//                retirementApprovalForm.setApproval(approval);
//
//                log.debug("@SOP/RD Retirement Form 정보 업데이트.");
//                RetirementApprovalForm savedRetirementApprovalForm = retirementApprovalFormRepository.save(retirementApprovalForm);
//                List<String> retirementSopList = Arrays.asList(retirementApprovalForm.getSopIds());
//                if (ObjectUtils.isEmpty(retirementApprovalForm.getId()) == false) {
//                    if (ObjectUtils.isEmpty(retirementApprovalForm.getRetirementDocumentSOPs()) == false) {
//                        for (RetirementDocument retirementDocument : retirementApprovalForm.getRetirementDocumentSOPs()) {
//                            if (!retirementSopList.contains(retirementDocument.getDocumentVersion().getId())) {
//                                retirementDocumentRepository.delete(retirementDocument);
//                            }
//                        }
//                    }
//                    if (ObjectUtils.isEmpty(retirementApprovalForm.getRetirementDocumentRDs()) == false) {
//                        if (ObjectUtils.isEmpty(retirementApprovalForm.getRdIds())) {
//                            retirementDocumentRepository.deleteAll(retirementApprovalForm.getRetirementDocumentRDs());
//                        } else {
//                            List<String> retRdList = Arrays.asList(retirementApprovalForm.getRdIds());
//                            for (RetirementDocument retirementDocument : retirementApprovalForm.getRetirementDocumentRDs()) {
//                                if (!retRdList.contains(retirementDocument.getDocumentVersion().getId())) {
//                                    retirementDocumentRepository.delete(retirementDocument);
//                                }
//                            }
//                        }
//                    }
//                }
//
//                List<String> retirementSavedSopIds = retirementApprovalForm.getRetirementDocumentSOPs().stream()
//                        .filter(r -> ObjectUtils.isEmpty(r.getId()) == false)
//                        .map(r -> r.getDocumentVersion().getId()).collect(Collectors.toList());
//                List<String> retirementSavedRdIds = retirementApprovalForm.getRetirementDocumentRDs().stream()
//                        .filter(r -> ObjectUtils.isEmpty(r.getId()) == false)
//                        .map(r -> r.getDocumentVersion().getId()).collect(Collectors.toList());
//                log.debug("==> SOP Retirement 저장된 정보 확인 : {}", retirementApprovalForm.getSopIds());
//                for(String sopId : retirementApprovalForm.getSopIds()) {
//                    log.debug("==> SOP Retirement 저장된 정보 확인 : {}", sopId);
//                    if(retirementSavedSopIds.contains(sopId) == false) {
//                        DocumentVersion documentVersion = DocumentVersion.builder().id(sopId).build();
//                        RetirementDocument retirementDocument = new RetirementDocument();
//                        retirementDocument.setRetirementApprovalForm(savedRetirementApprovalForm);
//                        retirementDocument.setDocumentType(DocumentType.SOP);
//                        retirementDocument.setDocumentVersion(documentVersion);
//
//                        retirementDocumentRepository.save(retirementDocument);
//                    }
//                }
//                log.debug("==> RD Retirement 저장된 정보 확인1 : {}", retirementApprovalForm.getRdIds());
//                if(ObjectUtils.isEmpty(retirementApprovalForm.getRdIds()) == false) {
//                    for(String rdId : retirementApprovalForm.getRdIds()) {
//                        log.debug("==> RD Retirement 저장된 정보 확인2 : {}", rdId);
//                        if(retirementSavedRdIds.contains(rdId) == false) {
//                            DocumentVersion documentVersion = DocumentVersion.builder().id(rdId).build();
//                            RetirementDocument retirementDocument = new RetirementDocument();
//                            retirementDocument.setRetirementApprovalForm(savedRetirementApprovalForm);
//                            retirementDocument.setDocumentType(DocumentType.RD);
//                            retirementDocument.setDocumentVersion(documentVersion);
//
//                            retirementDocumentRepository.save(retirementDocument);
//                        }
//                    }
//                }
//                break;
        }
    }

    public void removeAllLines(Approval approval) {
        QApprovalLine qApprovalLine = QApprovalLine.approvalLine;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qApprovalLine.approval.id.eq(approval.getId()));
        builder.and(qApprovalLine.lineType.ne(ApprovalLineType.requester));
        Iterable<ApprovalLine> approvalLines = approvalLineRepository.findAll(builder);
        approvalLineRepository.deleteAll(approvalLines);
    }

//    public void saveSopDeviationReport(Integer id, SOPDeviationReport sopDeviationReport, ApprovalStatus status, User user) throws Exception {
//        Approval approval;
//
//        if(ObjectUtils.isEmpty(id)) {
//            approval = Approval.builder()
//                    .type(ReportType.SOP_Deviation_Report)
//                    .status(status)
//                    .content(objectMapper.writeValueAsString(sopDeviationReport))
//                    .build();
//        } else {
//            approval = approvalRepository.findById(id).get();
//            approval.setStatus(status);
//            approval.setContent(objectMapper.writeValueAsString(sopDeviationReport));
//            /**
//             * 요청자를 제외하고 삭제한다.
//             */
//            List<Integer> ids = approval.getApprovalLines().stream().filter(a -> a.getLineType() != ApprovalLineType.requester).map(a -> a.getId()).collect(Collectors.toList());
//            log.debug("remove lines : {}", ids);
//            if(ObjectUtils.isEmpty(ids) == false) {
//                for (Integer lineId : ids) {
//                    approval.getApprovalLines().remove(ApprovalLine.builder().id(lineId).build());//기존 결재라인 삭제
//                }
//            }
//
//        }
//
//        sopDeviationReport.getApprovalLines().forEach(l -> l.setApproval(approval));
//        //첫번째 결재 유저
//        Optional<ApprovalLine> optionalApprovalLine = sopDeviationReport.getApprovalLines().stream().filter(l -> l.getLineType() != ApprovalLineType.requester).findFirst();
//        if(optionalApprovalLine.isPresent()) {
//            ApprovalLine approvalLine = optionalApprovalLine.get();
//            approvalLine.setStatus(ApprovalStatus.request);
//            log.info("** 첫번째 결재할 유저 확인 됨. {}", approvalLine.getDisplayName());
//        }
//
//        if(ObjectUtils.isEmpty(approval.getApprovalLines())) {
//            approval.setApprovalLines(sopDeviationReport.getApprovalLines());
//        } else {
//            approval.getApprovalLines().addAll(sopDeviationReport.getApprovalLines());
//        }
//
//        /**
//         * Training 기간을 위반한 경우
//         */
//        if(ObjectUtils.isEmpty(sopDeviationReport.getTrainingPeriodId()) == false) {
//            TrainingLog trainingLog = new TrainingLog();
//            trainingLog.setDocumentVersion(documentVersionService.findById(sopDeviationReport.getDeviatedSOPDocumentId()));
//            trainingLog.setEmpNo(user.getEmpNo());
//            trainingLog.setReportStatus(DeviationReportStatus.REPORTED);
//            trainingLog.setStatus(TrainingStatus.NOT_STARTED);
//            trainingLog.setTrainingPeriod(trainingPeriodService.findById(sopDeviationReport.getTrainingPeriodId()).get());
//
//            TrainingLog savedTrainingLog = trainingLogService.saveOrUpdate(trainingLog, null);
//
//            approval.setTrainingLogId(savedTrainingLog.getId());
//            approval.setTrainingPeriodId(sopDeviationReport.getTrainingPeriodId());
//        }
//
//        approvalRepository.save(approval);
//    }
//
//    public void saveSopRdReportForm(Integer id, SopRdRequestForm sopRdRequestForm, ApprovalStatus status, User user) throws Exception {
//        Approval approval;
//
//        if(ObjectUtils.isEmpty(id)) {
//            approval = Approval.builder()
//                    .type(ReportType.SOP_RD_Request_Form)
//                    .status(status)
//                    .content(objectMapper.writeValueAsString(sopRdRequestForm))
//                    .build();
//        } else {
//            approval = approvalRepository.findById(id).get();
//            approval.setStatus(status);
//            approval.setContent(objectMapper.writeValueAsString(sopRdRequestForm));
//            /**
//             * 요청자를 제외하고 삭제한다.
//             */
//            List<Integer> ids = approval.getApprovalLines().stream().filter(a -> a.getLineType() != ApprovalLineType.requester).map(a -> a.getId()).collect(Collectors.toList());
//            log.debug("remove lines : {}", ids);
//            if(ObjectUtils.isEmpty(ids) == false) {
//                for (Integer lineId : ids) {
//                    approval.getApprovalLines().remove(ApprovalLine.builder().id(lineId).build());//기존 결재라인 삭제
//                }
//            }
//
//        }
//
////        sopRdRequestForm.getApprovalLines().forEach(l -> l.setApproval(approval));
//        //첫번째 결재 유저
//        Optional<ApprovalLine> optionalApprovalLine = sopRdRequestForm.getApprovalLines().stream().filter(l -> l.getLineType() != ApprovalLineType.requester).findFirst();
//        if(optionalApprovalLine.isPresent()) {
//            ApprovalLine approvalLine = optionalApprovalLine.get();
//            approvalLine.setStatus(ApprovalStatus.request);
//            log.info("** 첫번째 결재할 유저 확인 됨. {}", approvalLine.getDisplayName());
//        }
//
//        log.info("@@@@ approvalId : {}", approval.getId());
//
//        if(approval.getApprovalLines() == null) {
//            log.info("==> #1 approval.approvalLines is null");
//            approval.setApprovalLines(new ArrayList<>());
//        }
////        } else {
//            log.info("==> #2 approval.approvalLines is not null : {}", approval.getApprovalLines().size());
//            for(ApprovalLine line : sopRdRequestForm.getApprovalLines()) {
//                line.setApproval(approval);
//                approval.getApprovalLines().add(line);
//                log.debug("==> add approval lin");
//            }
////        }
//
//
//        approvalRepository.save(approval);
//    }

    public void approvedOrRejected(Integer id, ApprovalLine line, Account user) {
        Optional<Approval> optionalApproval = findById(id);
        Approval approval = optionalApproval.get();

        Optional<ApprovalLine> optionalApprovalLine = approval.getApprovalLines().stream().filter(v ->
            v.getId().equals(line.getId())).findFirst();
        if(optionalApprovalLine.isPresent()) {
            ApprovalLine approvalLine = optionalApprovalLine.get();
            approvalLine.setStatus(line.getStatus());
            approvalLine.setComments(line.getComments());
            approvalLine.setBase64signature(signatureRepository.findById(user.getUsername()).get().getBase64signature());
        }
//        approvalLineRepository.save(approvalLine);

        if(!ObjectUtils.isEmpty(line.getNextId())) {
            log.debug("** 다음 결재가 존재함.");
            if(line.getStatus() == ApprovalStatus.approved) {
                Optional<ApprovalLine> optionalNextApprovalLine = approval.getApprovalLines().stream().filter(v -> v.getId().equals(line.getNextId())).findFirst();
                if (optionalNextApprovalLine.isPresent()) {
                    ApprovalLine nextLine = optionalNextApprovalLine.get();
                    nextLine.setStatus(ApprovalStatus.request);
                    log.debug("다음 결재 예정자 상태 업데이트");
//                    approvalLineRepository.save(nextLine);
                    Account fromUser = userService.findByUsername(approval.getApprovalLines().get(0).getUsername()).get();
                    Account receiveUser = userService.findByUsername(nextLine.getUsername()).get();

                    sendReviewApprovalEmail(receiveUser, fromUser, approval, approval.getApprovalLines().get(0));
//                    HashMap<String, Object> model = new HashMap<>();
//                    model.put("message", "전자결재 " +(approval.getApprovalLines().get(1).getLineType() == ApprovalLineType.reviewer ? "검토" : "승인") + " 요청이 있습니다.");
//                    Mail mail = Mail.builder()
//                            .to(new String[]{receiveUser.getEmail()})
//                            .subject("[전자결재/"+fromUser.getKorName()+"] " + approval.getType().getLabel())
//                            .model(model)
//                            .templateName("approval-alert")
//                            .build();
//
//                    mailService.sendMail(mail);
                }
                approval.setStatus(ApprovalStatus.progress);
            } else {
                log.debug("** 반려인 경우 다음 결재 상태를 업데이트 하지 않음.");
                approval.setStatus(line.getStatus());

                Account receiveUser = userService.findByUsername(approval.getUsername()).get();
//                HashMap<String, Object> model = new HashMap<>();
//                model.put("message", "전자결재 반려 처리 되었습니다.");
//                Mail mail = Mail.builder()
//                        .to(new String[]{receiveUser.getEmail()})
//                        .subject("[전자결재/반려] " + approval.getType().getLabel())
//                        .model(model)
//                        .templateName("approval-alert")
//                        .build();
//
//                mailService.sendMail(mail);

                sendApprovalResultEmail(receiveUser, approval, "반려");

                if(approval.getType() == ReportType.SOP_Training_Deviation_Report) {
                    trainingLogReportStatus(approval);
                }

            }
        } else {
            log.debug("** 현재 결재에서 종료.");
            approval.setStatus(line.getStatus());
            log.debug("approval  id : {}, status : {}", approval.getId(), approval.getStatus());

            if(approval.getType() == ReportType.SOP_Training_Deviation_Report) {
                trainingLogReportStatus(approval);
//            } else if(approval.getType() == ReportType.RD_Approval_Form && approval.getStatus() == ApprovalStatus.approved) {
//                addRDRevisionDocument(approval);
            } else if(approval.getType() == ReportType.SOP_RF_Retirement_Form && approval.getStatus() == ApprovalStatus.approved) {
                retirementDocument(approval);
            } else if(approval.getType() == ReportType.SOP_RF_Request_Form && approval.getStatus() == ApprovalStatus.approved) {
                sopRfRequestForm(approval);
            } else if(approval.getType() == ReportType.SOP_Disclosure_Request_Form) {
                sendEmailExternalCustomer(approval);
            }

            Account receiveUser = userService.findByUsername(approval.getApprovalLines().get(0).getUsername()).get();
//            HashMap<String, Object> model = new HashMap<>();
//            model.put("message", "전자결재 " +(approval.getStatus().getLabel()) + " 처리 되었습니다.");
//            Mail mail = Mail.builder()
//                    .to(new String[]{receiveUser.getEmail()})
//                    .subject("[전자결재/"+approval.getStatus().getLabel()+"] " + approval.getType().getLabel())
//                    .model(model)
//                    .templateName("approval-alert")
//                    .build();
//
//            mailService.sendMail(mail);
            sendApprovalResultEmail(receiveUser, approval, approval.getStatus().getLabel());
        }

        approvalRepository.save(approval);
    }

    private void sendEmailExternalCustomer(Approval approval) {
        SOPDisclosureRequestForm sopDisclosureRequestForm = approval.getSopDisclosureRequestForm();

        for(ExternalCustomer customer : sopDisclosureRequestForm.getExternalCustomers()) {
            HashMap<String, Object> model = new HashMap<>();
            model.put("form", sopDisclosureRequestForm);
            model.put("customer", customer);

            //2021-03-17 YSH :: 회사명 공통 작업
            model.put("siteCompanyTitle", siteCompanyTitle);

            Mail mail = new Mail();
            mail.setTo(new String[]{customer.getEmail()});
            mail.setSubject("[CAUH] ISO MS Invitation");
            mail.setModel(model);
            mail.setTemplateName("external-customer-template");

            mailService.sendMail(mail);
        }
    }

//    private void addRDRevisionDocument(Approval approval) {
//        RDApprovalForm rdApprovalForm = approval.getRdApprovalForm();
//        DocumentVersion supersededVersion = rdApprovalForm.getSupersededVersion();
//
//        DocumentVersion documentVersion = new DocumentVersion();
//        documentVersion.setDocument(supersededVersion.getDocument());
//        documentVersion.setParentVersion(supersededVersion);
//        documentVersion.setStatus(DocumentStatus.REVISION);
//        documentVersion.setVersion(rdApprovalForm.getVersion());
//        documentVersion.setEffectiveDate(rdApprovalForm.getEffectiveDate());
//
//        documentVersionService.saveDocumentVersion(documentVersion);
//    }

    private void retirementDocument(Approval approval) {
        log.info("@@Retirement Form 승인 처리 됨.");
        RetirementApprovalForm retirementApprovalForm = approval.getRetirementApprovalForm();
        for(RetirementDocument retirementDocument : retirementApprovalForm.getRetirementDocumentSOPs()) {
            DocumentVersion sopDocument = retirementDocument.getDocumentVersion();
            log.debug("SOP {} retirement.", sopDocument.getId());
//            documentVersionService.retirement(sopDocument);
        }

        for(RetirementDocument retirementDocument : retirementApprovalForm.getRetirementDocumentRFs()) {
            DocumentVersion rdDocument = retirementDocument.getDocumentVersion();
            log.debug("RD {} retirement.", rdDocument.getId());
//            documentVersionService.retirement(rdDocument);
        }
    }

    private void sopRfRequestForm(Approval approval) {
        SopRfRequestForm sopRfRequestForm = approval.getSopRfRequestForm();

        //신규 개발 SOP 등록
        Map<String, Document> newSOP = new HashMap<>();
        if(sopRfRequestForm.isNewSOPDevelopment()) {
            for(SopRfDevelopmentDoc sop : sopRfRequestForm.getSopDevelopmentDocs()) {
                Document document = new Document();
                document.setCategory(categoryService.findByShortName(sop.getCategoryId()).get());
                document.setDocId(sop.getDocId());
                document.setTitle(sop.getTitle());
                document.setDocumentNo(sop.getDocNo());
                document.setType(DocumentType.SOP);

                Document savedSop = documentRepository.save(document);
                DocumentVersion sopVer = new DocumentVersion();
                sopVer.setId(UUID.randomUUID().toString());
                sopVer.setDocument(savedSop);
                sopVer.setStatus(DocumentStatus.DEVELOPMENT);
                sopVer.setVersion(sop.getVersion());
                sopVer.setTrainingAll(true);
                documentVersionRepository.save(sopVer);
                log.debug("@신규 개발 SOP 정보 등록");

                newSOP.put(sop.getDocId(), savedSop);
            }
        }
        //신규 개발 RD 정보 등록
        if(sopRfRequestForm.isNewRFDevelopment()) {
            for(SopRfDevelopmentDoc rd : sopRfRequestForm.getRfDevelopmentDocs()) {
                Document document = new Document();
                Optional<Document> optionalDocument = documentService.findByDocId(rd.getSopId());
                if(optionalDocument.isPresent()) {
                    document.setSop(optionalDocument.get());
                } else {
                    if(sopRfRequestForm.isNewRFDevelopment()) {
                        if(newSOP.containsKey(rd.getSopId())) {
                            document.setSop(newSOP.get(rd.getSopId()));
                        }
                    }
                }


                document.setDocId(rd.getDocId());
                document.setTitle(rd.getTitle());
                document.setDocumentNo(rd.getDocNo());
                document.setType(DocumentType.RF);

                Document savedRd = documentRepository.save(document);
                DocumentVersion rfVer = new DocumentVersion();
                rfVer.setId(UUID.randomUUID().toString());
                rfVer.setDocument(savedRd);
                rfVer.setStatus(DocumentStatus.DEVELOPMENT);
                rfVer.setVersion(rd.getVersion());
                rfVer.setTrainingAll(false);
                documentVersionRepository.save(rfVer);
                log.debug("@신규 개발 RF 정보 등록");
            }
        }

        if(sopRfRequestForm.isSopRevision()) {
            List<SopRfRevisionDoc> sopRevisionDocs = sopRfRequestForm.getSopRevisionDocs();
            for(SopRfRevisionDoc revisionDoc : sopRevisionDocs) {
                DocumentVersion supersededVersion = revisionDoc.getDocumentVersion();

                DocumentVersion documentVersion = new DocumentVersion();
                documentVersion.setDocument(supersededVersion.getDocument());
                documentVersion.setParentVersion(supersededVersion);
                documentVersion.setStatus(DocumentStatus.REVISION);
                documentVersion.setId(UUID.randomUUID().toString());
//                documentVersion.setVersion(Double.toString(Double.parseDouble(supersededVersion.getVersion()) + 0.1));
                documentVersionRepository.save(documentVersion);
            }
        }

        if(sopRfRequestForm.isRfRevision()) {
            List<SopRfRevisionDoc> rdRevisionDocs = sopRfRequestForm.getRfRevisionDocs();
            for(SopRfRevisionDoc revisionDoc : rdRevisionDocs) {
                DocumentVersion supersededVersion = revisionDoc.getDocumentVersion();

                DocumentVersion documentVersion = new DocumentVersion();
                documentVersion.setDocument(supersededVersion.getDocument());
                documentVersion.setParentVersion(supersededVersion);
                documentVersion.setStatus(DocumentStatus.REVISION);
                documentVersion.setId(UUID.randomUUID().toString());
//                documentVersion.setVersion(Double.toString(Double.parseDouble(supersededVersion.getVersion()) + 0.1));

                documentVersionRepository.save(documentVersion);
            }
        }
    }

//    public Optional<Approval> findByTrainingPeriodId(Integer trainingPeriodId) {
//        QApproval qApproval = QApproval.approval;
//        BooleanBuilder builder = new BooleanBuilder();
////        builder.and(qApproval.training.eq(trainingPeriodId));
//
//        return approvalRepository.findOne(builder);
//    }

    private void trainingLogReportStatus(Approval approval) {
        if(approval.getType() == ReportType.SOP_Training_Deviation_Report) {
            if (ObjectUtils.isEmpty(approval.getSopDeviationReport().getTrainingLogId()) == false) {
                Optional<TrainingLog> optionalTrainingLog = trainingLogService.findById(approval.getSopDeviationReport().getTrainingLogId());
                if (optionalTrainingLog.isPresent()) {
                    TrainingLog trainingLog = optionalTrainingLog.get();
                    trainingLog.setReportStatus(approval.getStatus() == ApprovalStatus.rejected ? DeviationReportStatus.REJECTED : DeviationReportStatus.APPROVED);

                    trainingLogService.saveOrUpdate(trainingLog, null);
                }
            }
        }
    }

    public Page<Approval> findAllAdmin(ApprovalStatus status, ReportType reportType, Pageable pageable) {
//        BooleanBuilder builder = new BooleanBuilder();
//        QApproval qApproval = QApproval.approval;
//        if(!ObjectUtils.isEmpty(status)) {
//            builder.and(qApproval.status.eq(status));
//        }
//        builder.and(qApproval.deleted.eq(false));
//
//        return approvalRepository.findAll(builder, pageable);

        return approvalLineRepository.findAllAdmin(status, reportType, pageable);
    }


}
