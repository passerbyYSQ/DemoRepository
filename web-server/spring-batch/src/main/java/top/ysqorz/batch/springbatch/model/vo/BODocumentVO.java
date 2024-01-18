package top.ysqorz.batch.springbatch.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ...
 *
 * @author yaoshiquan
 * @date 2023/10/11
 */
@Data
@EqualsAndHashCode
public class BODocumentVO implements RowVO {

    /**
     * 类型
     */
    private String type;

    /**
     * 名称
     */
    private String name;

    /**
     * 版本
     */
    private String rev;

    /**
     * 新名称
     */
    private String newName;

    /**
     * 新版本
     */
    private String newRev;

    /**
     * 管理策略
     */
    private String policy;

    /**
     * 状态
     */
    private String state;

    /**
     * 库
     */
    private String vault;

    /**
     * 所有人
     */
    private String owner;

    /**
     * 描述
     */
    private String description;

    /**
     *
     */
    private String clau;

    /**
     * 指定用户
     */
    private String designatedUser;

    /**
     *
     */
    private String CUSSignatureJD;

    /**
     *
     */
    private String CUSSignatureSH2;

    /**
     *
     */
    private String CUSSignatureSH;

    /**
     * 访问类型
     */
    private String accessType;

    /**
     * 签入原因
     */
    private String checkinReason;

    /**
     * 言言
     */
    private String language;

    /**
     * 是否是型号对象
     */
    private String isVersionObject;

    /**
     * 移动文件到型号
     */
    private String moveFilesToVersion;

    /**
     * 停止版本控制
     */
    private String suspendVersioning;

    /**
     * 标题
     */
    private String title;

    /**
     * 发起人
     */
    private String originator;

    /**
     *
     */
    private String CUSSignatureGYDate;

    /**
     *
     */
    private String CUSSignatureBZH;

    /**
     *
     */
    private String CUSSignatureGY;

    /**
     *
     */
    private String CUSSignatureBZHDate;

    /**
     *
     */
    private String CUSSignatureBZ;

    /**
     *
     */
    private String CUSSignatureBZDate;

    /**
     *
     */
    private String CUSSignaturePZ;

    /**
     *
     */
    private String CUSSignatureJD2;

    /**
     *
     */
    private String CUSSignatureJDDate;

    /**
     *
     */
    private String CUSSignatureJD2Date;

    /**
     *
     */
    private String CUSSignatureSH2Date;

    /**
     *
     */
    private String CUSSignatureSHDate;

    /**
     *
     */
    private String CUSSignaturePZDate;

    /**
     * 型号
     */
    private String version;

    /**
     *
     */
    private String ZTEDocCatagory;

    /**
     * 文件版本
     */
    private String fileVersion;

    /**
     * 版本日期
     */
    private String versionDate;

    /**
     * 主键
     */
    private String primaryKey;

    /**
     * 第二键
     */
    private String secondaryKeys;

    /**
     * cad类型
     */
    private String CADType;

    @Override
    public String getRowKey() {
        return name + "_" + rev;
    }

}
