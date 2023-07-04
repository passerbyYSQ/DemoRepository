package top.ysqorz.r2dbc.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 所有持久化PO的公共字段
 */
@Data
@EqualsAndHashCode
public class BaseEntity implements Serializable {
    /**
     * 记录唯一标识
     */
    @Id
    private String id;

    /**
     * 创建时间
     */
    @Column("create_time")
    private LocalDateTime createTime;
}
