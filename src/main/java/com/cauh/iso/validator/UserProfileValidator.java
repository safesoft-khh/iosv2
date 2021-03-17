package com.cauh.iso.validator;

import com.cauh.common.entity.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class UserProfileValidator implements Validator {


    @Override
    public boolean supports(Class<?> clazz) {
        return false;
    }

    @Override
    public void validate(Object target, Errors errors) {
        Account account = (Account) target;

        if(ObjectUtils.isEmpty(account.getId())) {
            errors.rejectValue("username", "message.username.required", "ID를 입력해주세요.");
        }

        if(ObjectUtils.isEmpty(account.getBirthDate())) {
            errors.rejectValue("birthDate", "message.birthDate.required", "생년월일을 입력해주세요.");
        }

//        if(ObjectUtils.isEmpty(account.getPhone())) {
//            errors.rejectValue("phone", "message.phone.required", "전화번호를 입력해주세요.");
//        }

        if(ObjectUtils.isEmpty(account.getName())) {
            errors.rejectValue("name", "message.name.required", "이름을 입력해주세요.");
        }

        if(ObjectUtils.isEmpty(account.getEmail())) {
            errors.rejectValue("email", "message.email.required", "이메일을 입력해주세요.");
        }
    }
}
