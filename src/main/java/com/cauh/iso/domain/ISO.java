package com.cauh.iso.domain;

import com.cauh.common.entity.BaseEntity;
import com.cauh.iso.domain.constant.ISOType;
import com.cauh.iso.domain.constant.PostStatus;
import com.cauh.iso.domain.constant.TrainingType;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.envers.AuditMappedBy;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.ObjectUtils;

import javax.persistence.*;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Data
@NoArgsConstructor
@Entity
@Table(name = "s_iso")
@Slf4j
@ToString(of = {"id", "title"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
@Audited(withModifiedFlag = true)
public class ISO extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 765987308708693720L;
    //=======게시글 속성========
    @Id
    @Column(name = "id", length = 40)
    private String id;

    @Column(name = "title", columnDefinition = "nvarchar(255)", nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "nvarchar(MAX)", nullable = false)
    private String content;

    @Column(name = "deleted")
    private boolean deleted;

    @Column(name = "top_view_end_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date topViewEndDate;

    @Enumerated(EnumType.STRING)
    private ISOType isoType;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private PostStatus postStatus;

    //========================================

    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private ISOCategory category;

    //ISO 첨부 파일 (Training시, 강의 파일이 됨)
    @OneToMany(mappedBy = "iso")
    private List<ISOAttachFile> attachFiles;

    @Transient
    private List<String> removeFiles;

    @Transient
    private String uploadFileName;

    //강의 여부
    @ColumnDefault("0")
    private boolean training;



//    @ColumnDefault("0")
//    private boolean certification;
//
//    @Column(length = 20)
//    private String certificationHead;

    //정답 개수
    private Integer correctCount;

    //강의 active 여부
    @ColumnDefault("0")
    private boolean active;

    //=============강의 정보===============
    @Transient
    private String[] userIds;

    //ISO Training 배정 정보
    @OneToMany(mappedBy = "iso")
    private List<ISOTrainingMatrix> isoTrainingMatrix;

    //ISO Training 기간 정보
    @OneToMany(mappedBy = "iso")
    private List<ISOTrainingPeriod> isoTrainingPeriods;

    @Transient
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startDate;
    @Transient
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endDate;
    @Transient
    private boolean trainingAll;

    //퀴즈
    @Column(name = "quiz", columnDefinition = "nvarchar(MAX)")
    private String quiz;

    //퀴즈 임시저장 여부.
    private Boolean isQuizTemp;

    public String getTrainingDate() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        Optional<ISOTrainingPeriod> isoTrainingPeriodOptional = isoTrainingPeriods.stream()
                .filter(p -> !ObjectUtils.isEmpty(p))
                .filter(p -> p.getTrainingType() == TrainingType.SELF).findFirst();
        if(!isoTrainingPeriodOptional.isPresent()) {return "-";}
        ISOTrainingPeriod isoTrainingPeriod = isoTrainingPeriodOptional.get();
        startDate = isoTrainingPeriod.getStartDate();
        endDate = isoTrainingPeriod.getEndDate();
        return (startDate.compareTo(endDate) == 0) ? df.format(startDate) : df.format(startDate) + " ~ " + df.format(endDate);
    }

    public String getAttendee() {
        if(isoTrainingMatrix.stream().filter(tm -> tm.isTrainingAll()).count() > 0) {
            return "ALL";
        }

        long cnt = isoTrainingMatrix.stream().filter(tm -> tm.isTrainingAll() == false).count();
        return Long.toString(cnt);
    }

    @Builder
    public ISO(String title, String content, Date topViewEndDate, PostStatus postStatus) {
        this.title = title;
        this.content = content;
        this.topViewEndDate = topViewEndDate;
        this.postStatus = postStatus;
    }
}
