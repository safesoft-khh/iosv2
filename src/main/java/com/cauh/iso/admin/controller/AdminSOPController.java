package com.cauh.iso.admin.controller;

import com.cauh.common.entity.Account;
import com.cauh.common.security.annotation.CurrentUser;
import com.cauh.iso.admin.domain.constant.SOPAction;
import com.cauh.iso.domain.*;
import com.cauh.iso.domain.constant.ApprovalStatus;
import com.cauh.iso.domain.constant.DocumentLanguage;
import com.cauh.iso.domain.constant.DocumentStatus;
import com.cauh.iso.domain.constant.DocumentType;
import com.cauh.iso.domain.report.QRetirementDocument;
import com.cauh.iso.domain.report.RetirementDocument;
import com.cauh.iso.repository.DocumentVersionRepository;
import com.cauh.iso.repository.RetirementDocumentRepository;
import com.cauh.iso.service.*;
import com.cauh.iso.validator.DocumentVersionValidator;
import com.cauh.iso.validator.QuizValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xpath.operations.Bool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping({"/admin/{type}", "/ajax/admin/{type}"})
@RequiredArgsConstructor
@SessionAttributes({"documentVersion", "CategoryList", "sopList", "quiz", "jobDescriptionMap", "statusMap"})
@Slf4j
public class AdminSOPController {
    private final DocumentService documentService;
    private final DocumentVersionService documentVersionService;
    private final DocumentVersionValidator documentVersionValidator;
    private final CategoryService categoryService;
    private final QuizValidator quizValidator;
    private final JDService jdService;
    //private final RetirementDocumentRepository retirementDocumentRepository;
    private final DocumentVersionRepository documentVersionRepository;

    @Value("${sop.prefix}")
    private String sopPrefix;

    @Value("${form.prefix}")
    private String formPrefix;

    @Value("${form.name}")
    private String formName;

    @GetMapping("/management/{status}")
    public String management(@PathVariable("type") DocumentType type,
                             @PathVariable("status") String stringStatus,
                             @RequestParam(value = "category", required = false) String categoryId,
                             @RequestParam(value = "docId", required = false) String docId,
                             @PageableDefault(sort = {"createdDate"}, direction = Sort.Direction.DESC, size = 15) Pageable pageable, Model model) {
        DocumentStatus status = DocumentStatus.valueOf(stringStatus.toUpperCase());

        if(!StringUtils.isEmpty(docId)) {
            docId = docId.toUpperCase();
        }
        model.addAttribute("status", status);
        model.addAttribute("type", type);
        model.addAttribute("searchDocId", docId);
        model.addAttribute("documentList", documentVersionService.findAll(documentVersionService.getAdminSOPPredicate(type, status, categoryId, docId), pageable));

        model.addAttribute( "formName", formName);
        return "admin/sop/list";
    }


    @GetMapping({"/management/{stringStatus}/new", "/management/{stringStatus}/{id}/{action}"})
    public String edit(@PathVariable("type") DocumentType type,
                       @PathVariable("stringStatus") String stringStatus,
                       @PathVariable(value = "id", required = false) String id,
                       @PathVariable(value = "action", required = false) SOPAction action,
                       Model model) {
        DocumentStatus status = DocumentStatus.valueOf(stringStatus.toUpperCase());
        model.addAttribute("status", status);
        model.addAttribute("type", type);
        model.addAttribute("jobDescriptionMap", jdService.getJDMap());


        if(StringUtils.isEmpty(id)) {
            DocumentVersion documentVersion = DocumentVersion.builder()
                    .status(status)
                    .document(Document.builder().type(type).build())
                    .build();
            documentVersion.setAction(action);
            documentVersion.setTrainingAll(true);
            documentVersion.setNotification(false);

            model.addAttribute("documentVersion", documentVersion);
        } else {
            DocumentVersion documentVersion = documentVersionService.findById(id);
            documentVersion.setAction(action);
            //신규 작성 시 공지 알림 설정 기본값 : ON
            documentVersion.setNotification(false);

            if(type == DocumentType.SOP) {
                if(!ObjectUtils.isEmpty(documentVersion.getTrainingMatrixList())) {
                    //ALL 선택 인경우
                    if(documentVersion.getTrainingMatrixList().stream().filter(d -> d.isTrainingAll()).count() > 0) {
                        documentVersion.setTrainingAll(true);
                    } else {
                        List<String> ids = documentVersion.getTrainingMatrixList().stream().map(s -> Long.toString(s.getJobDescription().getId())).collect(Collectors.toList());
                        documentVersion.setJdIds(ids.toArray(new String[ids.size()]));
                    }
                } else {
                    documentVersion.setTrainingAll(true);
                }
            } else {
                //RF 파일이면서 개정(revision) 인경우
                if(!ObjectUtils.isEmpty(action)) {
                    if (action == SOPAction.revision) {
                        log.info("==> revision : {} 기존 파일정보 초기화", id);
                        documentVersion.setRfEngOriginalFileName(null);
                        documentVersion.setRfEngExt(null);
                        documentVersion.setRfEngFileName(null);
                        documentVersion.setRfEngFileSize(0);
                        documentVersion.setRfEngFileType(null);

                        documentVersion.setRfKorOriginalFileName(null);
                        documentVersion.setRfKorExt(null);
                        documentVersion.setRfKorFileName(null);
                        documentVersion.setRfKorFileSize(0);
                        documentVersion.setRfKorFileType(null);
                    }
                }
            }
            model.addAttribute("documentVersion", documentVersion);
        }

        if(!ObjectUtils.isEmpty(action)) {
            if(action == SOPAction.revision) {
                TreeMap<String, String> statusMap = new TreeMap<>();
                statusMap.put(DocumentStatus.APPROVED.name(), DocumentStatus.APPROVED.getLabel());
                statusMap.put(DocumentStatus.EFFECTIVE.name(), DocumentStatus.EFFECTIVE.getLabel());
                model.addAttribute("statusMap", statusMap);
            } else if(action == SOPAction.edit) {
                QDocumentVersion qDocumentVersion = QDocumentVersion.documentVersion;
                BooleanBuilder builder = new BooleanBuilder();
                builder.and(qDocumentVersion.parentVersion.id.eq(id));
                Iterable<DocumentVersion> childDocs = documentVersionService.findAll(builder);//나의 자식이 있는지..
                model.addAttribute("hasChild", ObjectUtils.isEmpty(childDocs) == false);
            } else if(action == SOPAction.remove) {
                //TODO 삭제 $%#%#$%처리!!

            }
        }

        if(type == DocumentType.SOP) {
            model.addAttribute("CategoryList", categoryService.categoryMap());
        } else {
            model.addAttribute("sopList", documentService.getSortedMap(status));

            TreeMap<String, String> languageMap = new TreeMap<>();
            languageMap.put(DocumentLanguage.KOR.name(), DocumentLanguage.KOR.getLabel());
            languageMap.put(DocumentLanguage.ENG.name(), DocumentLanguage.ENG.getLabel());
            model.addAttribute("languageMap", languageMap);
        }

        //2021-03-17 YSH :: SOP/RF Prefix 값 전달
        model.addAttribute("sopPrefix", sopPrefix);
        model.addAttribute("formPrefix", formPrefix);
        model.addAttribute("formName", formName);
        return "admin/sop/edit";
    }

    @Transactional
    @PostMapping({"/management/{stringStatus}/new", "/management/{stringStatus}/{id}/{action}"})
    public String edit(@PathVariable("type") DocumentType type,
                       @PathVariable("stringStatus") String stringStatus, @PathVariable(value = "id", required = false) String id, @ModelAttribute("documentVersion") DocumentVersion documentVersion,
                       BindingResult bindingResult, SessionStatus sessionStatus,
                       @PathVariable(value = "action", required = false) SOPAction action,
                       HttpServletRequest request,
                       RedirectAttributes attributes, Model model) {

        documentVersionValidator.validate(documentVersion, bindingResult);

//        sop.setStatus(DocumentStatus.valueOf(stringStatus.toUpperCase()));
        if(bindingResult.hasErrors()) {
            log.debug("--- Document Version Validate ---\n{}", bindingResult.getAllErrors());

            //2021-03-17 YSH :: SOP/RF Prefix 값 전달
            model.addAttribute("sopPrefix", sopPrefix);
            model.addAttribute("formPrefix", formPrefix);
            model.addAttribute("formName", formName);

            return "admin/sop/edit";
        }

        try {

//            //SOP Version - document id Duplicate Check
//            BooleanBuilder builder = new BooleanBuilder();
//            QDocumentVersion qDocumentVersion = QDocumentVersion.documentVersion;
//            builder.and(qDocumeCurrent SOPntVersion.version.eq(documentVersion.getVersion()));
//            builder.and(qDocumentVersion.document.id.eq(documentVersion.getId()));
//            if(!StringUtils.isEmpty(documentVersion.getId())) {
//                builder.and(qDocumentVersion.id.ne(documentVersion.getId()));
//            }
//
//            if(documentVersionRepository.findOne(builder).isPresent()) {
//                attributes.addFlashAttribute("messageType", "danger");
//                attributes.addFlashAttribute("message", "이미 등록된 버전입니다. (기존 SOP 정보를 확인해주세요.)");
//                return "redirect:/admin/{type}/management/{stringStatus}";
//            }


            if (ObjectUtils.isEmpty(action)) {
                documentVersionService.save(documentVersion);
            } else {
                if (action == SOPAction.edit) {
                    documentVersionService.save(documentVersion);
                } else if (action == SOPAction.revision) {
                    log.info("==> Revision : {}", id);
                    documentVersionService.revision(documentVersion);
                }
            }

            if(documentVersion.getNotification()) {
                documentVersionService.documentNotification(action, documentVersion);
            }

            sessionStatus.setComplete();
            return "redirect:/admin/{type}/management/{stringStatus}";
        } catch (Exception error) {
            attributes.addFlashAttribute("message", "문서 정보 등록 중 오류가 발생 하였습니다. 관리자에게 문의해 주세요.");
            log.error("==> SOP/RF 업로드 중 오류 발생 : {}", error);
            return "redirect:/admin/{type}/management/{stringStatus}";
        }
    }

    @Transactional
    @PutMapping("/management/{stringStatus}/{id}/retirement")
    public String retirement(@PathVariable("type") DocumentType type,
                             @PathVariable("stringStatus") String stringStatus,
                             @PathVariable(value = "id") String id,
                             @RequestParam(value = "retirementDate", required = false) String strRetirementDate,
                             RedirectAttributes attributes) throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        Date retirementDate = StringUtils.isEmpty(strRetirementDate)?null:df.parse(strRetirementDate);
        DocumentVersion savedDocumentVersion = documentVersionService.retirement(id, retirementDate);

        if(ObjectUtils.isEmpty(retirementDate)) {
            attributes.addFlashAttribute("message", savedDocumentVersion.getDocument().getDocId() +" " + savedDocumentVersion.getVersion() + " 이 Retirement 처리 되었습니다.");
        } else {
            attributes.addFlashAttribute("message", savedDocumentVersion.getDocument().getDocId() +" " + savedDocumentVersion.getVersion() + " 이 Retirement Date가 지정되었습니다. (" + strRetirementDate + ")");
        }

        //        attributes.addFlashAttribute("message", savedDocumentVersion.getDocument().getDocId() +" " + savedDocumentVersion.getVersion() + " 이 Superseded 처리 되었습니다.");

        return "redirect:/admin/{type}/management/{stringStatus}";
    }

    @Transactional
    @DeleteMapping("/management/{stringStatus}/{id}/remove")
    public String remove(@PathVariable("type") DocumentType type,
                       @PathVariable("stringStatus") String stringStatus, @PathVariable(value = "id") String id,
                       @CurrentUser Account user,
                       RedirectAttributes attributes) {

        log.info("@Document 삭제 요청 Doc Ver Id : {}, User : {}", id, user.getUsername());
        DocumentStatus status = DocumentStatus.valueOf(stringStatus.toUpperCase());
        if(status == DocumentStatus.DEVELOPMENT || status == DocumentStatus.REVISION) {
            documentVersionService.remove(id);
            attributes.addFlashAttribute("message", "삭제 처리 되었습니다.");
        } else {
            attributes.addFlashAttribute("message", "[Development, Revision] 상태의 문서만 삭제 할 수 있습니다.");
        }

        return "redirect:/admin/{type}/management/{stringStatus}";
    }

    @Transactional
    @PutMapping("/management/{stringStatus}/{id}/approved")
    public String approved(@PathVariable("type") DocumentType type,
                             @PathVariable("stringStatus") String stringStatus,
                             @PathVariable(value = "id") String id,
                             RedirectAttributes attributes) {

        DocumentVersion documentVersion = documentVersionService.approved(id);
        attributes.addFlashAttribute("message", documentVersion.getDocument().getDocId() +" " + documentVersion.getVersion() + " 이 Approved 처리 되었습니다.");
        return "redirect:/admin/{type}/management/{stringStatus}";
    }

    @GetMapping("/management/{stringStatus}/{docVerId}/quiz")
    public String quiz(@PathVariable("type") DocumentType type,
                             @PathVariable("docVerId") String docVerId,
                             Model model) {

        model.addAttribute("type", type);
        DocumentVersion documentVersion = documentVersionService.findById(docVerId);
        model.addAttribute("documentVersion", documentVersion);

        Quiz quiz = null;
        if(!StringUtils.isEmpty(documentVersion.getQuiz())) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                quiz = objectMapper.readValue(documentVersion.getQuiz(), Quiz.class);
            } catch (Exception error) {
                log.error("error : ", error);
            }
        } else {
            quiz = new Quiz();
            List<QuizQuestion> questions = new ArrayList<>();

            for (int i = 0; i < 5; i++) {
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

        return "admin/sop/quiz";
    }

    @PostMapping("/management/{stringStatus}/{docVerId}/quiz")
    public String saveQuiz(@PathVariable("type") DocumentType type,
                           @PathVariable("docVerId") String docVerId,
                           @ModelAttribute("quiz") Quiz quiz,
                           BindingResult result, SessionStatus status,
                           HttpServletRequest request,
                           RedirectAttributes attributes) throws Exception {

        boolean isRemove = WebUtils.hasSubmitParameter(request, "remove");
        if(isRemove) {
            String removeValue = ServletRequestUtils.getStringParameter(request, "remove");
            String[] arr = removeValue.split("\\.");
            quiz.getQuizQuestions().get(Integer.parseInt(arr[0])).getAnswers().remove(Integer.parseInt(arr[1]));
            return "admin/sop/quiz";
        }

        quizValidator.validate(quiz, result);

        if(result.hasErrors()) {
            return "admin/sop/quiz";
        }

        DocumentVersion documentVersion = documentVersionService.saveQuiz(docVerId, quiz);
        status.setComplete();

        attributes.addFlashAttribute("message", documentVersion.getDocument().getDocId() + " " + documentVersion.getVersion() + "에 퀴즈 정보가 수정 되었습니다.");
        return "redirect:/admin/SOP/management/{stringStatus}";
    }

    @GetMapping("/management/{stringStatus}/{docVerId}/quiz/upload")
    public String quizUpload(@PathVariable("type") DocumentType type,
                       @PathVariable("docVerId") String docVerId,
                       Model model) {

//        model.addAttribute("docVerId", docVerId);
        DocumentVersion documentVersion = documentVersionService.findById(docVerId);
        model.addAttribute("documentVersion", documentVersion);
        model.addAttribute("docId", documentVersion.getDocument().getDocId());

        return "admin/sop/quiz-upload";
    }

    @PutMapping("/management/{stringStatus}/{docVerId}/quiz")
    public String quizUploaded(@PathVariable("type") DocumentType type,
                       @PathVariable("stringStatus") String stringStatus,
                       @PathVariable("docVerId") String docVerId,
                       @RequestParam("quizTemplate") MultipartFile multipartFile,
                       Model model, RedirectAttributes attributes) {

        model.addAttribute("docVerId", docVerId);
        model.addAttribute("type", type);
        try {
            Quiz quiz = new Quiz();
            List<QuizQuestion> questions = new ArrayList<>();
            InputStream is = multipartFile.getInputStream();
            XSSFWorkbook workbook = new XSSFWorkbook(is);
            XSSFSheet sheet = workbook.getSheetAt(0);

            XSSFRow documentIdRow = sheet.getRow(2);
            documentIdRow.getCell(1).setCellType(CellType.STRING);
            String documentId = documentIdRow.getCell(1).getStringCellValue();
            XSSFRow versionRow = sheet.getRow(3);

            versionRow.getCell(1).setCellType(CellType.STRING);
            String version = versionRow.getCell(1).getStringCellValue();
            log.debug("documentId : {}, version : {}", documentId, version);

            DocumentVersion documentVersion = documentVersionService.findById(docVerId);
            if(!documentVersion.getDocument().getDocId().equals(documentId) || !documentVersion.getVersion().equals(version)) {
                attributes.addFlashAttribute("message", "Template에 DocumentId/Version 정보와 SOP 정보와 일치하지 않습니다.");
                return "redirect:/admin/SOP/management/{stringStatus}";
            }

            XSSFRow qRow = sheet.getRow(7);
            XSSFRow a1Row = sheet.getRow(8);
            XSSFRow a2Row = sheet.getRow(9);
            XSSFRow a3Row = sheet.getRow(10);
            XSSFRow a4Row = sheet.getRow(11);
            XSSFRow a5Row = sheet.getRow(12);
            XSSFRow correctRow = sheet.getRow(13);

            for(int i = 1; i <= 5; i ++) {
                qRow.getCell(i).setCellType(CellType.STRING);
                a1Row.getCell(i).setCellType(CellType.STRING);
                a2Row.getCell(i).setCellType(CellType.STRING);
                a3Row.getCell(i).setCellType(CellType.STRING);
                a4Row.getCell(i).setCellType(CellType.STRING);
                a5Row.getCell(i).setCellType(CellType.STRING);

                String q = qRow.getCell(i).getStringCellValue();
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

            quiz.setQuizQuestions(questions);
            model.addAttribute("quiz", quiz);
            return "admin/sop/quiz";
        } catch (Exception error) {
            log.error("error : {}", error.getMessage());
            return "redirect:/admin/SOP/management/{stringStatus}";
        }
//        model.addAttribute("documentVersion", documentVersion);


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

    @GetMapping("/management/{stringStatus}/{docVerId}/quiz/test")
    public String quizTest(@PathVariable("type") DocumentType type,
                       @PathVariable("docVerId") String docVerId,
                       Model model) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        Quiz quiz = objectMapper.readValue(documentVersionService.findById(docVerId).getQuiz(), Quiz.class);
        Collections.shuffle(quiz.getQuizQuestions());
        for(QuizQuestion quizQuestion : quiz.getQuizQuestions()) {
            Collections.shuffle(quizQuestion.getAnswers());
        }
        model.addAttribute("quiz", quiz);

        return "admin/sop/quiz-test";
    }

    @GetMapping("/management/retirement")
    public String retirement(@PathVariable("type") DocumentType documentType,
                             @PageableDefault(sort = {"retirementDate", "id"}, direction = Sort.Direction.DESC, size = 15) Pageable pageable,
                             Model model) {
//        approvalService.find
//        QRetirementDocument qRetirementDocument = QRetirementDocument.retirementDocument;
//        BooleanBuilder builder = new BooleanBuilder();
//        builder.and(qRetirementDocument.retirementApprovalForm.approval.status.eq(ApprovalStatus.approved));
//        builder.and(qRetirementDocument.documentType.eq(documentType));
//        builder.and(qRetirementDocument.retired.eq(false));

        QDocumentVersion qDocumentVersion = QDocumentVersion.documentVersion;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qDocumentVersion.status.eq(DocumentStatus.RETIREMENT));
        builder.and(qDocumentVersion.retirement.eq(true));

        model.addAttribute("retirement", documentVersionRepository.findAll(builder, pageable));

        return "admin/sop/retirement";
    }

//    @PostMapping("/management/retirement/setRetirementDate")
//    @ResponseBody
//    public Map<String, Object> retirementDate(@PathVariable("type") DocumentType documentType,
//                             RetirementDocument retirementDocument) {
//        Map<String, Object> res = new HashMap<>();
//        Optional<RetirementDocument> optionalRetirementDocument = retirementDocumentRepository.findById(retirementDocument.getId());
//        if(optionalRetirementDocument.isPresent()) {
//            RetirementDocument doc = optionalRetirementDocument.get();
//            doc.setRetirementDate(retirementDocument.getRetirementDate());
//            retirementDocumentRepository.save(doc);
//
//            res.put("result", "success");
//            res.put("id", doc.getId());
//        } else {
//            res.put("result", "fail");
//        }
//        return res;
//    }
}
