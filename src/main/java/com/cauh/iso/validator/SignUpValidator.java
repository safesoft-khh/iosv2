package com.cauh.iso.validator;

import com.cauh.common.entity.Account;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.regex.Pattern;

@Component
@Slf4j
public class SignUpValidator implements Validator {


    @Override
    public boolean supports(Class<?> aClass) {
        return false;
    }

    @Override
    public void validate(Object target, Errors errors) {
        Account account = (Account)target;

        String passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@$!%*#?&])[A-Za-z\\d$@$!%*#?&]{8,}$";

        if(ObjectUtils.isEmpty(account.getUsername())) {
            errors.rejectValue("username", "message.username.required", "ID를 입력해 주세요.");
        }

        if(ObjectUtils.isEmpty(account.getEmail())) {
            errors.rejectValue("email", "message.email.required", "> password is required");
        }

        if(ObjectUtils.isEmpty(account.getPassword())) {
            errors.rejectValue("password", "message.password.required", "> password is required");
        } else {
           if(!ObjectUtils.isEmpty(account.getConfirmPassword()) && !account.getConfirmPassword().equals(account.getPassword())){
               errors.rejectValue("password", "message.password.mismatched", "> The password and its P/W Confirm are not the same");
           } else if(!Pattern.matches(passwordRegex, account.getPassword())) {
               errors.rejectValue("password", "message.password.regex.mismatched", "> The password can over than 8 length consist of upper/lower alphabetical, number, dot and special character");
           }
        }

        if(ObjectUtils.isEmpty(account.getName())) {
            errors.rejectValue("name", "message.name.required", "> Name is required");
        }

        if(ObjectUtils.isEmpty(account.getBirthDate())) {
            errors.rejectValue("birthDate", "message.birthDate.required", "> Birth Date is required");
        }

        if(ObjectUtils.isEmpty(account.getIndate())) {
            errors.rejectValue("indate", "message.indate.required", "> The Emplyoee Date is required");
        }
    }
}
