package top.ysqorz.batch.springbatch.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/9/22
 */
@Data
@EqualsAndHashCode
public class GroupMemberVO implements RowVO {
    @NotBlank
    private String account;
    @NotBlank
    private String groupName;

    @Override
    public String getRowKey() {
        return account + "_" + groupName;
    }
}
