package com.cauh.iso.controller;

import com.cauh.common.entity.Account;
import com.cauh.common.entity.QAccount;
import com.cauh.common.entity.constant.UserStatus;
import com.cauh.common.repository.UserRepository;
import com.cauh.common.security.annotation.CurrentUser;
import com.cauh.iso.domain.*;
import com.cauh.iso.domain.constant.*;
import com.cauh.iso.repository.TrainingMatrixRepository;
import com.cauh.iso.service.*;
import com.cauh.iso.validator.ISOOfflineTrainingValidator;
import com.cauh.iso.xdocreport.ISOTrainingCertificationService;
import com.cauh.iso.utils.DateUtils;
import com.cauh.iso.xdocreport.TrainingLogReportService;
import com.cauh.iso.xdocreport.dto.TrainingLogReport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
@RequiredArgsConstructor
@Slf4j
@SessionAttributes({"quiz", "isoMap", "userMap", "isoTrainingLogs", "isoOfflineTraining"})
public class ISOTrainingController {

    private final TrainingMatrixRepository trainingMatrixRepository;
    private final ISOTrainingLogService isoTrainingLogService;
    private final ISOTrainingPeriodService isoTrainingPeriodService;
    private final ISOTrainingCertificationService isoTrainingCertificationService;
    private final ISOAccessLogService isoAccessLogService;
    private final ISOService isoService;
    private final ISOOfflineTrainingService isoOfflineTrainingService;
    private final ISOOfflineTrainingAttendeeService isoOfflineTrainingAttendeeService;
    private final ISOOfflineTrainingValidator isoOfflineTrainingValidator;
    private final UserRepository userRepository;
    private final TrainingLogReportService trainingLogReportService;
    private final TrainingAccessLogService trainingAccessLogService;
    private final FileStorageService fileStorageService;

    @Value("${site.company-title}")
    private String siteCompanyTitle;

    @GetMapping("/training/iso/mytraining")
    public String myTraining(@PageableDefault(size = 25) Pageable pageable, @CurrentUser Account user, Model model) {

        Page<MyTraining> isoTrainingMatrices = trainingMatrixRepository.getISOMyTraining(pageable, user);
        model.addAttribute("trainingMatrix", isoTrainingMatrices);

        return "iso/training/trainingList";
    }

    @PostMapping("/training/iso/mytraining")
    @ResponseBody
    public Map<String, String> saveTrainingLog(@RequestParam("isoId") String isoId,
                                  @RequestParam("isoTrainingPeriodId") Integer isoTrainingPeriodId,
                                  @RequestParam(value = "isoTrainingLogId", required = false) Integer isoTrainingLogId,
                                  @RequestParam("trainingTime") Integer trainingTime,
                                  @RequestParam("progressPercent") double progressPercent,
                                  @RequestParam("lastPageNo") Integer lastPageNo,
                                  @CurrentUser Account user, RedirectAttributes attributes) throws Exception {
        Map<String, String> result = new HashMap<>();
        ISOTrainingLog isoTrainingLog;
        Optional<ISO> isoOptional = isoService.getISO(isoId);
        if(isoOptional.isEmpty()) {
            result.put("type", "danger");
            result.put("message", "ISO Training ?????? ?????? ?????? ??? ????????? ?????????????????????.");
            return result;
        }

        ISO iso = isoOptional.get();

        if(ObjectUtils.isEmpty(isoTrainingLogId)) {
            ISOTrainingPeriod isoTrainingPeriod = isoTrainingPeriodService.findById(isoTrainingPeriodId).get();
            isoTrainingLog = new ISOTrainingLog();
            isoTrainingLog.setIso(iso);
            isoTrainingLog.setUser(user);
//            isoTrainingLog.setIsoTrainingPeriod(isoTrainingPeriod);
            isoTrainingLog.setType(isoTrainingPeriod.getTrainingType());
        } else {
            isoTrainingLog = isoTrainingLogService.findById(isoTrainingLogId).get();
        }

        isoTrainingLog.setLastPageNo(lastPageNo);
        isoTrainingLog.setProgressPercent(progressPercent);
        isoTrainingLog.setTrainingTime(ObjectUtils.isEmpty(trainingTime)?0:trainingTime);
        isoTrainingLog.setStatus(progressPercent >= 100 ? (!StringUtils.isEmpty(iso.getQuiz()) ? TrainingStatus.TRAINING_COMPLETED : TrainingStatus.COMPLETED) : TrainingStatus.PROGRESS);
        isoTrainingLog.setCompleteDate(isoTrainingLog.getStatus()==TrainingStatus.COMPLETED?new Date():null);

        ISOTrainingLog savedTrainingLog = isoTrainingLogService.saveOrUpdate(isoTrainingLog, null);
        log.debug("=> SavedTrainingLog Id : {}", savedTrainingLog.getId());

        //???????????? ???????????? ????????? ??????????????? ??? -> ????????? ?????? ??????
        if(isoTrainingLog.getStatus() == TrainingStatus.COMPLETED) {
            ISOTrainingCertification certification = ISOTrainingCertification.builder()
                    .certNo(isoTrainingCertificationService.getCertNo(iso))
                    .iso(iso).user(user).isoTrainingLog(savedTrainingLog)
                    .isoTrainingCertificationInfo(isoTrainingCertificationService.getCurrentCertificateInfo())
                    .build();
            isoTrainingCertificationService.createCertificationFile(certification);
            result.put("message", "????????? ?????? ???????????????.");
        }else if(isoTrainingLog.getStatus() == TrainingStatus.TRAINING_COMPLETED) {
            result.put("message", "Training ??????! Test??? ?????? ??? ?????????.");
        } else {
            result.put("message", "?????? ?????? ?????? ????????? ???????????? ???????????????.");
        }

        return result;
    }

    @GetMapping("/training/iso/mytraining/completed")
    public String completedTraining() {
//        @PageableDefault(size = 15, sort = {"completeDate"}, direction = Sort.Direction.DESC) Pageable pageable,        @PathVariable("documentType") DocumentType documentType, @CurrentUser Account user, Model model

//        QTrainingLog qTrainingLog = QTrainingLog.trainingLog;
//        BooleanBuilder builder = new BooleanBuilder();
//        builder.and(qTrainingLog.user.id.eq(user.getId()));
//        builder.and(qTrainingLog.status.eq(TrainingStatus.COMPLETED));
//        builder.and(qTrainingLog.documentVersion.document.type.eq(documentType));
//
//        model.addAttribute("trainingLog", trainingLogService.findAll(builder, pageable));
        return "iso/training/completedTraining";
    }

    @GetMapping("/ajax/training/iso/mytraining/completed")
    @ResponseBody
    public List<ISOTrainingLogDTO> ajaxCompletedTraining(@CurrentUser Account user) {
        QISOTrainingLog qIsoTrainingLog = QISOTrainingLog.iSOTrainingLog;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qIsoTrainingLog.user.id.eq(user.getId()));
        builder.and(qIsoTrainingLog.status.eq(TrainingStatus.COMPLETED));

        Iterable<ISOTrainingLog> isoTrainingLogs = isoTrainingLogService.findAll(builder, qIsoTrainingLog.completeDate.desc());
        List<ISOTrainingLog> trainingLogList = StreamSupport.stream(isoTrainingLogs.spliterator(), false)
                .collect(Collectors.toList());

        int size = trainingLogList.size();
        AtomicInteger atomicInteger = new AtomicInteger();
        atomicInteger.set(size);
        List<ISOTrainingLogDTO> isoTrainingLogDTOS = new ArrayList<>();
        isoTrainingLogs.forEach(log -> {
            ISOTrainingLogDTO dto = new ISOTrainingLogDTO();
            dto.setIndex(atomicInteger.getAndDecrement());
            dto.setId(log.getId());
            dto.setCompletionDate(DateUtils.format(log.getCompleteDate(), "dd-MMM-yyyy").toUpperCase());
            dto.setCourse(log.getTrainingCourse());
            dto.setHour(log.getHour());
            dto.setOrganization(log.getOrganization());

            //ISO?????? ???????????? ????????? ??????,
            if(log.getType() == TrainingType.SELF) {
                ISOTrainingCertification certification = isoTrainingCertificationService.findByIsoAndUser(log.getIso(), user);
                dto.setCertId(certification == null ? null : certification.getId().toString());
            }

            isoTrainingLogDTOS.add(dto);
        });

        return isoTrainingLogDTOS;
    }

    //????????? html .return
    @PutMapping(value = "/ajax/training/iso/certification/{isoCertId}", produces = "application/text;charset=utf8")
    @ResponseBody
    public String ajaxCompletedTraining(@PathVariable("isoCertId") String isoCertId) {

        if(isoCertId.equals("null") || isoCertId.equals("undefined")){
            return "<h2 style=''>????????? ????????? ???????????? ????????????</h2>";
        } else {
            ISOTrainingCertification isoTrainingCertification = isoTrainingCertificationService.findById(Integer.parseInt(isoCertId));
            return isoTrainingCertification.getCertHtml();
        }
    }

    @PostMapping("/training/iso/mytraining/completed/downloadCertFile")
    public ResponseEntity<Resource> downloadCertification(@RequestParam("isoCertId") Integer isoCertId, @CurrentUser Account user, HttpServletRequest request) throws Exception {
        ISOTrainingCertification isoTrainingCertification = isoTrainingCertificationService.findById(isoCertId);

        //AccessLog ??????
        isoAccessLogService.save(isoTrainingCertification, DocumentAccessType.DOWNLOAD);

        Resource resource = fileStorageService.loadFileAsResource("cert/" + isoTrainingCertification.getFileName());
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            log.info("Could not determine file type.");
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + isoTrainingCertification.getCertNo() + ".pdf\"")
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + URLEncoder.encode(user.getName() +  "_Cert_" + isoTrainingCertification.getId(), "utf-8") + ".pdf\"")
                .body(resource);
    }

    /**
     * Training Test ??????
     * @param isoId
     * @param model
     * @return
     * @throws Exception
     */
    @GetMapping("/ajax/training/iso/mytraining/test")
    public String test(@RequestParam("isoId") String isoId, Model model) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Quiz quiz = objectMapper.readValue(isoService.getISO(isoId).get().getQuiz(), Quiz.class);
        Collections.shuffle(quiz.getQuizQuestions());
        for(QuizQuestion quizQuestion : quiz.getQuizQuestions()) {
            quizQuestion.getAnswers().forEach(a -> a.setCorrect(false));//????????? ??? ?????????
            Collections.shuffle(quizQuestion.getAnswers());
        }

        model.addAttribute("quiz", quiz);

        return "iso/training/test";
    }

    @PostMapping("/training/iso/mytraining/test")
    @Transactional
    public String test(@RequestParam("isoTrainingLogId") Integer isoTrainingLogId,
                       @CurrentUser Account user, @ModelAttribute("quiz") Quiz quiz,
                       SessionStatus status, RedirectAttributes attributes) throws Exception{

        double correctCount = 0;
        double questionCount = quiz.getQuizQuestions().size();
        log.info("@User : {} Quiz ?????? ?????? ??????", user.getUsername());

        for(QuizQuestion q : quiz.getQuizQuestions()) {
            List<Integer> correct = Arrays.asList(q.getCorrect());
            List<Integer> choices = q.getAnswers().stream().filter(a -> a.isCorrect()).map(a -> a.getIndex()).collect(Collectors.toList());
            q.setChoices(choices.toArray(new Integer[choices.size()]));
            boolean match = (correct.size() == choices.size()) && correct.containsAll(choices);
            log.debug("Q.{} ??????:{}, ??????:{}, ?????? ?????? : {}", q.getIndex(), correct, choices, match);
            if(match) {
                correctCount ++;
            }
        }
        log.debug("?????? ??? : {}", correctCount);
        ISOTrainingLog trainingLog = isoTrainingLogService.findById(isoTrainingLogId).get();

        double score = 0;
        double curtLineCount = trainingLog.getIso().getCorrectCount();
        double curLineScore = ((curtLineCount/questionCount) * 100);

        if(correctCount > 0) {
            score = ((correctCount / questionCount) * 100);
            log.debug("?????? : {}", score);
//            log.debug("?????? : {}, ?????????:{} / ?????? ??????:{} * 100 = {}", score, correctCount, questionCount, (correctCount / questionCount) * 100);
        }

        log.debug("=> ?????? ??? : {}, ?????? : {}", correctCount, score);


        trainingLog.setScore((int)score);

        if(correctCount >= curtLineCount) {
            trainingLog.setStatus(TrainingStatus.COMPLETED);
            ISOTrainingCertification certification = ISOTrainingCertification.builder()
                    .certNo(isoTrainingCertificationService.getCertNo(trainingLog.getIso()))
                    .isoTrainingLog(trainingLog)
                    .iso(trainingLog.getIso()).user(user)
                    .isoTrainingCertificationInfo(isoTrainingCertificationService.getCurrentCertificateInfo())
                    .build();

                log.debug("@Certification ?????? : {}", certification.getCertNo());
                isoTrainingCertificationService.createCertificationFile(certification);

            trainingLog.setCompleteDate(new Date());
            attributes.addFlashAttribute("message", trainingLog.getScore() + "???("+(int)questionCount+"????????? ?????? "+(int)correctCount+"???)?????? ?????? ?????? ???????????????.");
        } else {
            trainingLog.setStatus(TrainingStatus.TEST_FAILED);
            attributes.addFlashAttribute("messageType", "danger");
            attributes.addFlashAttribute("message", "????????? [" + trainingLog.getScore() + "]?????????.("+(int)questionCount+"????????? ?????? "+(int)correctCount+"???) " + ((int)curLineScore) + "????????? ?????? ?????? ?????????.");
        }

        isoTrainingLogService.saveOrUpdate(trainingLog, quiz);
        status.setComplete();

        return "redirect:/training/iso/mytraining";
    }

    //Offline Training ?????? ??????
    @GetMapping("/training/iso/offline-training")
    public String offlineTraining(@PageableDefault(size = 25, sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable,
                                  @CurrentUser Account user, Model model) {

        QISOOfflineTrainingAttendee qisoOfflineTrainingAttendee = QISOOfflineTrainingAttendee.iSOOfflineTrainingAttendee;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qisoOfflineTrainingAttendee.account.id.in(user.getId()));

        model.addAttribute("isoOfflineTraining", isoOfflineTrainingAttendeeService.findAll(builder, pageable));
        return "iso/training/offline/list";
    }

    /**
     * ISO Offline Training ??????
     * @param model
     * @return
     */
    @GetMapping("/training/iso/offline-training/request")
    public String isoOfflineTrainingRequest(Model model) {
        model.addAttribute("isoOfflineTraining", new ISOOfflineTraining());

        QISO qiso = QISO.iSO;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qiso.training.eq(true).and(qiso.active.eq(true)));
        Iterable<ISO> isos = isoService.findAll(builder);

        model.addAttribute("isoMap", StreamSupport.stream(isos.spliterator(), false)
                .collect(Collectors.toMap(s -> s.getId(), s -> "[" + s.getIsoType().getLabel() + "] " + s.getTitle())));


        QAccount qUser = QAccount.account;
        BooleanBuilder userBuilder = new BooleanBuilder();
//        userBuilder.and(qUser.empNo.isNotNull());
        userBuilder.and(qUser.training.eq(true));
        userBuilder.and(qUser.enabled.eq(true));
        userBuilder.and(qUser.userStatus.eq(UserStatus.ACTIVE));

        Iterable<Account> users = userRepository.findAll(userBuilder, qUser.name.asc());

        model.addAttribute("userMap", StreamSupport.stream(users.spliterator(), false)
                .collect(Collectors.toMap(s -> Integer.toString(s.getId()), s -> s.getName())));

        return "iso/training/offline/request";
    }

    /**
     * Training ?????? ??????/?????? ??? ??????
     * @param isoOfflineTraining
     * @param selectedId
     * @param deselectedId
     * @return
     */
    @PutMapping("/training/iso/offline-training/request")
    public String offlineTrainingRequest(@ModelAttribute("isoOfflineTraining") ISOOfflineTraining isoOfflineTraining,
                                         @RequestParam(value = "selectedId", required = false) String selectedId,
                                         @RequestParam(value = "deselectedId", required = false) String deselectedId
    ) {
        if(StringUtils.isEmpty(selectedId) == false) {
            ISOOfflineTrainingDocument offlineTrainingDocument = new ISOOfflineTrainingDocument();
            offlineTrainingDocument.setIso(isoService.getISO(selectedId).get());
            if(isoOfflineTraining.getIsoOfflineTrainingDocuments().contains(offlineTrainingDocument) == false) {
                isoOfflineTraining.getIsoOfflineTrainingDocuments().add(offlineTrainingDocument);
            }
        } else if(StringUtils.isEmpty(deselectedId) == false) {
            isoOfflineTraining.getIsoOfflineTrainingDocuments().removeAll(isoOfflineTraining.getIsoOfflineTrainingDocuments().stream().filter(s -> s.getIso().getId().equals(deselectedId)).collect(Collectors.toList()));
        }

        if(ObjectUtils.isEmpty(isoOfflineTraining.getIsoOfflineTrainingDocuments())) {
            isoOfflineTraining.setIsoIds(null);
        }

        return "iso/training/offline/request";
    }

    /**
     * ???????????? ?????? ?????? (ISO)
     * @param isoOfflineTraining
     * @param result
     * @param status
     * @param attributes
     * @param user
     * @return
     */
    @PostMapping("/training/iso/offline-training/request")
    public String offlineTrainingRequest(@ModelAttribute("isoOfflineTraining") ISOOfflineTraining isoOfflineTraining, BindingResult result,
                                         SessionStatus status, RedirectAttributes attributes, @CurrentUser Account user) {
        isoOfflineTrainingValidator.validate(isoOfflineTraining, result);

        if(result.hasErrors()) {
            return "iso/training/offline/request";
        }

        isoOfflineTraining.setStatus(OfflineTrainingStatus.SUBMITTED);
        isoOfflineTraining.setEmpNo(user.getEmpNo());
        isoOfflineTrainingService.save(isoOfflineTraining);
        isoOfflineTrainingService.sendSubmittedEmail(user, isoOfflineTraining);
        status.setComplete();

        attributes.addFlashAttribute("message", "???????????? ?????? ????????? ?????? ???????????????.");
        return "redirect:/training/iso/offline-training";
    }

    @GetMapping("/training/iso/offline-training/{id}")
    public String offlineTrainingRequest(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("isoOfflineTraining", isoOfflineTrainingService.findById(id).get());

        return "iso/training/offline/view";
    }

    /**
     * Training Log ?????? ??????
     * @param pageable
     * @param user
     * @param model
     * @return
     */
    @GetMapping("/training/iso/trainingLog")
    public String uploadTrainingLog(@PageableDefault(size = 15, sort = {"completeDate"}, direction = Sort.Direction.DESC) Pageable pageable,
                                    @CurrentUser Account user, Model model) {

        QISOTrainingLog qIsoTrainingLog = QISOTrainingLog.iSOTrainingLog;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qIsoTrainingLog.user.id.eq(user.getId()));
        builder.and(qIsoTrainingLog.status.eq(TrainingStatus.COMPLETED));

        model.addAttribute("isoTrainingLog", isoTrainingLogService.findAll(builder, pageable));

        //2021-03-17. ????????? ????????? ??????
        model.addAttribute("siteCompanyTitle", siteCompanyTitle);

        return "iso/training/trainingLog";

    }

    @PostMapping("/training/iso/trainingLog/export")
    @Transactional
    public void exportTrainingLog(@CurrentUser Account user, HttpServletResponse response) throws Exception {

        QISOTrainingLog qIsoTrainingLog = QISOTrainingLog.iSOTrainingLog;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qIsoTrainingLog.user.id.eq(user.getId()));
        builder.and(qIsoTrainingLog.status.eq(TrainingStatus.COMPLETED));
        Iterable<ISOTrainingLog> iterable = isoTrainingLogService.findAll(builder, qIsoTrainingLog.completeDate.desc());
        List<TrainingLogReport> trainingLogs = StreamSupport.stream(iterable.spliterator(), false)
                .map(t -> TrainingLogReport.builder()
                        .completeDate(DateUtils.format(t.getCompleteDate(), "dd-MMM-yyyy").toUpperCase())
                        .course(t.getTrainingCourse())
                        .hr(t.getHour())
                        .organization(t.getOrganization())
                        .build()).collect(Collectors.toList());

        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ISO_Training_Log_" + new Date(System.currentTimeMillis()) + ".pdf");
        response.setContentType("application/pdf");

        log.debug("@Training Logs : {}", trainingLogs);
        log.debug("@User : {}", user);

        if(trainingLogReportService.isoGenerateReport(trainingLogs, user, response.getOutputStream())){
            trainingAccessLogService.save(user, TrainingLogType.ISO_TRAINING_LOG, DocumentAccessType.DOWNLOAD);
        }
    }

}
