package com.cauh.iso.admin.controller;

import com.cauh.iso.service.DocumentAccessLogService;
import com.cauh.iso.service.ISOAccessLogService;
import com.cauh.iso.service.TrainingAccessLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AdminDocumentAccessLogController {
    private final DocumentAccessLogService documentAccessLogService;
    private final ISOAccessLogService isoAccessLogService;
    private final TrainingAccessLogService trainingAccessLogService;

    @Value("${form.name}")
    private String formName;

    @GetMapping("/admin/document/accessLog")
    public String accessLog(@PageableDefault(sort = {"createdDate"}, direction = Sort.Direction.DESC, size = 15) Pageable pageable, Model model) {
        model.addAttribute("accessLog", documentAccessLogService.findAll(pageable));
        model.addAttribute("formName", formName);
        return "admin/sop/accessLog";
    }

    @GetMapping("/admin/iso/accessLog")
    public String isoAccessLog(@PageableDefault(sort = {"createdDate"}, direction = Sort.Direction.DESC, size = 15) Pageable pageable, Model model) {
        model.addAttribute("isoAccessLog", isoAccessLogService.findAll(pageable));
        model.addAttribute("formName", formName);
        return "admin/iso/accessLog";
    }

    @GetMapping("/admin/training/accessLog")
    public String trainingAccessLog(@PageableDefault(sort = {"createdDate"}, direction = Sort.Direction.DESC, size = 15) Pageable pageable, Model model) {
        model.addAttribute("trainingAccessLog", trainingAccessLogService.findAll(pageable));
        model.addAttribute("formName", formName);
        return "admin/training/accessLog";
    }

}
