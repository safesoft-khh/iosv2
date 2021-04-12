package com.cauh.iso.controller;

import com.cauh.common.entity.Account;
import com.cauh.common.security.annotation.CurrentUser;
import com.cauh.common.service.UserService;
import com.cauh.iso.domain.*;
import com.cauh.iso.domain.QDocument;
import com.cauh.iso.domain.QISO;
import com.cauh.iso.domain.constant.*;
import com.cauh.iso.security.annotation.IsAdmin;
import com.cauh.iso.service.*;
import com.cauh.iso.validator.ISOValidator;
import com.cauh.iso.validator.QuizValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.groupdocs.conversion.internal.c.a.s.internal.mw.Ca;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.WebUtils;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Slf4j
@SessionAttributes({"iso", "quiz"})
@RequiredArgsConstructor
public class ISOController {

    private final ISOService isoService;
    private final ISOAccessLogService isoAccessLogService;
    private final ISOValidator isoValidator;
    private final FileStorageService fileStorageService;
    private final UserService userService;
    private final QuizValidator quizValidator;
    private final ISOCategoryService isoCategoryService;

//    private String categoryCode;

    @GetMapping("/iso-14155")
    public String ISOList(){
        return "redirect:/iso-14155/board";
    }

    @GetMapping( "/iso-14155/board")
    public String ISOBoardList(@RequestParam(value = "categoryCode", required = false) String categoryCode,
                               @PageableDefault(sort = {"createdDate"}, direction = Sort.Direction.DESC, size = 15) Pageable pageable,
                               @CurrentUser Account user, Model model) {
        QISO qISO = QISO.iSO;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qISO.deleted.eq(false));
        builder.and(qISO.training.eq(false));

        if(StringUtils.isEmpty(categoryCode) == false) {
            model.addAttribute("categoryCode", categoryCode);
            //TODO ISO
            ISOCategory isoCategory = isoCategoryService.findByShortName(categoryCode).get();
            builder.and(qISO.category.eq(isoCategory));
        }
        else
            model.addAttribute("categoryCode", "");

        //공지사항 리스트
        model.addAttribute("isoList", isoService.getPage(builder, pageable));

        //공지사항(상단공지)
        Date today = new Date(System.currentTimeMillis());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        builder.and(qISO.topViewEndDate.goe(Date.valueOf(format.format(today))));

        Iterable<ISO> topISOs = isoService.getTopISOs(builder);
        model.addAttribute("topISOList", topISOs);

//        Optional<ISOTrainingMatrixFile> optionalTrainingMatrixFile = isotrainingMatrixService.findFirstByOrderByIdDesc();
//        model.addAttribute("isoTrainingMatrixFile", optionalTrainingMatrixFile.isPresent() ? optionalTrainingMatrixFile.get() : null);

        List<ISOCategory> categoryList = isoCategoryService.getCategoryList();
        model.addAttribute("categoryList", categoryList);

        return "iso/iso14155/boardList";
    }

    @GetMapping("/iso-14155/training")
    public String ISOTrainingList(@RequestParam(value = "categoryCode", required = false) String categoryCode,
                                  @PageableDefault(sort = {"createdDate"}, direction = Sort.Direction.DESC, size = 15) Pageable pageable, Model model) {
        QISO qISO = QISO.iSO;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qISO.deleted.eq(false));
        builder.and(qISO.training.eq(true));
        if(StringUtils.isEmpty(categoryCode) == false) {
            model.addAttribute("categoryCode", categoryCode);
            //TODO ISO
            ISOCategory isoCategory = isoCategoryService.findByShortName(categoryCode).get();
            builder.and(qISO.category.eq(isoCategory));
        }
        else
            model.addAttribute("categoryCode", "");

        //공지사항 리스트
        model.addAttribute("isoList", isoService.getPage(builder, pageable));

        //공지사항(상단공지)
        Date today = new Date(System.currentTimeMillis());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        builder.and(qISO.topViewEndDate.goe(Date.valueOf(format.format(today))));
        model.addAttribute("topISOList", isoService.getTopISOs(builder));

        List<ISOCategory> categoryList = isoCategoryService.getCategoryList();
        model.addAttribute("categoryList", categoryList);

        return "iso/iso14155/trainingList";
    }

    @IsAdmin
    @GetMapping("/iso-14155/{type}/new")
    public String newISO(Model model,
                         @RequestParam(value = "categoryCode", required = false) String categoryCode,
                         @PathVariable("type") String type,
                         RedirectAttributes attributes) {
        ISO iso = new ISO();
        if(!StringUtils.isEmpty(type) && type.equals("training")) {
            iso.setTraining(true);
        }

        iso.setTrainingAll(true);
        model.addAttribute("userMap", userService.getUserMap());
        model.addAttribute("iso", iso);

        List<ISOCategory> categoryList = isoCategoryService.getCategoryList();
        model.addAttribute("categoryList", categoryList);

        if(categoryList.isEmpty())
        {
            attributes.addFlashAttribute("message", "존재하지 않는 게시물 입니다.");
            return "redirect:/iso-14155/board";
        }

        if(categoryCode.isEmpty())      model.addAttribute("categoryCode", "");
        else                            model.addAttribute("categoryCode", categoryCode);
//        ISOCategory category = isoCategoryService.findByShortName(categoryCode).get();
//        model.addAttribute("category", category);

        return "iso/iso14155/edit";
    }

    @IsAdmin
    @GetMapping("/iso-14155/{isoId}/edit")
    public String isoEdit(@RequestParam(value = "categoryCode", required = false) String categoryCode,
                          @PathVariable("isoId") String isoId,
                          Model model, RedirectAttributes attributes) {
        Optional<ISO> isoOptional = isoService.getISO(isoId);
        if (isoOptional.isPresent()) {

            ISO iso = isoOptional.get();
            if (iso.isTraining()) {
                ISOTrainingPeriod isoTrainingPeriod = iso.getIsoTrainingPeriods().size() > 0?iso.getIsoTrainingPeriods().get(0):null;
                iso.setStartDate(isoTrainingPeriod.getStartDate());
                iso.setEndDate(isoTrainingPeriod.getEndDate());

                //TrainingAll이 있으면 trainingAll로 세팅 아니면 유저별 참석
                if(iso.getIsoTrainingMatrix().stream().filter(d -> d.isTrainingAll()).count() > 0) {
                    iso.setTrainingAll(true);
                } else {
                    List<String> ids = iso.getIsoTrainingMatrix().stream().map(im -> Integer.toString(im.getUser().getId())).collect(Collectors.toList());
                    iso.setUserIds(ids.toArray(new String[ids.size()]));
                }
            }

            model.addAttribute("iso", iso);
            model.addAttribute("userMap", userService.getUserMap());
            model.addAttribute("categoryCode", categoryCode);
        } else {
            attributes.addFlashAttribute("message", "존재하지 않는 게시물 입니다.");
            return "redirect:/iso?categoryCode="+categoryCode;
        }
        return "iso/iso14155/edit";
    }

    @IsAdmin
    @Transactional
    @PostMapping({"/iso-14155/{type}/new", "/iso-14155/{isoId}/edit"})
    public String saveISO(
                          @RequestParam(value = "categoryCode", required = false) String categoryCode,
                          @PathVariable(value = "type", required = false) String type,
                          @PathVariable(value = "isoId", required = false) String isoId,
                          @ModelAttribute("iso") ISO iso,
                          @RequestParam(value = "uploadingFile") MultipartFile uploadingFile,
                          BindingResult bindingResult, SessionStatus sessionStatus,
                          RedirectAttributes attributes, Model model,
                          HttpServletRequest request) {

        Optional<ISOCategory> ca = isoCategoryService.findByShortName(categoryCode);
        if(ca.isEmpty())
        {
            List<ISOCategory> categoryList = isoCategoryService.getCategoryList();
            model.addAttribute("categoryList", categoryList);
            model.addAttribute("categoryCode", categoryCode);

            model.addAttribute("userMap", userService.getUserMap());
            attributes.addFlashAttribute("message", "Catetory를 선택해 주세요.");
            return "iso/iso14155/edit";
        }

        //업로드 진행할 파일의 이름을 넣음.
        iso.setUploadFileName(uploadingFile.getOriginalFilename());
        isoValidator.validate(iso, bindingResult);

        if (bindingResult.hasErrors()) {
            List<ISOCategory> categoryList = isoCategoryService.getCategoryList();
            model.addAttribute("categoryList", categoryList);
            model.addAttribute("categoryCode", categoryCode);

            model.addAttribute("userMap", userService.getUserMap());
            return "iso/iso14155/edit";
        }

        ISOCategory isoCategory = isoCategoryService.findByShortName(categoryCode).get();

        //TODO HKH
        iso.setCategory(isoCategory);
        iso.setPostStatus(PostStatus.NONE);
        iso.setIsoType(ISOType.ISO_14155);
        ISO savedISO = isoService.saveISO(iso, uploadingFile);

        sessionStatus.setComplete();

        if (ObjectUtils.isEmpty(isoId)) {
            attributes.addFlashAttribute("message", "ISO 14155가 저장 되었습니다.");
            return "redirect:/iso-14155/" + savedISO.getId() + (StringUtils.isEmpty(request.getQueryString()) ? "" : "?" + request.getQueryString());
        } else {
            attributes.addFlashAttribute("message", "ISO 14155가 수정 되었습니다.");
            return "redirect:/iso-14155/{isoId}" + (StringUtils.isEmpty(request.getQueryString()) ? "" : "?" + request.getQueryString());
        }

    }

    @GetMapping("/iso-14155/{isoId}")
    public String isoView(
            @RequestParam(value = "categoryCode", required = false) String categoryCode,
            @PageableDefault(sort = {"createdDate"}, direction = Sort.Direction.DESC, size = 15) Pageable pageable,
            @PathVariable("isoId") String isoId, Model model, RedirectAttributes attributes) {
        Optional<ISO> isoOptional = isoService.getISO(isoId);
        if (isoOptional.isPresent()) {
            ISO iso = isoOptional.get();
            if (iso.isTraining()) {
                ISOTrainingPeriod isoTrainingPeriod = iso.getIsoTrainingPeriods().size() > 0?iso.getIsoTrainingPeriods().get(0):null;
                iso.setStartDate(isoTrainingPeriod.getStartDate());
                iso.setEndDate(isoTrainingPeriod.getEndDate());

                //TrainingAll이 있으면 trainingAll로 세팅 아니면 유저별 참석
                if(iso.getIsoTrainingMatrix().stream().filter(d -> d.isTrainingAll()).count() > 0) {
                    iso.setTrainingAll(true);
                } else {
                    List<Account> userList = iso.getIsoTrainingMatrix().stream().map(tm -> tm.getUser()).collect(Collectors.toList());
                    Page<Account> userPageList = new PageImpl<>(userList, pageable, userList.size());

                    iso.setTrainingAll(false);
                    model.addAttribute("userPageList", userPageList);
                }
            }
            model.addAttribute("viewIso", iso);
        } else {
            attributes.addFlashAttribute("message", "존재하지 않는 ISO 게시물 입니다.");
            return "redirect:/iso-14155";
        }

        //model.addAttribute("categoryCode", categoryCode);
        return "iso/iso14155/view";
    }

    @IsAdmin
    @PutMapping("/iso-14155/active")
    @Transactional
    public String isoActive(@RequestParam("isoId") String isoId, RedirectAttributes attributes) {

        Optional<ISO> isoOptional = isoService.getISO(isoId);
        if(isoOptional.isPresent()) {
            ISO iso = isoOptional.get();
            String res = isoService.isoActivate(iso);

            if(!res.equals("success")) {
                attributes.addFlashAttribute("messageType", "danger");
                attributes.addFlashAttribute("message", res);
                return "redirect:/iso-14155/training";
            }

            attributes.addFlashAttribute("message", "[" + iso.getTitle() + "] Training이 Active되었습니다.");
        }

        return "redirect:/iso-14155/training";
    }

    @IsAdmin
    @PutMapping("/iso-14155/expand")
    @Transactional
    public String isoTrainingPeriodExpand(@RequestParam("isoId") String isoId, @RequestParam("addDays") Integer addDays, RedirectAttributes attributes) {

        log.info("@ISO Training Period Exapnd : {}, {}", isoId, addDays);

        Optional<ISO> isoOptional = isoService.getISO(isoId);
        if(isoOptional.isPresent()) {
            ISO iso = isoOptional.get();
            String res = isoService.isoPeriodExpand(iso, addDays);

            if(!res.equals("success")) {
                attributes.addFlashAttribute("messageType", "danger");
                attributes.addFlashAttribute("message", res);
                return "redirect:/iso-14155/training";
            }

            attributes.addFlashAttribute("message", "[" + iso.getTitle() + "] Training의 기한이 연장되었습니다.");
        }

        return "redirect:/iso-14155/training";
    }

//    /**
//     * 메일전송 대상자에게 메일 전송
//     * @param isoId
//     * @return
//     */
//    @IsAdmin
//    @GetMapping("/ajax/iso-14155/{isoId}/send")
//    @ResponseBody
//    public Map<String, String> sendEmail(@PathVariable("isoId") String isoId) {
//        Map<String, String> model = new HashMap<>();
//        isoService.sendMail(isoId);
//        model.put("result", "success");
//        model.put("id", isoId);
//        return model;
//    }

    /**
     * ISO 삭제.
     * @param isoId
     * @param attributes
     * @param request
     * @return
     */
    @IsAdmin
    @DeleteMapping("/iso-14155/{isoId}")
    public String isoRemove(@PathVariable("isoId") String isoId, RedirectAttributes attributes, HttpServletRequest request) {
        Optional<ISO> iso = isoService.getISO(isoId);
        if(iso.isPresent() && iso.get().isActive()){
            attributes.addFlashAttribute("messageType", "danger");
            attributes.addFlashAttribute("message", "삭제 실패 :: 교육 진행중인 ISO입니다.");
            return "redirect:/iso-14155/" + (iso.get().isTraining()?"training":"board");
        }else if (iso.isPresent()) {
            isoService.remove(iso.get());
            attributes.addFlashAttribute("message", "ISO-14155 게시물이 삭제 되었습니다.");
            return "redirect:/iso-14155/" + (iso.get().isTraining()?"training":"board") + (StringUtils.isEmpty(request.getQueryString()) ? "" : "?" + request.getQueryString());
        } else {
            attributes.addFlashAttribute("messageType", "danger");
            attributes.addFlashAttribute("message", "존재하지 않는 ISO-14155 게시물 입니다.");
            return "redirect:/iso-14155/board";
        }
    }

    /**
     * 퀴즈 입력 화면
     * @param isoId
     * @param model
     * @param attributes
     * @return
     */
    @IsAdmin
    @GetMapping("/iso-14155/{isoId}/quiz")
    public String quizEdit(@PathVariable("isoId") String isoId, Model model, RedirectAttributes attributes){
        Optional<ISO> isoOptional = isoService.getISO(isoId);

        if(isoOptional.isEmpty()) {
            attributes.addFlashAttribute("messageType", "danger");
            attributes.addFlashAttribute("message", "존재하지 않는 ISO입니다.");
            return "redirect:/iso-14155";
        }

        ISO iso = isoOptional.get();
        model.addAttribute("iso", iso);

        Quiz quiz = null;
        if(!StringUtils.isEmpty(iso.getQuiz())) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                quiz = objectMapper.readValue(iso.getQuiz(), Quiz.class);
            } catch (Exception error) {
                log.error("error : ", error);
            }
        } else {
            quiz = new Quiz();
            List<QuizQuestion> questions = new ArrayList<>();

            for (int i = 0; i < 25; i++) {
                List<QuizAnswer> quizAnswers = new ArrayList<>();
                for (int x = 0; x < 5; x++) {
                    quizAnswers.add(new QuizAnswer(x + 1));
                }

                QuizQuestion quizQuestion = new QuizQuestion(i + 1);
                quizQuestion.setAnswers(quizAnswers);
                questions.add(quizQuestion);
            }
            quiz.setQuizQuestions(questions);
        }
        model.addAttribute("quiz", quiz);

        return "iso/iso14155/quiz";
    }

    /**
     * 퀴즈 등록
     * @param isoId
     * @param quiz
     * @param result
     * @param status
     * @param request
     * @param attributes
     * @return
     * @throws Exception
     */
    @IsAdmin
    @PostMapping("/iso-14155/{isoId}/quiz")
    public String saveQuiz(@PathVariable("isoId") String isoId,
                           @ModelAttribute("quiz") Quiz quiz,
                           BindingResult result, SessionStatus status,
                           HttpServletRequest request, Model model,
                           RedirectAttributes attributes) throws Exception {
        boolean isQuizRemove = WebUtils.hasSubmitParameter(request, "removeQuiz");
        boolean isRemove = WebUtils.hasSubmitParameter(request, "remove");
        boolean isQuizAdd = WebUtils.hasSubmitParameter(request, "add");

        if(isQuizRemove) {
            if(quiz.getQuizQuestions().size() <= 25) {
                log.debug("@Quiz Minimum");
                model.addAttribute("type", "warning");
                model.addAttribute("message", "최소 문항 개수는 25개 입니다.");
                return "iso/iso14155/quiz";
            }

            String removeValue = ServletRequestUtils.getStringParameter(request, "removeQuiz");
            quiz.getQuizQuestions().remove(Integer.parseInt(removeValue));
            return "iso/iso14155/quiz";
        }
        else if(isRemove) {
            String removeValue = ServletRequestUtils.getStringParameter(request, "remove");
            String[] arr = removeValue.split("\\.");
            quiz.getQuizQuestions().get(Integer.parseInt(arr[0])).getAnswers().remove(Integer.parseInt(arr[1]));
            return "iso/iso14155/quiz";
        } else if(isQuizAdd) {
            if(quiz.getQuizQuestions().size() >= 30) {
                log.debug("@Quiz Maximum");
                model.addAttribute("type", "warning");
                model.addAttribute("message", "최대 문항 개수는 30개 입니다.");
                return "iso/iso14155/quiz";
            }

            QuizQuestion quizQuestion = new QuizQuestion(quiz.getQuizQuestions().size());
            List<QuizAnswer> quizAnswers = new ArrayList<>();
            for (int x = 0; x < 5; x++) {
                quizAnswers.add(new QuizAnswer(x + 1));
            }
            quizQuestion.setAnswers(quizAnswers);
            quiz.getQuizQuestions().add(quizQuestion);
            return "iso/iso14155/quiz";
        }

        quizValidator.validate(quiz, result);

        if(result.hasErrors()) {
            log.debug("Quiz Error : {}", result.getAllErrors());
            return "iso/iso14155/quiz";
        }

        ISO savedIso = isoService.saveQuiz(isoId, quiz);
        status.setComplete();

        if(ObjectUtils.isEmpty(savedIso)) {
            attributes.addFlashAttribute("messageType", "danger");
            attributes.addFlashAttribute("message", "존재하지 않는 ISO입니다.");
            return "redirect:/iso-14155/training";
        }

        attributes.addFlashAttribute("message", "[" + savedIso.getTitle() + "] 에 퀴즈 정보가 수정 되었습니다.");
        return "redirect:/iso-14155/training";
    }

    /**
     * 퀴즈 업로드
     * @param isoId
     * @param model
     * @param attributes
     * @return
     */
    @IsAdmin
    @GetMapping("/iso-14155/{isoId}/quiz/upload")
    public String quizUpload(@PathVariable("isoId") String isoId, Model model, RedirectAttributes attributes) {

//        model.addAttribute("docVerId", docVerId);
        Optional<ISO> isoOptional = isoService.getISO(isoId);

        if(isoOptional.isEmpty()) {
            attributes.addFlashAttribute("messageType", "danger");
            attributes.addFlashAttribute("message", "존재하지 않는 ISO입니다.");
            return "redirect:/iso-14155";
        }

        model.addAttribute("iso", isoOptional.get());
        model.addAttribute("title", isoOptional.get().getTitle());

        return "iso/iso14155/quiz-upload";
    }


    /**
     * 업로드 된 Template 파일을 토대로 Quiz 풀기.
     * @param isoId
     * @param multipartFile
     * @param model
     * @param attributes
     * @return
     */
    @PutMapping("/iso-14155/{isoId}/quiz")
    public String quizUploaded(@PathVariable("isoId") String isoId,
                               @RequestParam("quizTemplate") MultipartFile multipartFile,
                               Model model, RedirectAttributes attributes) {

        try {
            Quiz quiz = new Quiz();
            List<QuizQuestion> questions = new ArrayList<>();
            InputStream is = multipartFile.getInputStream();
            XSSFWorkbook workbook = new XSSFWorkbook(is);
            XSSFSheet sheet = workbook.getSheetAt(0);

            //검증 부분 사용 안함으로 인해 주석처리 : 2021-02-17
//            XSSFRow titleRow = sheet.getRow(2);
//            titleRow.getCell(1).setCellType(CellType.STRING);
//            String title = titleRow.getCell(1).getStringCellValue();
//
//            XSSFRow isoTypeRow = sheet.getRow(3);
//            isoTypeRow.getCell(1).setCellType(CellType.STRING);
//            String isoType = isoTypeRow.getCell(1).getStringCellValue();
//
//            log.debug("ISO Type : {}, title : {}", isoType, title);
//
//            //임시로 삭제.
//            ISO iso = isoService.findById(isoId);
//            if(!(iso.getIsoType().getLabel().equals(isoType) && iso.getTitle().equals(title))) {
//                attributes.addFlashAttribute("message", "Template에 Title/ISO Type 정보가 ISO 정보와 일치하지 않습니다.");
//                return "redirect:/iso-14155/training";
//            }

            XSSFRow qRow = sheet.getRow(7);
            XSSFRow a1Row = sheet.getRow(8);
            XSSFRow a2Row = sheet.getRow(9);
            XSSFRow a3Row = sheet.getRow(10);
            XSSFRow a4Row = sheet.getRow(11);
            XSSFRow a5Row = sheet.getRow(12);
            XSSFRow correctRow = sheet.getRow(13);

            //최대 30문항까지.
            for(int i = 1; i <= 30; i ++) {
                qRow.getCell(i).setCellType(CellType.STRING);
                a1Row.getCell(i).setCellType(CellType.STRING);
                a2Row.getCell(i).setCellType(CellType.STRING);
                a3Row.getCell(i).setCellType(CellType.STRING);
                a4Row.getCell(i).setCellType(CellType.STRING);
                a5Row.getCell(i).setCellType(CellType.STRING);

                log.info("@@GETCELL BEFORE : {}", i);

                String q = ObjectUtils.isEmpty(qRow.getCell(i))?null:qRow.getCell(i).getStringCellValue();
                String a1 = a1Row.getCell(i).getStringCellValue();
                String a2 = a2Row.getCell(i).getStringCellValue();
                String a3 = a3Row.getCell(i).getStringCellValue();
                String a4 = a4Row.getCell(i).getStringCellValue();
                String a5 = a5Row.getCell(i).getStringCellValue();
                correctRow.getCell(i).setCellType(CellType.STRING);
                String correct = correctRow.getCell(i).getStringCellValue();
                log.debug("Q : {}, a1 : {}, a2 : {}, a3 : {}, a4 : {}, a5 : {}, ## correct : {}", q, a1, a2, a3, a4, a5, correct);

                QuizQuestion quizQuestion = new QuizQuestion(i + 1);
                List<QuizAnswer> quizAnswers = new ArrayList<>();

                //문항 내용이 없을 경우, 읽기를 멈추고 여태 읽었던 내용까지만 작업을 진행한다.
                if(StringUtils.isEmpty(q) && i > 25) {
                    log.debug("@Quiz Upload Stop (25 over) :: 바로 빠져나옴.");
                    break;
                } else if(StringUtils.isEmpty(q) && i <= 25) { //CASE 2. 문서 내용이 없는 경우 일반 Template 적용
                    log.debug("@Quiz Upload Stop (25 less or equal) :: 기본 서식 추가");

                    for (int x = 0; x < 5; x++) {
                        quizAnswers.add(new QuizAnswer(x + 1));
                    }
                    quizQuestion.setAnswers(quizAnswers);
                    questions.add(quizQuestion);
                } else { //CASE 3. 문서 내 Quiz 서식 적용
                    quizQuestion.setText(q);
                    quizAnswers.add(new QuizAnswer(1, a1, eq("1", correct)));
                    quizAnswers.add(new QuizAnswer(2, a2, eq("2", correct)));
                    if(!StringUtils.isEmpty(a3)) {
                        quizAnswers.add(new QuizAnswer(3, a3, eq("3", correct)));
                    }
                    if(!StringUtils.isEmpty(a4)) {
                        quizAnswers.add(new QuizAnswer(4, a4, eq("4", correct)));
                    }
                    if(!StringUtils.isEmpty(a5)) {
                        quizAnswers.add(new QuizAnswer(5, a5, eq("5", correct)));
                    }
                    quizQuestion.setAnswers(quizAnswers);
                    questions.add(quizQuestion);
                }
            }

            quiz.setQuizQuestions(questions);
            model.addAttribute("quiz", quiz);

            return "iso/iso14155/quiz";
        } catch (Exception error) {
            log.error("error : {}", error.getMessage());
            attributes.addFlashAttribute("messageType", "danger");
            attributes.addFlashAttribute("message", "Quiz Upload 동작 중 에러가 발생하였습니다.");
            return "redirect:/iso-14155/training";
        }
    }

    /**
     * 퀴즈를 테스트 하는 화면 진입.
     * @param isoId
     * @param model
     * @return
     * @throws Exception
     */
    @GetMapping("/iso-14155/{isoId}/quiz/test")
    public String quizTest(@PathVariable("isoId") String isoId, Model model) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        Quiz quiz = objectMapper.readValue(isoService.getISO(isoId).get().getQuiz(), Quiz.class);
        Collections.shuffle(quiz.getQuizQuestions());
        for(QuizQuestion quizQuestion : quiz.getQuizQuestions()) {
            Collections.shuffle(quizQuestion.getAnswers());
        }
        model.addAttribute("quiz", quiz);
        return "iso/iso14155/quiz-test";
    }

    @GetMapping("/iso-14155/viewer/{isoId}")
    public void viewer(@PathVariable("isoId") String isoId, @RequestParam("page") int page, @CurrentUser Account user,
                       HttpServletRequest request, HttpServletResponse response) throws Exception {
        String fileName = isoId + "-" + page + ".jpg";
        log.info("FileName : {}", fileName);

        //ISO 접속 기록 저장
        isoAccessLogService.save(isoId, DocumentAccessType.TRAINING);
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        // Try to determine file's content type
        String contentType = "image/jpeg";

        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName+ "\"");
        response.setContentType(contentType);
        imageDraw(resource.getInputStream(), response.getOutputStream());
    }



    /**
     * 파일 다운로드
     * @param id
     * @param attachFileId
     * @param request
     * @return
     */
    @GetMapping("/iso-14155/{id}/downloadFile/{attachFileId:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable("id") String id, @PathVariable("attachFileId") String attachFileId,
                                                 HttpServletRequest request) {
        // Load file as Resource
        Optional<ISOAttachFile> optionalAttachFile = isoService.getAttachFile(attachFileId);
        if (optionalAttachFile.isPresent()) {
            ISOAttachFile attachFile = optionalAttachFile.get();
            Resource resource = fileStorageService.loadFileAsResource(attachFile.getFileName());

            // Try to determine file's content type
            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {
                log.info("Could not determine file type.");
            }

            // Fallback to the default content type if type could not be determined
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachFile.getOriginalFileName() + "\"")
                    .body(resource);
        } else {
            return ResponseEntity.of(Optional.empty());
        }
    }

    /**
     *
     * @param answerIndex
     * @param correct ex) 1,2 or 4
     * @return
     */
    private boolean eq(String answerIndex, String correct) {
        if(correct.trim().length() == 1) {
            return correct.trim().equals(answerIndex);
        }
        List<String> correctList = new ArrayList<>();
        String[] correctArr = correct.split(",");
        for(String str : correctArr) {
            correctList.add(str.trim());
        }
        return correctList.contains(answerIndex);
    }


    /**
     * 이미지 그리기
     * @param inputStream
     * @param os
     */
    public static void imageDraw(InputStream inputStream, OutputStream os) {
        try {
            BufferedImage original = ImageIO.read(inputStream);
            ImageIO.write(original, "jpg", os);
        } catch (Exception error) {
            log.error("Error : ", error);
        } finally {
            log.debug("ISO Viewer 이미지 생성 완료!");
            try {
                if (os != null) {
                    os.flush();
                    os.close();
                }
            } catch (IOException ioe) {}
        }
    }
}
