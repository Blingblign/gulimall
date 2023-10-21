package com.zzclearning.gulimall.ware.constant;

/**
 * @author bling
 * @create 2022-10-30 21:30
 */
public class WareConstant {
    public enum PurchaseType {
        CREATED(0,"新建"),ASSIGNED(1,"已分配"),RECEIVED(2,"已领取"),FINISHED(3,"已完成"),ERRORED(4,"有异常");
        private final Integer code;
        private final String msg;
        PurchaseType(Integer code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public Integer getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }
    public enum PurchaseDetailType {
        CREATED(0,"新建"),ASSIGNED(1,"已分配"),RECEIVED(2,"正在采购"),FINISHED(3,"已完成"),ERRORED(4,"采购失败");
        private final Integer code;
        private final String msg;
        PurchaseDetailType(Integer code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public Integer getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }
}
