package com.cauh.iso.controller;

import com.cauh.common.entity.Department;
import com.cauh.common.entity.QAccount;
import com.cauh.common.entity.Account;
import com.cauh.common.entity.TrainingRecord;
import com.cauh.common.entity.constant.TrainingRecordStatus;
import com.cauh.common.entity.constant.UserStatus;
import com.cauh.common.mapper.DeptUserMapper;
import com.cauh.common.repository.TrainingRecordRepository;
import com.cauh.common.repository.UserRepository;
import com.cauh.common.security.annotation.CurrentUser;
import com.cauh.iso.admin.service.DepartmentService;
import com.cauh.iso.domain.*;
import com.cauh.iso.domain.constant.*;
import com.cauh.iso.repository.TrainingMatrixRepository;
import com.cauh.iso.repository.TrainingTestLogRepository;
import com.cauh.iso.service.*;
import com.cauh.iso.utils.DateUtils;
import com.cauh.iso.validator.OfflineTrainingValidator;
import com.cauh.iso.xdocreport.IndexReportService;
import com.cauh.iso.xdocreport.TrainingLogReportService;
import com.cauh.iso.xdocreport.dto.TrainingLogReport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
@RequiredArgsConstructor
@Slf4j
@SessionAttributes({"quiz", "sopMap", "userMap", "offlineTraining", "trainingLogs"})
public class TrainingController {
    private final DocumentVersionService documentVersionService;
    private final TrainingMatrixRepository trainingMatrixRepository;
    private final TrainingPeriodService trainingPeriodService;
    private final TrainingLogService trainingLogService;
    private final TrainingLogReportService trainingLogReportService;
    private final TrainingTestLogRepository trainingTestLogRepository;
    private final UserRepository userRepository;
    private final OfflineTrainingValidator offlineTrainingValidator;
    private final OfflineTrainingService offlineTrainingService;
    private final OfflineTrainingAttendeeService offlineTrainingAttendeeService;
    private final DepartmentService departmentService;
    private final DocumentService documentService;
    private final TrainingService trainingService;
    private final TrainingAccessLogService trainingAccessLogService;

    @Value("${site.company-title}")
    private String siteCompanyTitle;

    @Value("${site.code}")
    private String siteCode;

//    @Value("${gw.userTbl}")
//    private String gwUserTbl;
//
//    @Value("${gw.deptTbl}")
//    private String gwDeptTbl;

//    private final SOPTrainingMatrixRepositoryImpl sopTrainingMatrixRepositoryImpl;

    @GetMapping("/training/sop/my-training-matrix")
    public String myTrainingMatrix(@PageableDefault(size = 300) Pageable pageable, @CurrentUser Account user, Model model, RedirectAttributes attributes) {
//        if(ObjectUtils.isEmpty(user.getJobDescriptions())) {
//            attributes.addFlashAttribute("message", "JD가 지정 되어 있지 않습니다. 관리자에게 문의 하세요.");
//            return "redirect:/notice";
//        }

        Page<MyTrainingMatrix> sopTrainingMatrices = trainingMatrixRepository.getMyTrainingMatrix(pageable, user.getUserJobDescriptions());
        model.addAttribute("trainingMatrix", sopTrainingMatrices);
        model.addAttribute("userJobDescriptions", user.getUserJobDescriptions());

        return "training/myTrainingMatrix";
    }



    @GetMapping("/training/sop/{requirement}-training")
    public String training(@PathVariable("requirement") TrainingRequirement requirement, @PageableDefault(size = 25) Pageable pageable, @CurrentUser Account user, Model model) {
        Page<MyTraining> sopTrainingMatrices = trainingMatrixRepository.getMyTraining(requirement, pageable, user);
        model.addAttribute("trainingMatrix", sopTrainingMatrices);
        model.addAttribute("requirement", requirement);

        return "training/trainingList";
    }

    @PostMapping("/training/sop/{requirement}-training")
    public String saveTrainingLog(@PathVariable("requirement") TrainingRequirement requirement,
                                  @RequestParam("docVersionId") String docVersionId,
                                @RequestParam("progressPercent") double progressPercent,
                                @RequestParam("trainingPeriodId") Integer trainingPeriodId,
                                @RequestParam("trainingTime") Integer trainingTime,
                                @RequestParam("lastPageNo") Integer lastPageNo,
                                @RequestParam(value = "trainingLogId", required = false) Integer trainingLogId,
                                @CurrentUser Account user,
                                RedirectAttributes attributes) {
        TrainingLog trainingLog;
        DocumentVersion documentVersion = documentVersionService.findById(docVersionId);
        if(ObjectUtils.isEmpty(trainingLogId)) {
            TrainingPeriod trainingPeriod = trainingPeriodService.findById(trainingPeriodId).get();
            trainingLog = new TrainingLog();
            trainingLog.setDocumentVersion(documentVersion);
            trainingLog.setUser(user);
            trainingLog.setReportStatus(DeviationReportStatus.NA);
            trainingLog.setTrainingPeriod(trainingPeriod);
            trainingLog.setType(trainingPeriod.getTrainingType());
        } else {
            trainingLog = trainingLogService.findById(trainingLogId).get();
        }

        trainingLog.setLastPageNo(lastPageNo);
        trainingLog.setProgressPercent(progressPercent);
        trainingLog.setTrainingTime(trainingTime);
//        trainingLog.setType(TrainingType.SELF);
        trainingLog.setStatus(progressPercent >= 100 ? (!StringUtils.isEmpty(documentVersion.getQuiz()) ? TrainingStatus.TRAINING_COMPLETED : TrainingStatus.COMPLETED) : TrainingStatus.PROGRESS);
        trainingLog.setCompleteDate(trainingLog.getStatus()==TrainingStatus.COMPLETED?new Date():null);

        TrainingLog savedTrainingLog = trainingLogService.saveOrUpdate(trainingLog, null);
        log.debug("=> SavedTrainingLog Id : {}", savedTrainingLog.getId());

        if(trainingLog.getStatus() == TrainingStatus.TRAINING_COMPLETED) {
            attributes.addFlashAttribute("message", "Training 완료! Test를 진행 해 주세요.");
            attributes.addFlashAttribute("trainingLogId", savedTrainingLog.getId());
        } else {
            attributes.addFlashAttribute("message", "진행 중인 교육 정보가 업데이트 되었습니다.");
        }
        return "redirect:/training/sop/{requirement}-training";
    }

    @GetMapping("/ajax/training/sop/{requirement}-training/test")
    public String test(@PathVariable("requirement") TrainingRequirement requirement, @RequestParam("docVerId") String docVerId, Model model) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Quiz quiz = objectMapper.readValue(documentVersionService.findById(docVerId).getQuiz(), Quiz.class);
        Collections.shuffle(quiz.getQuizQuestions());
        for(QuizQuestion quizQuestion : quiz.getQuizQuestions()) {
            quizQuestion.getAnswers().forEach(a -> a.setCorrect(false));//선택된 답 초기화
            Collections.shuffle(quizQuestion.getAnswers());
        }

        model.addAttribute("quiz", quiz);

        return "training/test";
    }



    @PostMapping("/training/sop/{requirement}-training/test")
    @Transactional
    public String test(@PathVariable("requirement") TrainingRequirement requirement, @RequestParam("trainingLogId") Integer trainingLogId, @CurrentUser Account user, @ModelAttribute("quiz") Quiz quiz, SessionStatus status, RedirectAttributes attributes) {

        double correctCount = 0;
        double questionCount = quiz.getQuizQuestions().size();
        log.info("@User : {} Quiz 정답 체크 시작", user.getUsername());
        for(QuizQuestion q : quiz.getQuizQuestions()) {
            List<Integer> correct = Arrays.asList(q.getCorrect());
            List<Integer> choices = q.getAnswers().stream().filter(a -> a.isCorrect()).map(a -> a.getIndex()).collect(Collectors.toList());
            q.setChoices(choices.toArray(new Integer[choices.size()]));
            boolean match = (correct.size() == choices.size()) && correct.containsAll(choices);
            log.debug("Q.{} 정답:{}, 선택:{}, 일치 여부 : {}", q.getIndex(), correct, choices, match);
            if(match) {
                correctCount ++;
            }
        }
        log.debug("정답 수 : {}", correctCount);
        TrainingLog trainingLog = trainingLogService.findById(trainingLogId).get();

        double score = 0;
        if(correctCount > 0) {
            score = ((correctCount / questionCount) * 100);
            log.debug("점수 : {}", score);
//            log.debug("점수 : {}, 정답수:{} / 전체 문항:{} * 100 = {}", score, correctCount, questionCount, (correctCount / questionCount) * 100);
        }

        log.debug("=> 정답 수 : {}, 점수 : {}", correctCount, score);
        trainingLog.setScore((int)score);
        if(correctCount >= 4) {
            trainingLog.setStatus(TrainingStatus.COMPLETED);
            trainingLog.setCompleteDate(new Date());
            attributes.addFlashAttribute("message", trainingLog.getScore() + "점("+(int)questionCount+"문제중 정답 "+(int)correctCount+"개)으로 교육 완료 되었습니다.");
        } else {
            trainingLog.setStatus(TrainingStatus.TEST_FAILED);
            attributes.addFlashAttribute("message", "점수는 [" + trainingLog.getScore() + "]입니다.("+(int)questionCount+"문제중 정답 "+(int)correctCount+"개) 80점이상 이수 가능 합니다.");
        }

        trainingLogService.saveOrUpdate(trainingLog, quiz);
        status.setComplete();

        return "redirect:/training/sop/{requirement}-training";
    }

    @GetMapping("/ajax/training/sop/{requirement}-training/{trainingTestLogId}/testLog")
    public String test(@PathVariable("requirement") TrainingRequirement requirement, @PathVariable("trainingTestLogId") Integer trainingTestLogId, Model model) throws Exception {

        QTrainingTestLog qTrainingTestLog = QTrainingTestLog.trainingTestLog;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qTrainingTestLog.trainingLog.id.eq(trainingTestLogId));

        Iterable<TrainingTestLog> trainingTestLogs = trainingTestLogRepository.findAll(builder);
        model.addAttribute("trainingTestLogs", trainingTestLogs);
        return "training/testLog";
    }

    @GetMapping("/training/sop/mandatory-training/completed")
    public String completedTraining() {
//        @PageableDefault(size = 15, sort = {"completeDate"}, direction = Sort.Direction.DESC) Pageable pageable,        @PathVariable("documentType") DocumentType documentType, @CurrentUser Account user, Model model

//        QTrainingLog qTrainingLog = QTrainingLog.trainingLog;
//        BooleanBuilder builder = new BooleanBuilder();
//        builder.and(qTrainingLog.user.id.eq(user.getId()));
//        builder.and(qTrainingLog.status.eq(TrainingStatus.COMPLETED));
//        builder.and(qTrainingLog.documentVersion.document.type.eq(documentType));
//
//        model.addAttribute("trainingLog", trainingLogService.findAll(builder, pageable));
        return "training/completedTraining";
    }

    @GetMapping("/ajax/training/sop/mandatory-training/completed")
    @ResponseBody
    public List<TrainingLogDTO> ajaxCompletedTraining(@CurrentUser Account user) {
        QTrainingLog qTrainingLog = QTrainingLog.trainingLog;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qTrainingLog.user.id.eq(user.getId()));
        builder.and(qTrainingLog.status.eq(TrainingStatus.COMPLETED));

        Iterable<TrainingLog> trainingLogs = trainingLogService.findAll(builder, qTrainingLog.completeDate.desc());
        List<TrainingLog> trainingLogList = StreamSupport.stream(trainingLogs.spliterator(), false)
                .collect(Collectors.toList());
        int size = trainingLogList.size();
        AtomicInteger atomicInteger = new AtomicInteger();
        atomicInteger.set(size);
        List<TrainingLogDTO> trainingLogDTOS = new ArrayList<>();
        trainingLogs.forEach(log -> {
            TrainingLogDTO dto = new TrainingLogDTO();
            dto.setIndex(atomicInteger.getAndDecrement());
            dto.setId(log.getId());
            dto.setCompletionDate(DateUtils.format(log.getCompleteDate(), "dd-MMM-yyyy"));
            dto.setCourse(log.getTrainingCourse());
            dto.setHour(log.getHour());
            dto.setOrganization(log.getOrganization());

            trainingLogDTOS.add(dto);
        });

        return trainingLogDTOS;
    }

    /**
     * 재교육(Re-training) 신청
     * @return
     */
    @PostMapping("/training/sop/mandatory-training/completed")
    public String requestReTraining(@RequestParam("id") Integer id, @CurrentUser Account user, RedirectAttributes attributes) {
        boolean result = trainingService.requestReTraining(id, user);

        if(result) {
            attributes.addFlashAttribute("message", "Re-Training 신청이 완료 되었습니다.");
            return "redirect:/training/sop/mandatory-training";
        } else {
            attributes.addFlashAttribute("message", "요청한 정보가 존재하지 않습니다.");
            return "redirect:/training/sop/mandatory-training/completed";
        }
    }

    @GetMapping("/training/sop/offline-training")
    public String offlineTraining(@PageableDefault(size = 25, sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable,
                                  @CurrentUser Account user, Model model) {

        QOfflineTrainingAttendee qOfflineTrainingAttendee = QOfflineTrainingAttendee.offlineTrainingAttendee;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qOfflineTrainingAttendee.account.id.in(user.getId()));

        model.addAttribute("offlineTraining", offlineTrainingAttendeeService.findAll(builder, pageable));
        return "training/offline/list";
    }

    @GetMapping("/training/sop/offline-training/request")
    public String offlineTrainingRequest(Model model) {
        model.addAttribute("offlineTraining", new OfflineTraining());

        QDocumentVersion qDocumentVersion = QDocumentVersion.documentVersion;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qDocumentVersion.document.type.eq(DocumentType.SOP).and(qDocumentVersion.status.in(DocumentStatus.EFFECTIVE, DocumentStatus.APPROVED)));
        Iterable<DocumentVersion> documentVersions = documentVersionService.findAll(builder);
        model.addAttribute("sopMap", StreamSupport.stream(documentVersions.spliterator(), false)
                .collect(Collectors.toMap(s -> s.getId(), s -> s.getDocument().getDocId() + " " + s.getDocument().getTitle() + " v" + s.getVersion())));

        QAccount qUser = QAccount.account;
        BooleanBuilder userBuilder = new BooleanBuilder();

        //TODO 2021-01-28 :: 사번 사용여부 확인 필요
        //userBuilder.and(qUser.empNo.isNotNull());
        userBuilder.and(qUser.training.eq(true));
        userBuilder.and(qUser.enabled.eq(true));
        userBuilder.and(qUser.userStatus.eq(UserStatus.ACTIVE));


        Iterable<Account> users = userRepository.findAll(userBuilder, qUser.name.asc());
        model.addAttribute("userMap", StreamSupport.stream(users.spliterator(), false)
//                .filter(u -> u.getId() != user.getId())
                .collect(Collectors.toMap(s -> Integer.toString(s.getId()), s -> s.getName())));
        return "training/offline/request";
    }

    @GetMapping("/training/sop/offline-training/{id}")
    public String offlineTrainingRequest(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("offlineTraining", offlineTrainingService.findById(id).get());

        return "training/offline/view";
    }

    @PutMapping("/training/sop/offline-training/request")
    public String offlineTrainingRequest(@ModelAttribute("offlineTraining") OfflineTraining offlineTraining,
                                         @RequestParam(value = "selectedId", required = false) String selectedId,
                                         @RequestParam(value = "deselectedId", required = false) String deselectedId
                                         ) {
        if(StringUtils.isEmpty(selectedId) == false) {
            OfflineTrainingDocument offlineTrainingDocument = new OfflineTrainingDocument();
            offlineTrainingDocument.setDocumentVersion(documentVersionService.findById(selectedId));
            if(offlineTraining.getOfflineTrainingDocuments().contains(offlineTrainingDocument) == false) {
                offlineTraining.getOfflineTrainingDocuments().add(offlineTrainingDocument);
            }
        } else if(StringUtils.isEmpty(deselectedId) == false) {
            offlineTraining.getOfflineTrainingDocuments().removeAll(offlineTraining.getOfflineTrainingDocuments().stream().filter(s -> s.getDocumentVersion().getId().equals(deselectedId)).collect(Collectors.toList()));
        }

        if(ObjectUtils.isEmpty(offlineTraining.getOfflineTrainingDocuments())) {
            offlineTraining.setSopIds(null);
        }

        return "training/offline/request";
    }


    @PostMapping("/training/sop/offline-training/request")
    public String offlineTrainingRequest(@ModelAttribute("offlineTraining") OfflineTraining offlineTraining, BindingResult result, SessionStatus status, RedirectAttributes attributes, @CurrentUser Account user) {
        offlineTrainingValidator.validate(offlineTraining, result);

        if(result.hasErrors()) {
            return "training/offline/request";
        }

        offlineTraining.setStatus(OfflineTrainingStatus.SUBMITTED);
        offlineTraining.setEmpNo(user.getEmpNo());
        offlineTrainingService.save(offlineTraining);

        offlineTrainingService.sendSubmittedEmail(user, offlineTraining);
        attributes.addFlashAttribute("message", "오프라인 교육 신청이 완료 되었습니다.");
        return "redirect:/training/sop/offline-training";
    }

    @GetMapping("/training/sop/trainingLog")
    public String trainingLog(@PageableDefault(size = 15, sort = {"completeDate"}, direction = Sort.Direction.DESC) Pageable pageable,
                              @CurrentUser Account user, Model model) {
        QTrainingLog qTrainingLog = QTrainingLog.trainingLog;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qTrainingLog.user.id.eq(user.getId()));
        builder.and(qTrainingLog.status.eq(TrainingStatus.COMPLETED));

        model.addAttribute("trainingLog", trainingLogService.findAll(builder, pageable));

        //2021-03-17. 설정된 회사명 사용
        model.addAttribute("siteCompanyTitle", siteCompanyTitle);

        return "training/trainingLog";
    }

    @PostMapping("/training/sop/trainingLog")
    public String uploadTrainingLog(@RequestParam("file") MultipartFile multipartFile, RedirectAttributes attributes,
                                    @CurrentUser Account user, Model model) throws Exception {
        XWPFDocument doc = new XWPFDocument(multipartFile.getInputStream());
        boolean hasError = false;
        List<XWPFTable> tables = doc.getTables();
        log.info("@User[{}] Training Log Upload Table Size : {}", user.getUsername(), tables.size());
        if (tables.size() == 3) {
            XWPFTable headerTable = tables.get(0);
            boolean isHeader = headerTable.getNumberOfRows() == 2 && headerTable.getRow(0).getTableCells().size() == 4;
            XWPFTable logTable = tables.get(1);

            boolean isLogTable = logTable.getNumberOfRows() > 1 && logTable.getRow(0).getTableCells().size() == 4;
            if(isHeader && isLogTable) {
                String empNo = headerTable.getRow(1).getCell(3).getText();
                log.debug("@Employee No : {}", empNo);

                if(!empNo.equals(user.getEmpNo())) {
                    log.warn("로그인한 사용자의 트레이닝 로그 파일 Emp No가 다름.");

                    attributes.addFlashAttribute("message", "Training Log 파일의 사번과 로그인한 사용자의 사번이 다릅니다.");
                    return "redirect:/training/trainingLog";
                }
                List<TrainingLog> trainingLogs = new ArrayList<>();
                for(int i = 1; i < logTable.getNumberOfRows(); i ++) {
                    XWPFTableRow row = logTable.getRow(i);
                    String completionDate = row.getCell(0).getText();
                    String trainingCourse = row.getCell(1).getText();
                    String trainingHr = row.getCell(2).getText();
                    String organization = row.getCell(3).getText();
                    double time = Double.parseDouble(trainingHr) * 3600;
                    boolean isSelfTraining = TrainingType.SELF.getLabel().toUpperCase().equals(organization.toUpperCase());
                    Date completeDate = DateUtils.toDate(completionDate, "dd-MMM-yyyy");
                    if(ObjectUtils.isEmpty(completeDate)) {
                        hasError = true;
                    }
                    TrainingLog trainingLog = TrainingLog.builder()
                            .completeDate(completeDate)
                            .trainingTime((int)time)
                            .type(isSelfTraining ? TrainingType.SELF : TrainingType.OTHER)
                            .build();

                    if(isSelfTraining == false) {
                        trainingLog.setOrganizationOther(organization);
                    }

//                    Pattern pattern = Pattern.compile("^(([A-Z]{3})\\-([A-Z]{2,5})(\\d{4}))\\s\\w(\\d\\.\\d)\\s(.+)");
                    Pattern pattern = Pattern.compile("^(([A-Z]{4})\\-([A-Z]{2,10})(\\d{3}))\\s\\w(\\d\\.\\d)\\s(.+)");
                    Matcher matcher = pattern.matcher(trainingCourse);
                    if(matcher.matches()) {
                        trainingLog.setMatched(true);
                        String docId = matcher.group(1);
                        String categoryId = matcher.group(3);
                        String sopNo = matcher.group(4);
                        String version = matcher.group(5);
                        String title = matcher.group(6);

                        log.debug("{}. {}, {}, {}, {}, {}\n", i, docId, categoryId, sopNo, version, title);

                        QDocument qDocument = QDocument.document;
                        BooleanBuilder builder = new BooleanBuilder();
                        builder.and(qDocument.docId.eq(docId));
                        Optional<Document> optionalDocument = documentService.findOne(builder);
                        if(optionalDocument.isPresent()) {
                            Document sop = optionalDocument.get();
                            QDocumentVersion qDocumentVersion = QDocumentVersion.documentVersion;
                            BooleanBuilder verBuilder = new BooleanBuilder();
                            verBuilder.and(qDocumentVersion.document.id.eq(sop.getId()));
                            verBuilder.and(qDocumentVersion.version.eq(version));
                            Optional<DocumentVersion> optionalDocumentVersion = documentVersionService.findOne(verBuilder);
//                            trainingLog.setDocumentVersion(DocumentVersion.builder()
//                                    .document(Document.builder().documentNo(sopNo).type(DocumentType.SOP).docId(docId).title(title).build())
//                                    .version(version)
//                                    .build());
                            if(optionalDocumentVersion.isPresent()) {
                                DocumentVersion documentVersion = optionalDocumentVersion.get();
                                trainingLog.setDocumentVersion(documentVersion);
                                QTrainingPeriod qTrainingPeriod = QTrainingPeriod.trainingPeriod;
                                BooleanBuilder pBuilder = new BooleanBuilder();
                                pBuilder.and(qTrainingPeriod.documentVersion.id.eq(documentVersion.getId()));
                                pBuilder.and(qTrainingPeriod.trainingType.eq(TrainingType.SELF));
                                trainingLog.setTrainingPeriod(trainingPeriodService.findOne(pBuilder).get());
                            } else {
                                trainingLog.setImportTrainingCourse(trainingCourse);
                            }
                        } else {
                            trainingLog.setImportTrainingCourse(trainingCourse);
                        }
                    } else {
                        trainingLog.setImportTrainingCourse(trainingCourse);
                    }

                    if(hasError == false && StringUtils.isEmpty(trainingLog.getImportTrainingCourse()) == false) {
                        hasError = true;
                    }
                    trainingLogs.add(trainingLog);
                }
                model.addAttribute("hasError", hasError);
                model.addAttribute("empNo", empNo);
                model.addAttribute("trainingLogs", trainingLogs);

                //2021-03-17 YSH :: Site Code 공통작업.
                model.addAttribute("siteCode", siteCode);

                return "training/importTrainingLog";
            }
        }

        attributes.addFlashAttribute("message", "Training Log 정보를 확인 할 수 없습니다.");
        return "redirect:/training/sop/trainingLog";
    }

    @PutMapping("/training/sop/trainingLog")
    @Transactional
    public String importTrainingLog(@ModelAttribute("trainingLogs") List<TrainingLog> trainingLogs,
                                    SessionStatus status, RedirectAttributes attributes,
                                    @CurrentUser Account user) {

        trainingLogService.saveAll(trainingLogs, user);
        status.setComplete();
        attributes.addFlashAttribute("message", "Training Log가 등록 되었습니다.");
        return "redirect:/training/sop/trainingLog";
    }

    @GetMapping("/training/sop/teamDeptTrainingLog")
    @PreAuthorize("authentication.principal.lev eq 1 or authentication.principal.teamManager eq true")
    public String teamDeptTrainingLog(@PageableDefault(size = 25) Pageable pageable,
                                      @CurrentUser Account user,
                                      @RequestParam(value = "departmentId", required = false) Integer departmentId,
                                      @RequestParam(value = "userId", required = false) Integer userId,
                                      Model model) {
        //TODO 팀장, 부서장 정확히 할것
//        log.info("@User.lev : {}", user.getLev());
        log.info("@User.teamManager : {}", user.isTeamManager());
        QTrainingLog qTrainingLog = QTrainingLog.trainingLog;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qTrainingLog.user.id.ne(user.getId()));

        if(!ObjectUtils.isEmpty(userId)) {
            builder.and(qTrainingLog.user.id.eq(userId));
        }

//        if(user.getLev() == 1 && StringUtils.isEmpty(user.getDeptCode()) == false && StringUtils.isEmpty(user.getTeamCode())) {//부서장
//            builder.and(qTrainingLog.user.deptCode.eq(user.getDeptCode()));
//            if(!ObjectUtils.isEmpty(teamCode)) {
//                builder.and(qTrainingLog.user.teamCode.eq(teamCode));
//            }
//            Map<String, String> param = new HashMap<>();
//            param.put("gwUserTbl", gwUserTbl);
//            param.put("gwDeptTbl", gwDeptTbl);
//            param.put("deptCode", user.getDeptCode());
//
//            model.addAttribute("teamList", deptUserMapper.findByDeptTeam(param));
//        } else if(user.isTeamManager()) {//팀장
//            builder.and(qTrainingLog.user.teamCode.eq(user.getTeamCode()));
//        }
//        builder.and(qTrainingLog.status.eq(TrainingStatus.COMPLETED));
//
//        if(user.isTeamManager() || ObjectUtils.isEmpty(teamCode) == false) {
//            QUser qUser = QUser.user;
//            BooleanBuilder userBuilder = new BooleanBuilder();
//            userBuilder.and(qUser.teamCode.eq(ObjectUtils.isEmpty(teamCode) ? user.getTeamCode() : teamCode));
//            model.addAttribute("userList", userRepository.findAll(userBuilder, qUser.engName.asc()));
//        }

        model.addAttribute("trainingLog", trainingLogService.findAll(builder, pageable));
        return "training/teamDeptTrainingLog";
    }

    @GetMapping("/training/sop/teamDeptTrainingLog2")
    @PreAuthorize("authentication.principal.lev eq 1 or authentication.principal.teamManager eq true")
    public String teamDeptTrainingLog2(@PageableDefault(size = 25) Pageable pageable,
                                      @CurrentUser Account user,
                                      @RequestParam(value = "departmentId", required = false) Integer departmentId,
                                      @RequestParam(value = "userId", required = false) Integer userId,
                                      @RequestParam(value = "docId", required = false) String docId,
                                      Model model) {
//        //TODO 팀장, 부서장 정확히 할것
//        log.info("@User.lev : {}", user.getLev());
//        log.info("@User.teamManager : {}", user.isTeamManager());
//        if(user.getLev() == 1 && StringUtils.isEmpty(user.getDeptCode()) == false && StringUtils.isEmpty(user.getTeamCode())) {//부서장
//            Map<String, String> param = new HashMap<>();
//            param.put("gwUserTbl", gwUserTbl);
//            param.put("gwDeptTbl", gwDeptTbl);
//            param.put("deptCode", user.getDeptCode());
//            model.addAttribute("teamList", deptUserMapper.findByDeptTeam(param));
//        }
//
//        List usernames = new ArrayList();
//        if(user.isTeamManager() || !ObjectUtils.isEmpty(teamCode)) {
//            QUser qUser = QUser.user;
//            BooleanBuilder userBuilder = new BooleanBuilder();
//            if(!ObjectUtils.isEmpty(teamCode)) {
//                userBuilder.and(qUser.teamCode.eq(teamCode));
//            } else if(!ObjectUtils.isEmpty(user.getTeamCode())) {
//                userBuilder.and(qUser.teamCode.eq(user.getTeamCode()));
//            } else {
//                userBuilder.and(qUser.deptCode.eq(user.getDeptCode()));
//            }
//
//            Iterable<User> users = userRepository.findAll(userBuilder, qUser.korName.asc());
//            model.addAttribute("userList", users);
//        }

//        model.addAttribute("trainingLog", trainingLogService.findAll(builder, pageable));
//        BooleanBuilder docStatus = new BooleanBuilder();
//        QDocumentVersion qDocumentVersion = QDocumentVersion.documentVersion;
//        docStatus.and(qDocumentVersion.status.notIn(DocumentStatus.DEVELOPMENT, DocumentStatus.REVISION));
//        model.addAttribute("trainingLog", sopTrainingMatrixRepository.getTrainingList(user.getDeptCode(), teamCode, userId, docId, user, pageable, docStatus));
        return "training/teamDeptTrainingLog2";
    }

    @PostMapping("/training/sop/teamDeptTrainingLog2")
    @Transactional(readOnly = true)
    @PreAuthorize("authentication.principal.lev eq 1 or authentication.principal.teamManager eq true")
    public void downloadTeamDeptTrainingLog(
            @CurrentUser Account user,
            @RequestParam(value = "teamId", required = false) Integer teamId,
            @RequestParam(value = "userId", required = false) Integer userId,
            @RequestParam(value = "docId", required = false) String docId,
            HttpServletResponse response) throws Exception {

        //트레이닝 완료 되지 않았을 경우,
        BooleanBuilder completeStatus = new BooleanBuilder();
        QTrainingLog qTrainingLog = QTrainingLog.trainingLog;
        completeStatus.and(qTrainingLog.status.notIn(TrainingStatus.COMPLETED).or(qTrainingLog.status.isNull()));

        Department department = departmentService.getDepartmentById(teamId);
        List<MyTraining> trainingList = trainingMatrixRepository.getDownloadTrainingList(department, userId, docId, user, completeStatus);
        InputStream is = IndexReportService.class.getResourceAsStream("Admin_SOP_TrainingLog.xlsx");
        Context context = new Context();
        context.putVar("trainings", trainingList);
        response.setHeader("Content-Disposition", "attachment; filename=\"TrainingLog("+DateUtils.format(new Date(), "yyyyMMdd")+").xlsx\"");
        JxlsHelper.getInstance().processTemplate(is, response.getOutputStream(), context);
    }

//    @GetMapping("/training/trainingLog/employees")
//    public String employeesTrainingLog(@PageableDefault(size = 25) Pageable pageable, @CurrentUser User user, Model model) {
//        List<Map<String, String>> employees = deptUserMapper.getAllUsers();
//        model.addAttribute("employees", employees);
//        return "training/employees";
//    }
//
//    @GetMapping("/training/trainingLog/employees/{empNo}")
//    public void getEmployeeTrainingLog(@PathVariable("empNo") String empNo, HttpServletRequest request, HttpServletResponse response) throws Exception {
//        Optional<User> optionalUser = userRepository.findByEmpNo(empNo);
//        if(optionalUser.isPresent()) {
//            generateTrainingLog(request, response, optionalUser.get(), false);
//        } else {
//            response.sendError(404, "Oops! 404 -_-;;");
//        }
//    }

//    @PostMapping("/training/sop/trainingLog/publish")
//    public String publishTrainingLog(@CurrentUser Account user, RedirectAttributes attributes) throws Exception {
//        Optional<TrainingRecord> optionalTrainingRecord = trainingRecordRepository.findTop1ByUsernameOrderByIdDesc(user.getUsername());
//        if(optionalTrainingRecord.isPresent()) {
//            TrainingRecord trainingRecord = optionalTrainingRecord.get();
//            if(TrainingRecordStatus.REVIEW == trainingRecord.getStatus()) {
//                attributes.addFlashAttribute("message", "현재 검토 진행 중 입니다. 완료 후 배포 가능 합니다.");
//                return "redirect:/training/sop/trainingLog";
//            } else {
//                generateTrainingLog(user, trainingRecord.getStatus() == TrainingRecordStatus.REVIEWED ? null : trainingRecord.getId());
//            }
//        } else {
//            generateTrainingLog(user, null);
//        }
//
//        attributes.addFlashAttribute("message", "Training Log 배포 완료 되었습니다.");
//        return "redirect:/training/sop/trainingLog";
//    }

    @GetMapping("/training/sop/trainingLog/export")
    @Transactional
    public void exportTrainingLog(@CurrentUser Account user, HttpServletResponse response) throws Exception {

        QTrainingLog qTrainingLog = QTrainingLog.trainingLog;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qTrainingLog.user.id.eq(user.getId()));
        builder.and(qTrainingLog.status.eq(TrainingStatus.COMPLETED));
        Iterable<TrainingLog> iterable = trainingLogService.findAll(builder, qTrainingLog.completeDate.desc());
        List<TrainingLogReport> trainingLogs = StreamSupport.stream(iterable.spliterator(), false)
                .map(t -> TrainingLogReport.builder()
                        .completeDate(DateUtils.format(t.getCompleteDate(), "dd-MMM-yyyy").toUpperCase())
                        .course(t.getTrainingCourse())
                        .hr(t.getHour())
                        .organization(t.getOrganization())
                        .build()).collect(Collectors.toList());

        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=SOP_Training_Log_" + new Date(System.currentTimeMillis()) + ".pdf");
        response.setContentType("application/pdf");

        log.debug("@Training Logs : {}", trainingLogs);
        log.debug("@User : {}", user);

        if(trainingLogReportService.sopGenerateReport(trainingLogs, user, response.getOutputStream())){
           trainingAccessLogService.save(user, TrainingLogType.SOP_TRAINING_LOG, DocumentAccessType.DOWNLOAD);

        }
    }
}
