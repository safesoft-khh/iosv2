package com.cauh.iso.domain;

import com.cauh.common.entity.BaseEntity;
import com.cauh.iso.admin.domain.constant.SOPAction;
import com.cauh.iso.domain.constant.DocumentStatus;
import com.cauh.iso.utils.DateUtils;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@Entity
@Table(name = "s_document_version",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"document_id", "version"})
        },
        indexes = {
        @Index(columnList = "document_id, version"),
        @Index(columnList = "status")
})
@Slf4j
@ToString(of = {"id"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@Audited(withModifiedFlag = true)
public class DocumentVersion extends BaseEntity implements Serializable {

    @Id
    @Column(name = "id", length = 40)
    private String id;

    @OneToMany(mappedBy = "documentVersion")
    private List<TrainingMatrix> trainingMatrixList;

    @ManyToOne
    @JoinColumn(name = "document_id", referencedColumnName = "id")
    private Document document;

    @Column(name = "version", length = 5)
    private String version;

    @Column(name = "quiz", columnDefinition = "nvarchar(MAX)")
    private String quiz;

    @ManyToOne
    @JoinColumn(name = "parent_version_id", referencedColumnName = "id")
    private DocumentVersion parentVersion;

    @Column(name = "approved_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date approvedDate;

    @Column(name = "effective_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date effectiveDate;
    
    //2021-02-04 추가 : Retirement를 관리자에서만 컨트롤
    @Column(name = "retirement_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date retirementDate;

    @Column(name = "retirement")
    @ColumnDefault("0")
    private boolean retirement;

//    @OneToMany(mappedBy = "documentVersion")
//    private List<TrainingPeriod> trainingPeriods;

//    @Transient
//    private Date trainingStartDate;
//    @Transient
//    private Date trainingEndDate;

//    public Date getTrainingStartDate(Date baselineDate) {
//        if(effectiveDate.before(baselineDate)) {
//            log.debug("** 기준일:{} 이전에 Effective:{} 된 경우", baselineDate, effectiveDate);
//            return DateUtils.addDay(baselineDate, 1);
//        } else {
//            log.debug("** 기준일:{} 이후에 Effective:{} 된 경우", baselineDate, effectiveDate);
//            return DateUtils.addDay(effectiveDate, -57);
//        }
//    }
//
//    public Date getTrainingEndDate(Date baselineDate) {
//        if(effectiveDate.before(baselineDate)) {
//            log.debug("** 기준일:{} 이전에 Effective:{} 된 경우", baselineDate, effectiveDate);
//            return DateUtils.addDay(baselineDate, 57);
//        } else {
//            log.debug("** 기준일:{} 이후에 Effective:{} 된 경우", baselineDate, effectiveDate);
//            return DateUtils.addDay(effectiveDate, -1);
//        }
//    }
//
//    /**
//     * @param baselineDate
//     * @return -1 : 교육 기간 지남, 남음, 0 : Today까지, 1 : 교육 기간 남아 있음
//     */
//    public int diff(Date baselineDate) {
//        Date endDate = getTrainingEndDate(baselineDate);
//        Date now = new Date();
//        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/DD");
//        String strDate = dateFormat.format(now);
//        return endDate.compareTo(DateUtils.toDate(strDate, "yyyy/MM/DD"));
//    }


    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 15, nullable = false)
    private DocumentStatus status;

    @Transient
    private boolean trainingAll;
    @Transient
    private MultipartFile uploadSopDocFile;
    @Transient
    private MultipartFile uploadRfKorFile;
    @Transient
    private MultipartFile uploadHwpKorPdfFile;
    @Transient
    private MultipartFile uploadRfEngFile;
    @Transient
    private MultipartFile uploadHwpEngPdfFile;
    @Transient
    private SOPAction action;

    @Transient
    private String[] jdIds;
    
    //메일 알림 여부
    @Transient
    private Boolean notification;

//    /** ISO Option **/
//    @Column(name = "title", columnDefinition = "nvarchar(255)")
//    private String title;
//
//    @Column(name = "content", columnDefinition = "nvarchar(MAX)")
//    private String content;
//
////    @Column(name = "deleted")
////    private boolean deleted;
//
//    @Column(name = "top_view_end_date")
//    @DateTimeFormat(pattern = "yyyy-MM-dd")
//    private Date topViewEndDate;
//
//    @Column(name = "status")
//    @Enumerated(EnumType.STRING)
//    private NoticeStatus noticeStatus;
//
//    @OneToMany(mappedBy = "notice")
//    @AuditMappedBy(mappedBy = "notice")
//    private List<NoticeAttachFile> attachFiles;
//
//    @Transient
//    private List<String> removeFiles;
//    /** ISO Option end **/


    /** sop file **/
    @Column(name = "file_name", columnDefinition = "nvarchar(255)")
    private String fileName;

    @Column(name = "original_file_name", columnDefinition = "nvarchar(255)")
    private String originalFileName;

    @Column(name = "file_type", columnDefinition = "nvarchar(255)")
    private String fileType;

    @Column(name = "file_size")
    private long fileSize;

    @Column(name = "ext", length = 5)
    private String ext;

    @Column(name = "total_page", length = 3)
    private Integer totalPage;
    /** sop file end **/

    /** rf eng file **/
    @Column(name = "rf_eng_file_name", columnDefinition = "nvarchar(255)")
    private String rfEngFileName;

    @Column(name = "rf_eng_hwp_pdf_file_name", columnDefinition = "nvarchar(255)")
    private String rfEngHwpPdfFileName;

    @Column(name = "rf_eng_original_file_name", columnDefinition = "nvarchar(255)")
    private String rfEngOriginalFileName;

    @Column(name = "rf_eng_file_type", columnDefinition = "nvarchar(255)")
    private String rfEngFileType;

    @Column(name = "rf_eng_file_size")
    private long rfEngFileSize;

    @Column(name = "rf_eng_ext", length = 5)
    private String rfEngExt;
    /** rf file end **/

    /** rf kor file **/
    @Column(name = "rf_kor_file_name", columnDefinition = "nvarchar(255)")
    private String rfKorFileName;

    @Column(name = "rf_kor_hwp_pdf_file_name", columnDefinition = "nvarchar(255)")
    private String rfKorHwpPdfFileName;

    @Column(name = "rf_kor_original_file_name", columnDefinition = "nvarchar(255)")
    private String rfKorOriginalFileName;

    @Column(name = "rf_kor_file_type", columnDefinition = "nvarchar(255)")
    private String rfKorFileType;

    @Column(name = "rf_kor_file_size")
    private long rfKorFileSize;

    @Column(name = "rf_kor_ext", length = 5)
    private String rfKorExt;
    /** rf file end **/

    @Builder
    public DocumentVersion(String id, Document document, String fileName, String originalFileName, String fileType, long fileSize, Integer totalPage, String version,
                           DocumentVersion parentVersion, Date effectiveDate, DocumentStatus status, String ext) {
        this.id = id;
        this.document = document;
        this.fileName = fileName;
        this.originalFileName = originalFileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.totalPage = totalPage;
        this.version = version;
        this.parentVersion = parentVersion;
        this.effectiveDate = effectiveDate;
        this.status = status;
        this.ext = ext;
    }

    public String getStrEffectiveDate() {
        if(ObjectUtils.isEmpty(effectiveDate)) {
            return "";
        } else {
            return DateUtils.format(effectiveDate, "dd-MMM-yyyy").toUpperCase();
        }
    }

    public String getDocInfo(){
        return String.format("[%s] %s v%s", document.getDocId(), document.getTitle(), version);
    }

    public String getMatrixRoles(){
        if(trainingMatrixList.stream().filter(m -> m.isTrainingAll()).count() > 0) {
            return "ALL";
        } else {
            return trainingMatrixList.stream().filter(m -> !m.isTrainingAll())
                    .map(m -> m.getJobDescription().getShortName()).collect(Collectors.joining(", "));
        }
    }

}
