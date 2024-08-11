package com.atguigu.common.constant;

/**
 * ClassName: WareConstant
 * Package: com.atguigu.common.constant
 * Description:
 *
 * @Author: 邓桂材
 * @Create: 2024/8/11 -17:53
 * @Version: v1.0
 */
public class WareConstant {
    public enum PurchaseStatusEnum{
        CREATED(0,"新建"),ASSIGNED(1,"已分配"),RECEIVED(2,"已领取"),FINISHED(3,"已完成"),HASERROR(4,"有异常");
        private int code;
        private String msg;

        PurchaseStatusEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }
    public enum PurchaseDetailStatusEnum{
        CREATED(0,"新建"),ASSIGNED(1,"已分配"),BUYING(2,"正在采购"),FINISHED(3,"已完成"),HASERROR(4,"采购失败");
        private int code;
        private String msg;

        PurchaseDetailStatusEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }
}
