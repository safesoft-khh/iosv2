package com.cauh.iso.admin.controller;

import com.cauh.common.entity.*;
import com.cauh.common.entity.constant.UserStatus;
import com.cauh.common.repository.SignatureRepository;
import com.cauh.common.repository.UserRepository;
import com.cauh.common.security.annotation.CurrentUser;
import com.cauh.common.service.UserService;
import com.cauh.iso.admin.service.DepartmentService;
import com.cauh.iso.domain.*;
import com.cauh.iso.domain.constant.DocumentAccessType;
import com.cauh.iso.domain.constant.ISOType;
import com.cauh.iso.domain.constant.TrainingLogType;
import com.cauh.iso.domain.constant.TrainingStatus;
import com.cauh.iso.repository.ISOTrainingCertificationInfoRepository;
import com.cauh.iso.repository.TrainingMatrixRepository;
import com.cauh.iso.service.ISOOfflineTrainingService;
import com.cauh.iso.service.ISOService;
import com.cauh.iso.service.ISOTrainingPeriodService;
import com.cauh.iso.service.TrainingAccessLogService;
import com.cauh.iso.utils.DateUtils;
import com.cauh.iso.validator.ISOCertificateInfoValidator;
import com.cauh.iso.validator.ISOTrainingPeriodValidator;
import com.cauh.iso.xdocreport.ISOTrainingCertificationService;
import com.cauh.iso.xdocreport.IndexReportService;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
@RequestMapping({"/admin", "/ajax/admin"})
@RequiredArgsConstructor
@SessionAttributes({"isoTrainingPeriod", "infoList", "isoMap"})
@Slf4j
public class AdminISOTrainingController {

    private final ISOOfflineTrainingService isoOfflineTrainingService;
    private final ISOTrainingPeriodService isoTrainingPeriodService;
    private final ISOService isoService;
    private final ISOTrainingPeriodValidator isoTrainingPeriodValidator;
    private final ISOCertificateInfoValidator isoCertificateInfoValidator;
    private final ISOTrainingCertificationService isoTrainingCertificationService;
    private final TrainingMatrixRepository trainingMatrixRepository;
    private final ISOTrainingCertificationInfoRepository isoTrainingCertificationInfoRepository;
    private final TrainingAccessLogService trainingAccessLogService;
    private final DepartmentService departmentService;
    private final UserRepository userRepository;
    private final SignatureRepository signatureRepository;

    private final UserService userService;

    @Value("${cert.header}")
    String certHeader;

    @GetMapping("/training/iso/offline-training")
    public String offlineTraining(@PageableDefault(size = 25, sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable, Model model) {
        model.addAttribute("isoOfflineTraining", isoOfflineTrainingService.findAll(pageable));
        return "admin/training/offline/iso/list";
    }

    @GetMapping("/training/iso/offline-training/{id}")
    public String offlineTraining(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("isoOfflineTraining", isoOfflineTrainingService.findById(id).get());
        return "admin/training/offline/iso/view";
    }

    /**
     * Training 이력 반영 및 반영 적용 메일 알림
     * @param id
     * @param attributes
     * @return
     */
    @PostMapping("/training/iso/offline-training/{id}")
    public String offlineTraining(@PathVariable("id") Integer id, RedirectAttributes attributes) {
        ISOOfflineTraining savedOfflineTraining = isoOfflineTrainingService.isoOfflineTrainingApply(id);
        isoOfflineTrainingService.sendApplyEmail(savedOfflineTraining);

        attributes.addFlashAttribute("message", "트레이닝 이력이 반영 되었습니다.");
        return "redirect:/admin/training/iso/offline-training";
    }

    @DeleteMapping("/training/iso/offline-training/{id}")
    public String deleteOfflineTraining(@PathVariable("id") Integer id, RedirectAttributes attributes) {
        isoOfflineTrainingService.delete(id);

        attributes.addFlashAttribute("message", "Offline Training 요청 정보가 삭제 되었습니다.");
        return "redirect:/admin/training/iso/offline-training";
    }

    @GetMapping({"/training/iso/trainingLog", "/training/iso/trainingLog/{complete}"})
    public String teamDeptTrainingLog(@PageableDefault(size = 25) Pageable pageable,
                                       @PathVariable(value = "complete", required = false) String isComplete,
                                       @RequestParam(value = "deptId", required = false) Integer deptId,
                                       @RequestParam(value = "teamId", required = false) Integer teamId,
                                       @RequestParam(value = "userId", required = false) Integer userId,
                                       @RequestParam(value = "title", required = false) String title,
                                       Model model) {
        //부서 목록
        model.addAttribute("deptList", departmentService.getParentDepartment());
        Department department = null;

        if(!ObjectUtils.isEmpty(deptId)) {
            department = new Department(deptId);
            model.addAttribute("teamList", departmentService.getChildDepartment(department));

            if (!ObjectUtils.isEmpty(teamId)) {//팀아이뒤가 있을 경우
                QAccount qUser = QAccount.account;
                department = new Department(teamId);
                BooleanBuilder userBuilder = new BooleanBuilder();
                userBuilder.and(qUser.department.eq(department));
                userBuilder.and(qUser.userStatus.eq(UserStatus.ACTIVE));
                model.addAttribute("userList", userRepository.findAll(userBuilder, qUser.engName.asc()));
            }
            else {//부서로 검색
                QAccount qUser = QAccount.account;
                BooleanBuilder userBuilder = new BooleanBuilder();
                userBuilder.and(qUser.department.eq(department));
                userBuilder.and(qUser.userStatus.eq(UserStatus.ACTIVE));
                userBuilder.or(qUser.department.in(departmentService.getChildDepartment(department)));
                model.addAttribute("userList", userRepository.findAll(userBuilder, qUser.engName.asc()));
            }
        }

        BooleanBuilder completeStatus = new BooleanBuilder();
        QISOTrainingLog qisoTrainingLog = QISOTrainingLog.iSOTrainingLog;

        if(StringUtils.isEmpty(isComplete)) {
            completeStatus.and(qisoTrainingLog.status.notIn(TrainingStatus.COMPLETED).or(qisoTrainingLog.status.isNull()));
        } else if(!StringUtils.isEmpty(isComplete) && isComplete.equals("completed")) {
            completeStatus.and(qisoTrainingLog.status.in(TrainingStatus.COMPLETED));
        } else {
            return "redirect:/admin/training/iso/trainingLog";
        }

        model.addAttribute("isoTrainingLog", trainingMatrixRepository.getISOTrainingList(department, userId, ISOType.ISO_14155, title, pageable, completeStatus));
        return "iso/training/teamDeptTrainingLog2";
    }

    @PostMapping({"/training/iso/trainingLog", "/training/iso/trainingLog/{complete}"})
    @Transactional
    public void downloadTeamDeptTrainingLog(@PathVariable(value = "complete", required = false) String isComplete,
                                            @RequestParam(value = "deptId", required = false) Integer deptId,
                                            @RequestParam(value = "teamId", required = false) Integer teamId,
                                            @RequestParam(value = "userId", required = false) Integer userId,
                                            @RequestParam(value = "title", required = false) String title,
                                            @CurrentUser Account user,
                                            HttpServletRequest request, HttpServletResponse response) throws Exception {

        //team, user 정보는 있는데 부서정보가 없는 경우
        if((!ObjectUtils.isEmpty(teamId) || !ObjectUtils.isEmpty(userId)) && ObjectUtils.isEmpty(deptId)) {
            log.error("부서 정보가 잘못되었습니다. deptId : {}, teamId = {}, userId = {}", deptId, teamId, userId);
            return;
        }

        Department department = null;

        //팀정보가 있으면 팀, 없으면 부서정보.
        if(!ObjectUtils.isEmpty(deptId)) {
            if(!ObjectUtils.isEmpty(teamId)) {
                department = departmentService.getDepartmentById(teamId);
            } else {
                department = departmentService.getDepartmentById(deptId);
            }
        }

        BooleanBuilder completeStatus = new BooleanBuilder();
        QISOTrainingLog qisoTrainingLog = QISOTrainingLog.iSOTrainingLog;

        TrainingLogType trainingLogType = null;

        if(StringUtils.isEmpty(isComplete)) {
            completeStatus.and(qisoTrainingLog.status.notIn(TrainingStatus.COMPLETED).or(qisoTrainingLog.status.isNull()));
            trainingLogType = TrainingLogType.ISO_ADMIN_NOT_COMPLETE_LOG;
        } else if(!StringUtils.isEmpty(isComplete) && isComplete.equals("completed")) {
            completeStatus.and(qisoTrainingLog.status.in(TrainingStatus.COMPLETED));
            trainingLogType = TrainingLogType.ISO_ADMIN_COMPLETE_LOG;
        } else {
            log.error("Training Export Error : 존재하지 않는 URI입니다 : {}", request.getRequestURI());
            return;
        }

        List<MyTraining> trainingList = trainingMatrixRepository.getDownloadISOTrainingList(department, userId, ISOType.ISO_14155, title, completeStatus);
        InputStream is = IndexReportService.class.getResourceAsStream("Admin_ISO_TrainingLog.xlsx");

        Context context = new Context();
        context.putVar("trainings", trainingList);
        response.setHeader("Content-Disposition", "attachment; filename=\"ISO_TrainingLog("+ DateUtils.format(new Date(), "yyyyMMdd")+").xlsx\"");
        JxlsHelper.getInstance().processTemplate(is, response.getOutputStream(), context);

        //Training AccessLog 저장
        Optional<TrainingAccessLog> savedLog = trainingAccessLogService.save(user, trainingLogType, DocumentAccessType.DOWNLOAD);
        log.info("@Download Training Log 기록 : {}", savedLog.get().getId());

    }

    @GetMapping("/training/iso/training-certification")
    public String trainingCertification(Model model){

        return "admin/iso/certList";
    }

    @GetMapping("/training/iso/training-certification/info")
    public String trainingCertificateInfo(@RequestParam(value = "action", defaultValue = "list") String action,
                                               @RequestParam(value = "id", required = false) Integer id, Model model){

        Iterable<ISOTrainingCertificationInfo> infoList = isoTrainingCertificationInfoRepository.findAll();

        model.addAttribute("infoList", infoList);
        model.addAttribute("userMap", userService.getUserMap());

        if("new".equals(action)){
            model.addAttribute("info", new ISOTrainingCertificationInfo());
        } else if ("edit".equals(action)) {
            ISOTrainingCertificationInfo info = isoTrainingCertificationInfoRepository.findById(id).get();
            model.addAttribute("info", info);
        }

        model.addAttribute("action", action);
        model.addAttribute("id", id);

        return "admin/iso/certificateInfoList";
    }

    @GetMapping("/training/iso/trainingCertList")
    @ResponseBody
    public List<ISOTrainingCertificationDTO> ajaxTrainingCertification() {
        QISOTrainingCertification qisoTrainingCertification = QISOTrainingCertification.iSOTrainingCertification;
        BooleanBuilder builder = new BooleanBuilder();

        Iterable<ISOTrainingCertification> isoTrainingCertifications = isoTrainingCertificationService.findAll(builder, qisoTrainingCertification.id.desc());
        List<ISOTrainingCertification> isoTrainingCertificationList = StreamSupport.stream(isoTrainingCertifications.spliterator(), false).collect(Collectors.toList());
        log.info("@Test : {}", isoTrainingCertificationList);

        int size = isoTrainingCertificationList.size();
        AtomicInteger atomicInteger = new AtomicInteger();
        atomicInteger.set(size);
        List<ISOTrainingCertificationDTO> isoTrainingCertificationDTOS = new ArrayList<>();
        isoTrainingCertificationList.forEach(cert -> {
            ISOTrainingCertificationDTO dto = new ISOTrainingCertificationDTO();

            dto.setIndex(atomicInteger.getAndDecrement());
            dto.setId(cert.getId());
            dto.setCertNo(certHeader + cert.getCertNo());
            dto.setName(cert.getUser().getName());
            dto.setTeamDept(cert.getUser().getTeamDept());
            dto.setRole(cert.getUser().getCommaJobTitle());
            dto.setIsoType(cert.getIso().getIsoType().getLabel());
            dto.setTrainingTitle(cert.getIso().getTitle());
            dto.setCompletionDate(DateUtils.format(cert.getIsoTrainingLog().getCompleteDate(), "dd-MMM-yyyy").toUpperCase());

            isoTrainingCertificationDTOS.add(dto);
        });

        return isoTrainingCertificationDTOS;
    }

    @PostMapping("/training/iso/training-certification/info")
    public String certificationInfo(@RequestParam(value = "action", defaultValue = "list") String action,
                                     @RequestParam(value = "id", required = false) Integer id,
                                     @ModelAttribute("info") ISOTrainingCertificationInfo isoTrainingCertificationInfo,
                                     BindingResult result, SessionStatus status, Model model, RedirectAttributes attributes) {


        Optional<Account> userOptional = userRepository.findById(Integer.parseInt(isoTrainingCertificationInfo.getUserId()));

        if(userOptional.isPresent()) {
            Account user = userOptional.get();

            //Signature의 ID는 유저 ID값을 사용함.
            Optional<Signature> signatureOptional = signatureRepository.findById(user.getUsername());
            String base64Signature = signatureOptional.isPresent()?signatureOptional.get().getBase64signature():null;

            isoTrainingCertificationInfo.setManager(user);
            isoTrainingCertificationInfo.setManagerName(user.getName());
            isoTrainingCertificationInfo.setBase64signature(base64Signature);

            isoCertificateInfoValidator.validate(isoTrainingCertificationInfo, result);

            if(result.hasErrors()) {
                model.addAttribute("id", id);
                model.addAttribute("action", action);
                model.addAttribute("userMap", userService.getUserMap());
                return "admin/iso/certificateInfoList";
            }

            isoTrainingCertificationInfoRepository.save(isoTrainingCertificationInfo);
            status.setComplete();
            attributes.addFlashAttribute("message", "수료증 정보가 등록 되었습니다.");
        } else {
            model.addAttribute("id", id);
            model.addAttribute("action", action);
            model.addAttribute("userMap", userService.getUserMap());
            model.addAttribute("message", "등록할 대상의 유저 정보에 문제가 발생하였습니다.");
            return "admin/iso/certificateInfoList";
        }

        return "redirect:/admin/training/iso/training-certification/info";
    }
}
