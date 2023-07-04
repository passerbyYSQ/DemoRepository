package top.ysqorz.common.constant;

import top.ysqorz.common.enumeration.StatusCode;

import static top.ysqorz.common.enumeration.StatusCode.$;

/**
 * 注意StatusCode中的code最好全局唯一，但这并不是强制的。所以在定义具体的StatusCode时
 * 最好通过idea全局搜索一下code是否是否已使用
 *
 * 例如：如果想确认5603的code是否已经被使用，可以全局搜索"$(5603"关键字来匹配确认是否已被使用
 *     StatusCode ACCOUNT_PASSWORD_INCORRECT = $(5603, "base.5603");
 *
 * idea匹配替换msgKey的快捷方式：
 * 正则匹配：\$\((.+?), +?"(.+?)"\)
 * 替换：\$($1, "base.$1")
 */
public interface BaseStatusCode {
    StatusCode SUCCESS = $(0, "base.0");
    StatusCode BUSINESS_FAILED = $(-1, "base.-1");

    /**
     * 21xx 无业务的通用常量
     */
    StatusCode TRUE = $(2100, "base.2100");
    StatusCode FALSE = $(2101, "base.2101");

    /**
     * 4xxx
     */
    StatusCode PARAM_INVALID = $(4000, "base.4000");
    /**
     * 401x 认证
     */
    StatusCode NO_AUTHENTICATION = $(4010, "base.4010");
    StatusCode TOKEN_EXPIRED = $(4011, "base.4011");
    StatusCode USER_INFO_EMPTY = $(4012, "base.4012");
    StatusCode ACCOUNT_EXPIRED = $(4013, "base.4013");
    /**
     * 403x 权限
     */
    StatusCode NO_AUTHORIZATION = $(4030, "base.4030");
    StatusCode NOT_OWNER_DATA = $(4031, "base.4031");
    StatusCode NOT_ADMIN = $(4032, "base.4032");
    /**
     * 405x
     */
    StatusCode METHOD_NOT_ALLOW = $(4050, "base.4050");
    /**
     * 41xx 其他参数错误
     */
    StatusCode TOKEN_INVALID = $(4100, "base.4100");
    StatusCode MESSAGE_NAME_INVALID = $(4101, "base.4101");
    StatusCode UNSUPPORTED_FILE_TYPE = $(4102, "base.4102");
    StatusCode UPLOADED_FILE_EMPTY = $(4103, "base.4103");
    StatusCode FILE_SIZE_EXCEED_LIMIT = $(4104, "base.4104");
    StatusCode MEDIA_TYPE_EMPTY = $(4105, "base.4105");
    StatusCode UNSUPPORTED_MEDIA_TYPE = $(4106, "base.4106");
    StatusCode FILE_NAME_INVALID = $(4107, "base.4107");
    StatusCode ROOT_BOM_NOT_EXIST = $(4108, "base.4108");
    StatusCode SET_CAPACITY_LESS_USED = $(4109, "base.4109");
    StatusCode CAPACITY_UNIT_INVALID = $(4110, "base.4110");
    StatusCode RESOURCE_CONSTRAINT_INVALID = $(4111, "base.4111");
    StatusCode EXPIRATION_INVALID = $(4112, "base.4112");
    StatusCode INSTALLATION_INVALID = $(4113, "base.4113");
    StatusCode VALUE_TYPE_INVALID = $(4114, "base.4114");

    /**
     * 5xxx
     */
    StatusCode SERVER_ERROR = $(5000, "base.5000");
    StatusCode UPDATE_FAILED = $(5001, "base.5001");
    StatusCode DATA_NOT_EXIT = $(5002, "base.5002");
    /**
     * 51xx UI
     */
    StatusCode UI_ERROR = $(5100, "base.5100");
    StatusCode OBJECT_META_INVALID = $(5101, "base.5101");
    StatusCode CLASS_META_INVALID = $(5102, "base.5102");
    StatusCode UI_DATA_INVALID = $(5103, "base.5103");
    StatusCode UI_SAVE_FAILED = $(5104, "base.5104");
    StatusCode UI_NOT_INIT = $(5105, "base.5105");
    StatusCode UI_INIT_FAILED = $(5106, "base.5106");
    StatusCode UI_OBJECT_INVALID = $(5107, "base.5107");
    StatusCode REQUIRED_ATTR_MISSING = $(5108, "base.5108");
    StatusCode ATTR_INVALID = $(5109, "base.5109");
    StatusCode CLASS_INVALID = $(5110, "base.5110");
    StatusCode UI_BUILD_FAILED = $(5111, "base.5111");
    StatusCode UNEDITABLE_ATTR = $(5112, "base.5112");
    StatusCode UI_OBJECT_NOT_INITIALIZED = $(5113, "base.5113");
    /**
     * 52xx 文件
     */
    StatusCode FILE_OPERATION_ERROR = $(5200, "base.5200");
    StatusCode FILE_NOT_EXIST = $(5201, "base.5201");
    StatusCode CREATE_DIR_FAILED = $(5202, "base.5202");
    StatusCode PERSONAL_STORAGE_SHORT = $(5203, "base.5203");
    StatusCode FILE_UPLOAD_FAILED = $(5204, "base.5204");
    StatusCode FILE_CONVERT_FAILED = $(5205, "base.5205");
    /**
     * 53xx IPC
     */
    StatusCode IPC_ERROR = $(5300, "base.5300");
    /**
     * 54xx 筛选器
     */
    StatusCode QUERY_FILTER_NOT_EXIST = $(5400, "base.5400");
    /**
     * 55xx 导入/导出
     */
    StatusCode UNSUPPORTED_EXPORT_OPERATION = $(5500, "base.5500");
    StatusCode UNSUPPORTED_IMPORT_OBJECT = $(5501, "base.5501");
    StatusCode IMPORT_FILE_PARSED_FAILED = $(5502, "base.5502");
    StatusCode IMPORT_FILE_CONTENT_EMPTY = $(5503, "base.5503");
    StatusCode FILE_IMPORTING = $(5504, "base.5504");
    StatusCode REQUIRED_COLUMN_MISSING = $(5505, "base.5505");
}
