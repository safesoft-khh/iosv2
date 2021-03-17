package com.cauh.iso.service;


import com.cauh.common.entity.QAccount;
import com.cauh.common.entity.Account;
import com.cauh.common.repository.UserRepository;
import com.cauh.iso.domain.Mail;
import com.querydsl.core.BooleanBuilder;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.ObjectUtils;

import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@Slf4j
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {
    private final Configuration freemarkerConfig;
    private final UserRepository userRepository;
    private final JavaMailSender javaMailSender;

    @Value("${server.domain}")
    private String domain;

    @Value("${site.footer-msg}")
    private String footerMsg;

    @Value("${mail.notice.name}")
    private String emailName;

    @Value("${mail.notice.address}")
    private String emailAddress;

    private String EMAIL_PATH = "email/";

    public String processTemplate(String fileName, HashMap<String, Object> model, Locale locale) {
        try {
            Template template = freemarkerConfig.getTemplate(EMAIL_PATH + fileName, "UTF-8");
//            log.debug("=> domain : {}", domain);
            if (ObjectUtils.isEmpty(model)) {
                model = new HashMap<>();
            }
            model.put("domain", domain);
            model.put("footerMsg", footerMsg);

            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);

            return html;
        } catch (Exception error) {
            log.error("Freemarker Template Error : ", error);
            return "";
        }
    }

    @Async("threadPoolTaskExecutor")
    public void sendMail(Mail mail) {
        try {
            String content = processTemplate(mail.getTemplateName() + ".ftlh", mail.getModel(), mail.getLocale());
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            helper.setTo(mail.getTo());
            log.info("*** 이메일 전송 요청 ***\n- Subject : {}\n- To : {}\n- CC : {}, model : {}", mail.getSubject(), mail.getTo(), mail.getCc(),
                    mail.getModel());
//            helper.setTo("sanomedics@dtnsm.com");
            if(ObjectUtils.isEmpty(mail.getCc()) == false) {
                helper.setCc(mail.getCc());
//                helper.setCc("sanomedics@dtnsm.com");
            }
            if(ObjectUtils.isEmpty(mail.getBcc()) == false) {
                helper.setCc(mail.getBcc());
            }
            helper.setReplyTo(emailAddress, emailName);
            helper.setFrom(emailAddress, emailName);
            helper.setSubject(mail.getSubject());
            helper.setText(content, true);
            javaMailSender.send(message);
        } catch (Exception e) {
            log.error("error : {}", e);
        }
    }

    @Override
    public List<String> getReceiveEmails() {
        QAccount qUser = QAccount.account;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qUser.receiveEmail.eq(true));
        builder.and(qUser.email.isNotNull());

        Iterable<Account> iterable = userRepository.findAll(builder);
        return StreamSupport.stream(iterable.spliterator(), false)
                .map(u -> u.getEmail())
            .collect(Collectors.toList());
    }
}
