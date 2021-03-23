package com.cauh.iso;

import com.cauh.common.entity.*;
import com.cauh.common.entity.constant.JobDescriptionStatus;
import com.cauh.common.entity.constant.UserStatus;
import com.cauh.common.entity.constant.UserType;
import com.cauh.common.repository.*;
import com.cauh.common.service.UserService;
import com.cauh.iso.component.CurrentUserComponent;
import com.cauh.iso.domain.*;
import com.cauh.iso.domain.constant.DocumentStatus;
import com.cauh.iso.domain.constant.DocumentType;
import com.cauh.iso.domain.constant.TrainingType;
import com.cauh.iso.repository.*;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
@Slf4j
@Order(1)
@RequiredArgsConstructor
public class InitializeDataRunner implements ApplicationRunner {
    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String springJpaDDLAuto;
    @Value("${spring.profiles.active}")
    private String activeProfile;

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final RoleAccountRepository roleAccountRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final UserJobDescriptionRepository userJobDescriptionRepository;
    private final CategoryRepository categoryRepository;
    private final DocumentRepository documentRepository;
    private final PasswordEncoder passwordEncoder;
    private final DocumentVersionRepository documentVersionRepository;
    private final TrainingMatrixRepository trainingMatrixRepository;
    private final TrainingPeriodRepository trainingPeriodRepository;
    private final JobDescriptionVersionRepository jobDescriptionVersionRepository;

    private final CurrentUserComponent currentUserComponent;

//    private final DeptUserMapper deptUserMapper;
    private final UserService userService;

    public void run(ApplicationArguments args) {
        log.info("springJpaDDLAuto => {}", springJpaDDLAuto);
        log.info("@Env : {}", activeProfile);

        if("prod".equals(activeProfile) && !userService.findByUsername("admin").isPresent()) {
            //DEV 환경에서 시작 시, 초기 세팅
            //Role savedRole = addRole(1L, "ADMIN", "관리자");
            //JobDescription jobDescription = addJobDescription(1, "ADMIN", "관리자");
            //log.info("JD : {}", jobDescription);
            Account savedAccount = addUser(1, "admin", "admin", "관리자", "Administrator", true, null);
            //UserJobDescription userJobDescription = addUserJobDescription(1, savedAccount, jobDescription);
            //log.info("User JDs : {}", userJobDescription);
        }

        //현재 유저 Update
        currentUserComponent.updateCurrentUserList();

//        userService.sync();
//        if("local".equals(activeProfile)) {
//        } else if("prod".equals(activeProfile)) {
//            //TODO 향후 테스트 완료 후 해당 코드 삭제 필요!!!!
//            java.util.Date date = new java.util.Date();
//            JobDescription jd_qa = addJobDescription(1, "QAA", "Quality Assurance Associate");
//            JobDescriptionVersion jdv_qa = addJobDescriptionVersion(1, jd_qa, "1.0", date);
//
//            JobDescription jd_qmo = addJobDescription(2, "QMO", "Quality Management Officer");
//            JobDescriptionVersion jdv_qmo = addJobDescriptionVersion(2, jd_qmo, "1.0", date);
//
//            JobDescription jd_qmm = addJobDescription(3, "QAM", "Quality Assurance Manager");
//            JobDescriptionVersion jdv_qmm = addJobDescriptionVersion(3, jd_qmm, "1.0", date);
//
//            JobDescription jd_spm = addJobDescription(4, "SPM", "Software Project Manager");
//            JobDescriptionVersion jdv_spm = addJobDescriptionVersion(4, jd_spm, "1.0", date);
//
//            JobDescription jd_dm = addJobDescription(5, "DMA", "Data Management Associate");
//            JobDescription jd_cra = addJobDescription(6, "CRA", "Clinical Research Associate");
//            JobDescription jd_bda = addJobDescription(7, "BDA", "Business Development Associate");
//            JobDescription jd_cm = addJobDescription(8, "CM", "Central Monitor");
//            JobDescription jd_cda = addJobDescription(9, "CDA", "Clinical Data Associate");
//            JobDescription jd_mw = addJobDescription(10, "MW", "Medical Writer");
//
//            Optional<User> jhseo = userService.findByUsername("jhseo");
//            Optional<User> hjlim = userService.findByUsername("hjlim");
//            Optional<User> hjlee = userService.findByUsername("hjlee");
//            Optional<User> mhahn = userService.findByUsername("mhahn");
//            if(jhseo.isPresent()) {
//                addUserJobDescription(1, jhseo.get(), jdv_spm);
//                addUserJobDescription(6, jhseo.get(), jdv_qa);
//            }
//            if(hjlim.isPresent()) {
//                addUserJobDescription(2, hjlim.get(), jdv_qmm);
//                addUserJobDescripti on(3, hjlim.get(), jdv_qmo);
//            }
//            if(hjlee.isPresent()) {
//                addUserJobDescription(4, hjlee.get(), jdv_qa);
//            }
//            if(mhahn.isPresent()) {
//                addUserJobDescription(5, mhahn.get(), jdv_qa);
//            }
//        }
    }


    public DocumentVersion addDocumentVersion(Document document, String version, DocumentStatus status, Date effectiveDate) {
        DocumentVersion documentVersion = DocumentVersion.builder()
                .id(UUID.randomUUID().toString())
                .document(document)
                .version(version)
                .effectiveDate(effectiveDate)
                .status(status)
                .build();

        if(document.getType() == DocumentType.SOP) {
            documentVersion.setQuiz("{\"quizQuestions\":[{\"index\":1,\"text\":\"문제1\",\"answers\":[{\"index\":1,\"text\":\"보기1\",\"correct\":true},{\"index\":2,\"text\":\"보기2\",\"correct\":false},{\"index\":3,\"text\":\"보기3\",\"correct\":false},{\"index\":4,\"text\":\"보기4\",\"correct\":false},{\"index\":5,\"text\":\"보기5\",\"correct\":false}],\"correct\":[1]},{\"index\":2,\"text\":\"문제2\",\"answers\":[{\"index\":1,\"text\":\"보기1\",\"correct\":false},{\"index\":2,\"text\":\"보기2\",\"correct\":true},{\"index\":3,\"text\":\"보기3\",\"correct\":false},{\"index\":4,\"text\":\"보기4\",\"correct\":false},{\"index\":5,\"text\":\"보기5\",\"correct\":false}],\"correct\":[2]},{\"index\":3,\"text\":\"문제3\",\"answers\":[{\"index\":1,\"text\":\"보기1\",\"correct\":false},{\"index\":2,\"text\":\"보기2\",\"correct\":false},{\"index\":3,\"text\":\"보기3\",\"correct\":true},{\"index\":4,\"text\":\"보기4\",\"correct\":false},{\"index\":5,\"text\":\"보기5\",\"correct\":false}],\"correct\":[3]},{\"index\":4,\"text\":\"문제4\",\"answers\":[{\"index\":1,\"text\":\"보기1\",\"correct\":false},{\"index\":2,\"text\":\"보기2\",\"correct\":false},{\"index\":3,\"text\":\"보기3\",\"correct\":false},{\"index\":4,\"text\":\"보기4\",\"correct\":true},{\"index\":5,\"text\":\"보기5\",\"correct\":false}],\"correct\":[4]},{\"index\":5,\"text\":\"문제5\",\"answers\":[{\"index\":1,\"text\":\"보기1\",\"correct\":true},{\"index\":2,\"text\":\"보기2\",\"correct\":false},{\"index\":3,\"text\":\"보기3\",\"correct\":true},{\"index\":4,\"text\":\"보기4\",\"correct\":false},{\"index\":5,\"text\":\"보기5\",\"correct\":true}],\"correct\":[1,3,5]}]}");
        }

        return documentVersionRepository.save(documentVersion);
    }

    public Category addCategory(String shortName, String name) {
        Category category = Category.builder()
                .shortName(shortName)
                .name(name)
                .build();

        return categoryRepository.save(category);
    }

    public Document addSOP(String id, Category Category, String documentNo, String title) {
        Document document = Document.builder()
                .id(id)
                .Category(Category)
                .documentNo(documentNo)
                .docId(DocumentType.SOP.name() +"-" + Category.getShortName() + documentNo)
                .title(title)
                .type(DocumentType.SOP)
                .build();

        return documentRepository.save(document);
    }

    public Document addRD(Document sopDocument, String documentNo, String title) {
        Document rd = Document.builder()
                .sop(sopDocument)
                .documentNo(documentNo)
                .docId(sopDocument.getDocId() + "_RD" + documentNo)
                .title(title)
                .type(DocumentType.RF)
                .build();

        return documentRepository.save(rd);
    }

    public Role addRole(Long id, String name, String memo){
        Role role = new Role();
             role.setId(id);
             role.setMemo(memo);
             role.setName(name);
        return roleRepository.save(role);
    }

    public Account addUser(Integer id, String username, String password, String korName, String engName, boolean admin, String empNo) {
        Account user = new Account();
                user.setId(id);
                user.setUsername(username);
                user.setName(korName);
                user.setEngName(engName);
                user.setAccountNonLocked(true);
                user.setPassword(passwordEncoder.encode("admin"));
                user.setDeptName("시스템");
                user.setTeamName("관리자");
                user.setIndate(new Date());
                user.setEnabled(true);
                user.setUserStatus(UserStatus.ACTIVE);
                user.setAdmin(admin);
                user.setUserType(UserType.ADMIN);
                user.setEmail("sh.yang@safesoft.co.kr");

        return userRepository.save(user);
    }

    public RoleAccount addRoleUser(Long id, Role role, Account account) {
        RoleAccount roleAccount = new RoleAccount();
                    roleAccount.setId(id);
                    roleAccount.setRole(role);
                    roleAccount.setAccount(account);
        return roleAccountRepository.save(roleAccount);
    }

    public JobDescription addJobDescription(Integer id, String shortName, String title) {
        JobDescription jd = JobDescription.builder()
                .id(id)
                .shortName(shortName)
                .title(title)
                .build();

        return jobDescriptionRepository.save(jd);
    }

    public JobDescriptionVersion addJobDescriptionVersion(Integer id, JobDescription jobDescription, String version, java.util.Date releaseDate) {
        JobDescriptionVersion jobDescriptionVersion = new JobDescriptionVersion();
        jobDescriptionVersion.setId(id);
        jobDescriptionVersion.setJobDescription(jobDescription);
        jobDescriptionVersion.setVersion_no(version);
        jobDescriptionVersion.setRelease_date(releaseDate);

        return jobDescriptionVersionRepository.save(jobDescriptionVersion);
    }

    public UserJobDescription addUserJobDescription(Integer id, Account user, JobDescription jobDescription) {
        QUserJobDescription qUserJobDescription = QUserJobDescription.userJobDescription;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(qUserJobDescription.user.username.eq(user.getUsername()).and(qUserJobDescription.id.eq(jobDescription.getId())));
        boolean isPresent = userJobDescriptionRepository.findOne(builder).isPresent();
        log.debug("UserJobDescription [{}], JD-id : {}, isPresent : {}", user.getUsername(), jobDescription.getId(), isPresent);
        if(isPresent == false) {
            UserJobDescription userJd = new UserJobDescription();
            userJd.setId(id);
            userJd.setUser(user);
            userJd.setJobDescription(jobDescription);
            userJd.setAssignDate(user.getIndate());
            userJd.setStatus(JobDescriptionStatus.APPROVED);

            log.info(" ==> insert : {}", userJd);
            return userJobDescriptionRepository.save(userJd);
        } else {
            return null;
        }
    }


    public TrainingPeriod addTrainingPeriod(DocumentVersion documentVersion, java.util.Date startDate, java.util.Date endDate, TrainingType trainingType) {
        TrainingPeriod trainingPeriod = TrainingPeriod.builder()
                .documentVersion(documentVersion)
                .startDate(startDate)
                .endDate(endDate)
                .trainingType(trainingType)
                .build();

        return trainingPeriodRepository.save(trainingPeriod);
    }

    public void addTrainingMatrix(DocumentVersion documentVersion) {
        TrainingMatrix trainingMatrix = TrainingMatrix.builder()
                .documentVersion(documentVersion)
                .trainingAll(true)
                .build();

        trainingMatrixRepository.save(trainingMatrix);
    }



}
