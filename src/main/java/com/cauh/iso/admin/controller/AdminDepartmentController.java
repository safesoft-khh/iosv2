package com.cauh.iso.admin.controller;

import com.cauh.common.entity.Department;
import com.cauh.common.entity.JobDescription;
import com.cauh.common.entity.QAccount;
import com.cauh.common.entity.QDepartment;
import com.cauh.common.repository.DepartmentRepository;
import com.cauh.common.repository.UserRepository;
import com.cauh.iso.admin.service.DepartmentService;
import com.cauh.iso.admin.validator.DepartmentValidator;
import com.groupdocs.conversion.internal.c.a.d.a.d.B;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.util.*;

@Controller
@RequiredArgsConstructor
@Slf4j
@SessionAttributes({"departments", "department"})
public class AdminDepartmentController {

    private final DepartmentRepository departmentRepository;
    private final DepartmentService departmentService;
    private final DepartmentValidator departmentValidator;
    private final UserRepository userRepository;

    @GetMapping("/admin/department")
    public String departments(@RequestParam(value = "action", defaultValue = "list") String action,
                       @RequestParam(value = "id", required = false) Integer id,
                       @RequestParam(value = "pid", required = false) Integer parentId,
                       @ModelAttribute("jobDescription") JobDescription jobDescription, Model model) {
        QDepartment qDepartment = QDepartment.department;
        BooleanBuilder builder = new BooleanBuilder();
        //?????? ????????? ?????? ????????? ????????????
        builder.and(qDepartment.parentDepartment.isNull());
        Iterable<Department> departments = departmentRepository.findAll(builder, qDepartment.id.asc());
        model.addAttribute("departments", departments);

        if("new".equals(action)){
            model.addAttribute("department", new Department());
        }
        else if("subNew".equals(action)) {
            Department department = new Department();
            //pid??? ????????? parentDepartment??? ??? ??????
            if(!ObjectUtils.isEmpty(parentId)){
                Department parentDepartment = new Department(parentId);
                department.setParentDepartment(parentDepartment);
            }
            model.addAttribute("department", department);
        } else if ("edit".equals(action)) {
            Department department = departmentRepository.findById(id).get();
            model.addAttribute("department", department);
        } else if ("subEdit".equals(action)){
            Department department = departmentRepository.findById(id).get();
            log.info("SubEdit Dept : {}", department);

            model.addAttribute("department", department);
        }

        model.addAttribute("action", action);
        model.addAttribute("id", id);
        model.addAttribute("pid", parentId);

        return "/admin/department/departments";
    }

    @DeleteMapping("/admin/department")
    public String deleteDepartment(@RequestParam("id") Integer id, RedirectAttributes attributes){
        Optional<Department> optionalDepartment = departmentRepository.findById(id);

        if(optionalDepartment.isPresent()) {
            Department department = optionalDepartment.get();

            List<Department> totalDepartments = new ArrayList<>();
            totalDepartments.addAll(department.getChildDepartments());
            totalDepartments.add(department);

            for(Department dept : totalDepartments) {
                Integer size = userRepository.countAllByDepartment(dept);
                if(size > 0) {
                    attributes.addFlashAttribute("messageType", "danger");
                    attributes.addFlashAttribute("message", "ERROR :: [ " + dept.getName() + "] ????????? ????????? ????????? ????????????.");
                    return "redirect:/admin/department";
                }
            }
            attributes.addFlashAttribute("message", "?????? ????????? ?????????????????????.");
            departmentRepository.delete(optionalDepartment.get());
        }
        return "redirect:/admin/department";
    }

    @PostMapping("/admin/department")
    public String editDepartment(@RequestParam(value = "action", defaultValue = "list") String action,
                                 @RequestParam(value = "id", required = false) Integer id,
                                 @ModelAttribute("department") Department department, BindingResult result,
                                 SessionStatus status, Model model, RedirectAttributes attributes) {
        //isYn ?????? - ?????? null?????? 'N' ???????????? 'Y'
        if(!StringUtils.isEmpty(department.getIsYn()) && department.getIsYn().equals("Y")){
            department.setIsYn("Y");
        }else{
            department.setIsYn("N");
        }

        departmentValidator.validate(department, result);

        if(result.hasErrors()) {
            //parentDepartment??? ?????? ??????
            if(!ObjectUtils.isEmpty(department.getParentDepartment())){
                model.addAttribute("pid", department.getParentDepartment().getId());
            }

            model.addAttribute("id", id);
            model.addAttribute("action", action);
            return "admin/department/departments";
        }

        departmentService.saveAll(department);
        status.setComplete();
        attributes.addFlashAttribute("message", "?????? ????????? ?????? ???????????????.");
        return "redirect:/admin/department";
    }
}
