package com.cauh.iso.domain;

import com.cauh.common.entity.BaseEntity;
import com.cauh.iso.domain.constant.CategoryType;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@Entity
@Table(name = "s_category", uniqueConstraints = @UniqueConstraint(columnNames = {"short_name"}))
@Slf4j
@ToString(of = {"id", "name"})
@EqualsAndHashCode(of = {"id"}, callSuper = false)
public class Category extends BaseEntity implements Serializable {
    private static final long serialVersionUID = -6871231388579015424L;
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "id", length = 40)
    private String id;

    @Column(name = "short_name", length = 10, nullable = false)
    private String shortName;

    @Column(name = "name", columnDefinition = "nvarchar(255)", nullable = false)
    private String name;

    @Column(name = "deleted")
    private boolean deleted;

    @Transient
    private boolean readonly;

    @Builder
    public Category(String id, String shortName, String name) {
        this.id = id;
        this.shortName = shortName;
        this.name = name;
    }
}
