package top.ysqorz.r2dbc.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("user")
@Data
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {
    @Column("username")
    private String username;

    @Column("email")
    private String email;
}
