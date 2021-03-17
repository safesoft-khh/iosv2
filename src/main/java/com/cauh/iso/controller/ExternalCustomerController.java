package com.cauh.iso.controller;

import com.cauh.common.entity.Account;
import com.cauh.common.entity.QAccount;
import com.cauh.common.entity.Signature;
import com.cauh.common.repository.SignatureRepository;
import com.cauh.common.repository.UserRepository;
import com.cauh.common.security.annotation.CurrentUser;
import com.cauh.iso.component.DocumentViewer;
import com.cauh.iso.domain.*;
import com.cauh.iso.domain.constant.ApprovalLineType;
import com.cauh.iso.domain.constant.DocumentStatus;
import com.cauh.iso.domain.constant.DocumentType;
import com.cauh.iso.domain.constant.TrainingStatus;
import com.cauh.iso.domain.report.ExternalCustomer;
import com.cauh.iso.repository.ExternalCustomerRepository;
import com.cauh.iso.repository.ISOTrainingLogRepository;
import com.cauh.iso.repository.TrainingLogRepository;
import com.cauh.iso.service.CategoryService;
import com.cauh.iso.service.FileStorageService;
import com.cauh.iso.service.ISOCertificationService;
import com.cauh.iso.service.NoticeService;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.docx4j.wml.P;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
@RequiredArgsConstructor
@SessionAttributes({"CategoryList"})
@Slf4j
public class ExternalCustomerController {

    private final NoticeService noticeService;
    private final ISOCertificationService isoCertificationService;

    private final TrainingLogRepository trainingLogRepository;
    private final ISOTrainingLogRepository isoTrainingLogRepository;

    private final FileStorageService fileStorageService;
    private final ExternalCustomerRepository externalCustomerRepository;
    private final UserRepository userRepository;
    private final SignatureRepository signatureRepository;
    private final DocumentViewer documentViewer;
    private final CategoryService categoryService;

//    @Value("${file.binder-dir}")
//    private String bindPath;

    @Value("${site.company-title}")
    private String siteCompanyTitle;

    @GetMapping("/external/notice")
    public String externNoticeList(@PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC, size = 15) Pageable pageable, Model model){
        QNotice qNotice = QNotice.notice;

        //공지사항 리스트
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qNotice.deleted.eq(false));
        model.addAttribute("noticeList", noticeService.getList(builder, pageable));

        //공지사항(상단공지)
        Date today = new Date(System.currentTimeMillis());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        builder.and(qNotice.topViewEndDate.goe(Date.valueOf(format.format(today))));
        model.addAttribute("topNoticeList", noticeService.getTopNotices(builder));

        return "home/notice/external/list";
    }

    @GetMapping("/external/notice/{noticeId}")
    public String externNoticeView(@PathVariable("noticeId") Integer noticeId, Model model, RedirectAttributes attributes) {
        Optional<Notice> notice = noticeService.getNotice(noticeId);
        if(notice.isPresent()) {
            model.addAttribute("notice", notice.get());
        } else {
            attributes.addFlashAttribute("message", "존재하지 않는 게시물 입니다.");
            return "redirect:/external/notice";
        }
        return "home/notice/external/view";
    }

    @GetMapping("/external/notice/{id}/downloadFile/{attachFileId:.+}")
    public ResponseEntity<Resource> externDownloadFile(@PathVariable("id") Integer id, @PathVariable("attachFileId") String attachFileId, HttpServletRequest request) {
        // Load file as Resource
        Optional<NoticeAttachFile> optionalAttachFile = noticeService.getAttachFile(attachFileId);
        if(optionalAttachFile.isPresent()) {
            NoticeAttachFile attachFile = optionalAttachFile.get();
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


    @GetMapping("/external/certifications")
    public String externCertificationsList(@PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC, size = 15) Pageable pageable, Model model){
        QISOCertification qISOCertification = QISOCertification.iSOCertification;

        //인증현황 리스트
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qISOCertification.deleted.eq(false));
        model.addAttribute("certificationList", isoCertificationService.getList(builder, pageable));

        return "home/certifications/list";
    }

    @GetMapping({
            "/external/sop/{status}",
            "/external/sop/{status}/{categoryId}",
            "/external/sop/{status}/{categoryId}/{sopId}"
    })
    public String sopList(@PathVariable("status") String stringStatus,
                          @PathVariable(value = "categoryId", required = false) String categoryId,
                          @PathVariable(value = "sopId", required = false) String sopId,
                          @CurrentUser Account user,
                          Model model) {
        log.info("@externalCustomerId : {}", user.getExternalCustomerId());
        Optional<ExternalCustomer> optionalExternalCustomer = externalCustomerRepository.findById(user.getExternalCustomerId());
        DocumentStatus status = DocumentStatus.valueOf(stringStatus.toUpperCase());
        if(optionalExternalCustomer.isPresent()) {
            ExternalCustomer externalCustomer = optionalExternalCustomer.get();
            List<DisclosureSOP> disclosureSOPList = externalCustomerRepository.getDocumentList(externalCustomer.getSopDisclosureRequestForm().getId(), status, categoryId, sopId);

            log.info("@SOP DB 조회 데이터 수 : {}", disclosureSOPList.size());
            /**
             * Category 정보 설정
             */
            if(!ObjectUtils.isEmpty(disclosureSOPList) && StringUtils.isEmpty(categoryId)) {
                model.addAttribute("CategoryList", StreamSupport.stream(disclosureSOPList.spliterator(), false)
                        .map(v -> v.getDocumentVersion().getDocument().getType() == DocumentType.SOP ? v.getDocument().getCategory() : v.getSopDocument().getCategory())
                        .distinct()
                        .sorted(Comparator.comparing(Category::getId))
                        .collect(Collectors.toList()));
            }

            /**
             * 최초 status 로만 필터가 된 경우
             */
            if(StringUtils.isEmpty(categoryId) && StringUtils.isEmpty(sopId)) {
                log.info("@Category/SOP 선택된 정보 없음");
                disclosureSOPList = disclosureSOPList.stream().map(s -> new DisclosureSOP(null, null,
                        s.getDocument().getType() == DocumentType.SOP ? s.getDocument() : s.getSopDocument()))
                        .distinct()
                        .sorted(Comparator.comparing(c -> c.getSopDocument().getDocId()))
                        .collect(Collectors.toList());
            }

            if(!StringUtils.isEmpty(categoryId) && StringUtils.isEmpty(sopId)) {/**category 가 선택된 경우*/
                log.info("@Category Id 선택됨 : {}", categoryId);
                disclosureSOPList = disclosureSOPList.stream()
                        .filter(s -> s.getDocument().getType() == DocumentType.SOP ?
                                s.getDocument().getCategory().getId().equals(categoryId) : s.getSopDocument().getCategory().getId().equals(categoryId))
                        .map(s -> new DisclosureSOP(null, null, s.getDocument().getType() == DocumentType.SOP ?
                                s.getDocument() : s.getSopDocument()))
                        .distinct()
                        .sorted(Comparator.comparing(s -> s.getSopDocument().getDocId()))
                        .collect(Collectors.toList());
            } else if(!StringUtils.isEmpty(sopId)) {/** SOP 가 선택된 경우 */
                log.info("@SOP 선택 됨 : {}", sopId);
                disclosureSOPList = disclosureSOPList.stream()
//                        .filter(s -> s.getDocument().getType() == DocumentType.SOP ?
//                                s.getDocument().getCategory().getId().equals(categoryId) : s.getSopDocument().getCategory().getId().equals(categoryId))
//                        .map(s -> new DisclosureSOP(s.getDocumentVersion(), s.getDocument(), s.getDocument().getType() == DocumentType.SOP ?
//                                s.getDocument() : s.getSopDocument()))
//                        .filter(s -> s.getSopDocument().getId().equals(sopId))
//                        .distinct()
                        .sorted(Comparator.comparing(s -> s.getDocument().getDocId()))
                        .collect(Collectors.toList());
            }

            log.debug("sopList = {}", disclosureSOPList);
            model.addAttribute("sopList", disclosureSOPList);
            model.addAttribute("categoryId", categoryId);

            if (!StringUtils.isEmpty(categoryId)) {
                model.addAttribute("category", categoryService.findById(categoryId));
            }

            model.addAttribute("sopId", sopId);
            model.addAttribute("status", status);

            return "sop/external-list";
        } else {
            throw new RuntimeException("현재 사용자는 Access 권한이 없습니다.");
        }
    }


//    @GetMapping("/ajax/log/{username}")
//    public void digitalBinder(@PathVariable("username") String username, HttpServletResponse response) throws Exception {
//        Optional<Signature> optionalSignature = signatureRepository.findById(username);
//        OutputStream os = response.getOutputStream();
//        if(optionalSignature.isPresent()) {
//            try {
//                Signature signature = optionalSignature.get();
//
//                Path fileStorageLocation = Paths.get(bindPath).toAbsolutePath().normalize();
//                Path filePath = fileStorageLocation.resolve(signature.getBinderFileName()).normalize();
//                Resource resource = new UrlResource(filePath.toUri());
//
//                ByteArrayOutputStream html = new ByteArrayOutputStream();
//                documentViewer.toHTML("pdf", resource.getInputStream(), html);
//                os.write(html.toByteArray());
//            } catch (Exception e) {
//                os.write("File Not Found.".getBytes());
//            }
//
//        } else {
//            os.write("File Not Found.".getBytes());
//        }
//
//        os.flush();
//        os.close();
//    }

    @GetMapping("/external/log/iso")
    public String externISOTrainingLog(@PageableDefault(sort = {"completeDate"}, direction = Sort.Direction.DESC, size = 15) Pageable pageable,
                                 @RequestParam(value = "userId", required = false) Integer userId,
                                 @CurrentUser Account user, Model model, RedirectAttributes attributes){
        if(!ObjectUtils.isEmpty(user.getDisclosureISOUsers())) {
            QAccount qUser = QAccount.account;
            BooleanBuilder builder = new BooleanBuilder();
            builder.and(qUser.id.in(user.getDisclosureISOUsers()));
            model.addAttribute("userList", userRepository.findAll(builder));
        }

        if(!StringUtils.isEmpty(userId)) {
            Optional<Account> optionalUser = userRepository.findById(userId);

            if(optionalUser.isPresent()) {
                Account searchUser = optionalUser.get();
                model.addAttribute("logUser", searchUser);
                log.debug("Log User : {}", searchUser);

                if(user.getDisclosureISOUsers().contains(userId)) {
                    QISOTrainingLog qisoTrainingLog = QISOTrainingLog.iSOTrainingLog;

                    BooleanBuilder builder = new BooleanBuilder();
                    builder.and(qisoTrainingLog.user.id.eq(searchUser.getId()));
                    builder.and(qisoTrainingLog.status.eq(TrainingStatus.COMPLETED));

                    model.addAttribute("isoTrainingLog", isoTrainingLogRepository.findAll(builder, pageable));

                    //2021-03-17 YSH :: 회사명 공통작업
                    model.addAttribute("siteCompanyTitle", siteCompanyTitle);

                } else {
                    attributes.addFlashAttribute("messageType", "warning");
                    attributes.addFlashAttribute("message", "열람할 수 없는 User입니다.");
                    return "redirect:/external/log/iso";
                }
            } else {
                attributes.addFlashAttribute("messageType", "danger");
                attributes.addFlashAttribute("message", "존재하지 않는 User ID입니다.");
                return "redirect:/external/log/iso";
            }
        } else {
            // User Id가 없을 경우 결과 없는 배열 반환.
            model.addAttribute("isoTrainingLog", new PageImpl<ISOTrainingLog>(new ArrayList<>()));
        }

        return "externalCustomer/isoTrainingLog";
    }

    @GetMapping("/external/log/sop")
    public String sopTrainingLog(@PageableDefault(sort = {"completeDate"}, direction = Sort.Direction.DESC, size = 15) Pageable pageable,
                                 @RequestParam(value = "userId", required = false) Integer userId,
                                 @CurrentUser Account user, Model model, RedirectAttributes attributes){
        if(!ObjectUtils.isEmpty(user.getDisclosureSOPUsers())) {
            QAccount qUser = QAccount.account;
            BooleanBuilder builder = new BooleanBuilder();
            builder.and(qUser.id.in(user.getDisclosureSOPUsers()));
            model.addAttribute("userList", userRepository.findAll(builder));
        }

        if(!StringUtils.isEmpty(userId)) {
            Optional<Account> optionalUser = userRepository.findById(userId);

            if(optionalUser.isPresent()) {
                Account searchUser = optionalUser.get();
                model.addAttribute("logUser", searchUser);
                log.debug("Log User : {}", searchUser);

                if(user.getDisclosureSOPUsers().contains(userId)) {
                    QTrainingLog qTrainingLog = QTrainingLog.trainingLog;

                    BooleanBuilder builder = new BooleanBuilder();
                    builder.and(qTrainingLog.user.id.eq(searchUser.getId()));
                    builder.and(qTrainingLog.status.eq(TrainingStatus.COMPLETED));

                    model.addAttribute("trainingLog", trainingLogRepository.findAll(builder, pageable));

                    //2021-03-17 YSH :: 회사명 공통작업
                    model.addAttribute("siteCompanyTitle", siteCompanyTitle);

                } else {
                    attributes.addFlashAttribute("messageType", "warning");
                    attributes.addFlashAttribute("message", "열람할 수 없는 User입니다.");
                    return "redirect:/external/log/sop";
                }
            } else {
                attributes.addFlashAttribute("messageType", "danger");
                attributes.addFlashAttribute("message", "존재하지 않는 User ID입니다.");
                return "redirect:/external/log/sop";
            }
        } else {
            // User Id가 없을 경우 결과 없는 배열 반환.
            model.addAttribute("trainingLog", new PageImpl<TrainingLog>(new ArrayList<>()));
        }

        return "externalCustomer/sopTrainingLog";
    }

//    @GetMapping("/external/log/deviation")
//    public String deviationTrackingLog(@CurrentUser Account user, Model model){
//        if(ObjectUtils.isEmpty(user.getDisclosureUsers())) {
//            QAccount qUser = QAccount.account;
//            BooleanBuilder builder = new BooleanBuilder();
//            builder.and(qUser.username.in(user.getDisclosureUsers()));
//            model.addAttribute("users", userRepository.findAll(builder));
//        }
//
//        return "externalCustomer/deviationTrainingLog";
//    }


}
