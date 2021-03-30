package com.cauh.iso.admin.controller;

import com.cauh.common.repository.UserRepository;
import com.cauh.iso.domain.Approval;
import com.cauh.iso.domain.constant.ApprovalStatus;
import com.cauh.iso.domain.constant.ReportType;
import com.cauh.iso.service.ApprovalService;
import com.cauh.iso.admin.service.UserJobDescriptionService;
import com.cauh.iso.utils.DateUtils;
import com.cauh.iso.xdocreport.SOPDisclosureRequestFormService;
import com.cauh.iso.xdocreport.*;
import com.cauh.iso.xdocreport.dto.TrainingDeviationLogDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping({"/admin", "/ajax/admin"})
@RequiredArgsConstructor
@Slf4j
public class AdminApprovalController {
    private final ApprovalService approvalService;
    private final SOPDisclosureRequestFormService sopDisclosureRequestFormService;
    private final SOPDeviationReportService sopDeviationReportService;
    private final UserRepository userRepository;
    private final SopRfRequestFormService sopRfRequestFormService;
    private final SOPWaiverRequestApprovalFormService sopWaiverRequestApprovalFormService;
    private final UserJobDescriptionService userJobDescriptionService;
    private final SopRfRetirementApprovalFormService sopRfRetirementApprovalFormService;

    @Value("${form.name}")
    private String formName;


    @GetMapping({"/approval", "/approval/{status}"})
    public String totalList(@PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC, size = 15) Pageable pageable,
                            @PathVariable(value = "status", required = false) ApprovalStatus status,
                            @RequestParam(value = "type", required = false) String type,
                            Model model) {
        ReportType reportType = null;
        if(!StringUtils.isEmpty(type)) {
            reportType = ReportType.valueOf(type);
        }
        model.addAttribute("approvalList", approvalService.findAllAdmin(status, reportType, pageable));
        model.addAttribute("status", status);

//        model.addAttribute("reportType", Arrays.stream(ReportType.values()).collect(Collectors.toMap(a -> a.getViewName(), a -> a.getLabel())));
        model.addAttribute("reportTypeList", Arrays.asList(ReportType.values()).stream().filter(r -> r.isEnable()).collect(Collectors.toList()));
        model.addAttribute("reportType", type);
        return "admin/approval/totalList";
    }

    @Transactional(readOnly = true)
    @GetMapping({"/approval/{id}/v", "/approval/{status}/{id}/v"})
    public String approval(@PathVariable(value = "id") Integer id,
                           @PathVariable(value = "status", required = false) ApprovalStatus status,
                           Model model) {
        Approval approval = approvalService.findById(id).get();
        model.addAttribute("approvalView", approval);
        model.addAttribute("status", status);

        model.addAttribute("formName", formName);

        return "admin/approval/view";
    }

    @DeleteMapping({"/approval/{id}/v", "/approval/{status}/{id}/v"})
    public String delete(@PathVariable(value = "id") Integer id,
                         @PathVariable(value = "status", required = false) ApprovalStatus status, RedirectAttributes attributes) {
        approvalService.delete(id);


        attributes.addFlashAttribute("message", "삭제 되었습니다.");
        if(ObjectUtils.isEmpty(status)) {
            return "redirect:/admin/approval";
        } else {
            return "redirect:/admin/approval/{status}";
        }
    }

    @GetMapping({"/approval/training/deviation/print", "/approval/training/deviation/{status}/print"})
    @Transactional(readOnly = true)
    public void generateTrainingDeviationLog(@PathVariable(value = "status", required = false) ApprovalStatus status,
                                             HttpServletRequest request, HttpServletResponse response) throws Exception {

        List<TrainingDeviationLogDTO> DTOList = null;

        if(ObjectUtils.isEmpty(status)) {
            DTOList = approvalService.findAllByReportType(ReportType.SOP_Training_Deviation_Report);
            log.info("Training Deviation Log : {}", DTOList);

        } else {
            DTOList = approvalService.findAllByReportTypeAndStatus(ReportType.SOP_Training_Deviation_Report, status);
        }

        log.info("Approval List : {}", DTOList);

        if(DTOList.size() > 0) {
            String strType = ObjectUtils.isEmpty(status)?"_All":"_" + status.name();
            InputStream is = IndexReportService.class.getResourceAsStream("SOP_Training_Deviation_Log_01.xlsx");

            Context context = new Context();
            context.putVar("DTOList", DTOList);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Training_Deviation_Log" + strType +"_" + DateUtils.format(new Date(), "yyyyMMdd") +".xlsx");
            JxlsHelper.getInstance().processTemplate(is, response.getOutputStream(), context);
        } else {

            //FlashMap을 이용하여 request에 attribute 전달
            FlashMap flashMap = new FlashMap();
            flashMap.put("messageType", "danger");
            flashMap.put("message", "Training Deviation List가 없습니다.");
            FlashMapManager flashMapManager = RequestContextUtils.getFlashMapManager(request);
            flashMapManager.saveOutputFlashMap(flashMap, request, response);

            String url = String.format("/admin/approval%s?type=SOP_Training_Deviation_Report", ObjectUtils.isEmpty(status)?"":("/"+status.name()));
            response.sendRedirect(url);
        }
    }

    @GetMapping("/approval/{id}/print")
    @Transactional(readOnly = true)
    public void generateReport(@PathVariable(value = "id") Integer id,
                               HttpServletResponse response) throws Exception {

        Optional<Approval> optionalApproval = approvalService.findById(id);
        if(optionalApproval.isPresent()) {
            Approval approval = optionalApproval.get();
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+approval.getType().getLabel()+".pdf");
            approval.getApprovalLines().forEach(a ->
                a.setUser(userRepository.findByUsername(a.getUsername()).get()));

            if(approval.getType() != ReportType.SOP_RF_Retirement_Form) {
                response.setContentType("application/pdf");
            }

            if(approval.getType() == ReportType.SOP_Disclosure_Request_Form) {
                sopDisclosureRequestFormService.generateReport(approval.getSopDisclosureRequestForm(), approval.getApprovalLines(), response.getOutputStream());
            } else if(approval.getType() == ReportType.SOP_Training_Deviation_Report) {
                sopDeviationReportService.generateReport(approval.getSopDeviationReport(), approval.getApprovalLines(), response.getOutputStream());
            } else if(approval.getType() == ReportType.SOP_RF_Request_Form) {
                sopRfRequestFormService.generateReport(approval.getSopRfRequestForm(), approval.getApprovalLines(), response.getOutputStream());
            } else if(approval.getType() == ReportType.SOP_Waiver_Approval_Form) {
                sopWaiverRequestApprovalFormService.generateReport(approval.getSopWaiverApprovalForm(), approval.getApprovalLines(), response.getOutputStream());
            } else if(approval.getType() == ReportType.SOP_RF_Retirement_Form) {
                sopRfRetirementApprovalFormService.generateReport(approval.getRetirementApprovalForm(), approval.getApprovalLines(), response.getOutputStream());
            }
        }
    }
}
