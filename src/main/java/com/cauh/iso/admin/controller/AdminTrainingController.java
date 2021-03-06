package com.cauh.iso.admin.controller;

import com.cauh.common.entity.Account;
import com.cauh.common.entity.Department;
import com.cauh.common.entity.QAccount;
import com.cauh.common.entity.constant.UserStatus;
import com.cauh.common.mapper.DeptUserMapper;
import com.cauh.common.repository.DepartmentRepository;
import com.cauh.common.repository.UserRepository;
import com.cauh.common.security.annotation.CurrentUser;
import com.cauh.common.service.UserService;
import com.cauh.iso.admin.service.DepartmentService;
import com.cauh.iso.domain.*;
import com.cauh.iso.domain.constant.*;
import com.cauh.iso.repository.TrainingMatrixRepository;
import com.cauh.iso.service.*;
import com.cauh.iso.utils.DateUtils;
import com.cauh.iso.validator.TrainingPeriodValidator;
import com.cauh.iso.xdocreport.IndexReportService;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@SessionAttributes({"trainingPeriod", "sopMap"})
@Slf4j
public class AdminTrainingController {
    private final TrainingMatrixService trainingMatrixService;
    private final OfflineTrainingService offlineTrainingService;
    private final TrainingPeriodService trainingPeriodService;
    private final DocumentVersionService documentVersionService;
    private final TrainingPeriodValidator trainingPeriodValidator;
    private final DeptUserMapper deptUserMapper;
    private final DepartmentService departmentService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final TrainingMatrixRepository trainingMatrixRepository;
    private final TrainingAccessLogService trainingAccessLogService;

//    @Value("${gw.userTbl}")
//    private String gwUserTbl;
//
//    @Value("${gw.deptTbl}")
//    private String gwDeptTbl;

    @GetMapping("/training/sop/matrix")
    public String trainingMatrix(@PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC, size = 20) Pageable pageable, Model model) {
        model.addAttribute("trainingMatrix", trainingMatrixService.findAll(pageable));
        return "admin/training/matrix/list";
    }

    @PostMapping("/training/sop/matrix")
    public String uploadTrainingMatrix(@ModelAttribute TrainingMatrixFile trainingMatrixFile) {
        trainingMatrixService.save(trainingMatrixFile);

        return "redirect:/admin/training/sop/matrix";
    }

    @GetMapping("/training/sop/offline-training")
    public String offlineTraining(@PageableDefault(size = 25, sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable, Model model) {
        model.addAttribute("offlineTraining", offlineTrainingService.findAll(pageable));
        return "admin/training/offline/list";
    }

    @GetMapping("/training/sop/offline-training/{id}")
    public String offlineTraining(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("offlineTraining", offlineTrainingService.findById(id).get());
        return "admin/training/offline/view";
    }


    @PostMapping("/training/sop/offline-training/{id}")
    public String offlineTraining(@PathVariable("id") Integer id, RedirectAttributes attributes) {
        OfflineTraining savedOfflineTraining = offlineTrainingService.offlineTrainingApply(id);
        offlineTrainingService.sendApplyEmail(savedOfflineTraining);

        attributes.addFlashAttribute("message", "???????????? ????????? ?????? ???????????????.");
        return "redirect:/admin/training/sop/offline-training";
    }

    @DeleteMapping("/training/sop/offline-training/{id}")
    public String deleteOfflineTraining(@PathVariable("id") Integer id, RedirectAttributes attributes) {
        offlineTrainingService.delete(id);

        attributes.addFlashAttribute("message", "Offline Training ?????? ????????? ?????? ???????????????.");
        return "redirect:/admin/training/sop/offline-training";
    }

    @GetMapping("/training/sop/refresh-training")
    public String refreshTraining(@PageableDefault(size = 25, sort = {"startDate"}, direction = Sort.Direction.DESC) Pageable pageable, Model model) {
        QTrainingPeriod qTrainingPeriod = QTrainingPeriod.trainingPeriod;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qTrainingPeriod.documentVersion.document.type.eq(DocumentType.SOP));
        builder.and(qTrainingPeriod.trainingType.eq(TrainingType.REFRESH));

        model.addAttribute("refreshTraining", trainingPeriodService.findAll(builder, pageable));

        return "admin/training/refresh/list";
    }

    //refresh ?????? ???,
    @DeleteMapping("/training/sop/refresh-training")
    public String removeRefreshTraining(@RequestParam("id") Integer id, RedirectAttributes attributes) {
        trainingPeriodService.deleteById(id);
        attributes.addFlashAttribute("message", "?????? ???????????????.");
        return "redirect:/admin/training/sop/refresh-training";
    }

    //SOP ??? ISO refresh-training ?????? ??? ?????? ?????? ?????? ???,
    @GetMapping({"/training/sop/refresh-training/new", "/training/sop/refresh-training/{id}"})
    public String refreshTraining(@PathVariable(value = "id", required = false) Integer id, Model model) {
        QDocumentVersion qDocumentVersion = QDocumentVersion.documentVersion;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qDocumentVersion.document.type.eq(DocumentType.SOP)
                .and(qDocumentVersion.status.in(DocumentStatus.EFFECTIVE)));
        Iterable<DocumentVersion> documentVersions = documentVersionService.findAll(builder);

        model.addAttribute("sopMap", StreamSupport.stream(documentVersions.spliterator(), false)
                .collect(Collectors.toMap(s -> s.getId(), s -> s.getDocument().getDocId() + " " + s.getDocument().getTitle() + "v" + s.getVersion())));

        TrainingPeriod trainingPeriod = ObjectUtils.isEmpty(id) ? new TrainingPeriod() : trainingPeriodService.findById(id).get();
        trainingPeriod.setNotification(false);

        model.addAttribute("trainingPeriod", trainingPeriod);

        return "admin/training/refresh/edit";
    }

    //SOP ??? ISO refresh-training ?????? ??? ?????? ?????? ?????? ???,
    @PostMapping({"/training/sop/refresh-training/new", "/training/sop/refresh-training/{id}"})
    public String refreshTraining(@PathVariable(value = "id", required = false) Integer id, @ModelAttribute("trainingPeriod") TrainingPeriod trainingPeriod,
                                BindingResult result, SessionStatus status, RedirectAttributes attributes) {
        trainingPeriodValidator.validate(trainingPeriod, result);

        if(result.hasErrors()) {
            return "admin/training/refresh/edit";
        }

        if(!ObjectUtils.isEmpty(id)) { // ??????
            trainingPeriod.setId(id);
            attributes.addFlashAttribute("Refresh Training??? ?????? ???????????????.");
        } else { //?????? ??????
            attributes.addFlashAttribute("Refresh Training??? ?????? ???????????????.");
        }

        trainingPeriodService.saveOrUpdateRefreshTraining(trainingPeriod);
        status.setComplete();


        if(trainingPeriod.getNotification()) {
            trainingPeriodService.refreshNotification(trainingPeriod);
        }

        return "redirect:/admin/training/sop/refresh-training";
    }

    @GetMapping({"/training/sop/trainingLog", "/training/sop/trainingLog/{complete}"})
    public String teamDeptTrainingLog(@PageableDefault(size = 25) Pageable pageable,
                                       @PathVariable(value = "complete", required = false) String isComplete,
                                       @RequestParam(value = "deptId", required = false) Integer deptId,
                                       @RequestParam(value = "teamId", required = false) Integer teamId,
                                       @RequestParam(value = "userId", required = false) Integer userId,
                                       @RequestParam(value = "docId", required = false) String docId,
                                       Model model) {

        //?????? ??????
        model.addAttribute("deptList", departmentService.getParentDepartment());
        Department department = null;

//        boolean isDepart = !ObjectUtils.isEmpty(deptId);
//        boolean isTeam = !ObjectUtils.isEmpty(deptId);
//
//        if(isDepart || isTeam) {
//            if (isDepart && !isTeam) { //????????? ????????? ??????
//                department = new Department(deptId);
//                QAccount qUser = QAccount.account;
//                BooleanBuilder userBuilder = new BooleanBuilder();
//                userBuilder.and(qUser.department.id.eq(deptId));
//                model.addAttribute("userList", userRepository.findAll(userBuilder, qUser.engName.asc()));
//            } else if (isDepart && isTeam) { //?????? ??? ??? ?????? ????????? ??????
//                department = new Department(teamId);
//                QAccount qUser = QAccount.account;
//                BooleanBuilder userBuilder = new BooleanBuilder();
//                userBuilder.and(qUser.department.id.eq(teamId));
//                model.addAttribute("userList", userRepository.findAll(userBuilder, qUser.engName.asc()));
//            }
//
//        }

        if(!ObjectUtils.isEmpty(deptId)) {
            department = new Department(deptId);
            model.addAttribute("teamList", departmentService.getChildDepartment(department));

            if (!ObjectUtils.isEmpty(teamId)) {//??????????????? ?????? ??????
                QAccount qUser = QAccount.account;
                department = new Department(teamId);
                BooleanBuilder userBuilder = new BooleanBuilder();
                userBuilder.and(qUser.department.eq(department));
                userBuilder.and(qUser.userStatus.eq(UserStatus.ACTIVE));
                model.addAttribute("userList", userRepository.findAll(userBuilder, qUser.engName.asc()));
            }
            else {//????????? ??????
                QAccount qUser = QAccount.account;
                BooleanBuilder userBuilder = new BooleanBuilder();
                userBuilder.and(qUser.department.eq(department));
                userBuilder.and(qUser.userStatus.eq(UserStatus.ACTIVE));
                userBuilder.or(qUser.department.in(departmentService.getChildDepartment(department)));
                model.addAttribute("userList", userRepository.findAll(userBuilder, qUser.engName.asc()));
            }
        }

        BooleanBuilder docStatus = new BooleanBuilder();
        QDocumentVersion qDocumentVersion = QDocumentVersion.documentVersion;
        docStatus.and(qDocumentVersion.status.in(DocumentStatus.APPROVED, DocumentStatus.EFFECTIVE));

        BooleanBuilder completeStatus = new BooleanBuilder();
        QTrainingLog qTrainingLog = QTrainingLog.trainingLog;

        if(StringUtils.isEmpty(isComplete)) {
            completeStatus.and(qTrainingLog.status.notIn(TrainingStatus.COMPLETED).or(qTrainingLog.status.isNull()));
        } else if(!StringUtils.isEmpty(isComplete) && isComplete.equals("completed")) {
            completeStatus.and(qTrainingLog.status.in(TrainingStatus.COMPLETED));
        } else {
            return "redirect:/admin/training/sop/trainingLog";
        }

        model.addAttribute("trainingLog", trainingMatrixRepository.getTrainingList(department, userId, docId, null, pageable, docStatus, completeStatus));
        return "training/teamDeptTrainingLog2";
    }

    @PostMapping({"/training/sop/trainingLog", "/training/sop/trainingLog/{complete}"})
//    @Transactional(readOnly = true)
    @Transactional
    public void downloadTeamDeptTrainingLog(@PathVariable(value = "complete", required = false) String isComplete,
                                            @RequestParam(value = "deptId", required = false) Integer deptId,
                                            @RequestParam(value = "teamId", required = false) Integer teamId,
                                            @RequestParam(value = "userId", required = false) Integer userId,
                                            @RequestParam(value = "docId", required = false) String docId,
                                            @CurrentUser Account user,
                                            HttpServletRequest request, HttpServletResponse response) throws Exception {

        //team, user ????????? ????????? ??????????????? ?????? ??????
        if((!ObjectUtils.isEmpty(teamId) || !ObjectUtils.isEmpty(userId)) && ObjectUtils.isEmpty(deptId)) {
            log.error("?????? ????????? ?????????????????????. deptId : {}, teamId = {}, userId = {}", deptId, teamId, userId);
            return;
        }

        Department department = null;

        //???????????? ????????? ???, ????????? ????????????.
        if(!ObjectUtils.isEmpty(deptId)) {
            if(!ObjectUtils.isEmpty(teamId)) {
                department = departmentService.getDepartmentById(teamId);
            } else {
                department = departmentService.getDepartmentById(deptId);
            }
        }

        BooleanBuilder completeStatus = new BooleanBuilder();
        QTrainingLog qTrainingLog = QTrainingLog.trainingLog;

        TrainingLogType trainingLogType = null;

        if(StringUtils.isEmpty(isComplete)) {
            completeStatus.and(qTrainingLog.status.notIn(TrainingStatus.COMPLETED).or(qTrainingLog.status.isNull()));
            trainingLogType = TrainingLogType.SOP_ADMIN_NOT_COMPLETE_LOG;
        } else if(!StringUtils.isEmpty(isComplete) && isComplete.equals("completed")) {
            completeStatus.and(qTrainingLog.status.in(TrainingStatus.COMPLETED));
            trainingLogType = TrainingLogType.SOP_ADMIN_COMPLETE_LOG;
        } else {
            log.error("Training Export Error : ???????????? ?????? URI????????? : {}", request.getRequestURI());
            return;
        }

        List<MyTraining> trainingList = trainingMatrixRepository.getDownloadTrainingList(department, userId, docId, null, completeStatus);
        InputStream is = IndexReportService.class.getResourceAsStream("Admin_SOP_TrainingLog.xlsx");

        Context context = new Context();
        context.putVar("trainings", trainingList);
        response.setHeader("Content-Disposition", "attachment; filename=\"SOP_TrainingLog("+ DateUtils.format(new Date(), "yyyyMMdd")+").xlsx\"");
        JxlsHelper.getInstance().processTemplate(is, response.getOutputStream(), context);

        //Training AccessLog ??????
        Optional<TrainingAccessLog> savedLog = trainingAccessLogService.save(user, trainingLogType, DocumentAccessType.DOWNLOAD);
        log.info("@Download Training Log ?????? : {}", savedLog.get().getId());

    }
}
