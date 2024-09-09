package com.atguigu.common.exception;

public enum BizCodeEnum {

    UNKNOW_EXCEPTION(10000,"系统未知异常"),
    VALID_EXCEPTION(10001,"参数格式校验失败"),
    VALID_SMS_CODE_EXCEPTION(10002,"短信验证码获取频率太高"),
    TO0_MANY_REQUEST(10003,"请求流量过大"),
    PRODUCT_UP_EXCEPTION(11000,"商品上架异常"),
    USER_EXIST_EXCEPTION(15001,"用户已存在"),
    PHONE_EXIST_EXCEPTION(15002,"手机号已存在"),
    NO_STOCK_EXCEPTION(21000,"商品库存不足"),
    LOGINACCT_PASSWORD_INVALID_EXCEPTION(15003,"账号或者密码错误");
    private int code;
    private String message;
    BizCodeEnum(int code,String msg){
        this.code=code;
        this.message=msg;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
