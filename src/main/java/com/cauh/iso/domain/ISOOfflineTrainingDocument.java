package com.cauh.iso.domain;

import com.cauh.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@Entity
@Table(name = "s_offline_training_document")
@Slf4j
@ToString(of = {"id"})
@EqualsAndHashCode(of = {"id", "documentVersion"}, callSuper = false)
@SequenceGenerator(name = "OFF_TRAINING_ISO_SEQ_GENERATOR", sequenceName = "SEQ_OFF_TRAINING_ISO", initialValue = 1, allocationSize = 1)
@Audited(withModifiedFlag = true)
public class ISOOfflineTrainingDocument extends BaseEntity implements Serializable {
    private static final long serialVersionUID = -5898541634027846398L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "OFF_TRAINING_ISO_SEQ_GENERATOR")
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "iso_offline_training_id", referencedColumnName = "id")
    private ISOOfflineTraining isoOfflineTraining;

    @ManyToOne(optional = false)
    @JoinColumn(name = "iso_id", referencedColumnName = "id")
    private ISO iso;

    @Column(name = "hour", length = 4)
    private String hour;
}
