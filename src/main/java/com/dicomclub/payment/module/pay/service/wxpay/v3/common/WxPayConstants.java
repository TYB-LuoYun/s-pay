package com.dicomclub.payment.module.pay.service.wxpay.v3.common;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.time.FastDateFormat;

import java.text.Format;
import java.util.List;

/**
 * @author ftm
 * @date 2023/3/21 0021 16:08
 */
public class WxPayConstants {
    public static final Format QUERY_COMMENT_DATE_FORMAT = FastDateFormat.getInstance("yyyyMMddHHmmss");

    public WxPayConstants() {
    }

    public static class ReceiverType {
        public static final String MERCHANT_ID = "MERCHANT_ID";
        public static final String PERSONAL_WECHATID = "PERSONAL_WECHATID";
        public static final String PERSONAL_OPENID = "PERSONAL_OPENID";
        public static final String PERSONAL_SUB_OPENID = "PERSONAL_SUB_OPENID";

        public ReceiverType() {
        }
    }

    public static class RefundStatus {
        public static final String SUCCESS = "SUCCESS";
        public static final String REFUND_CLOSE = "REFUNDCLOSE";
        public static final String PROCESSING = "PROCESSING";
        public static final String CHANGE = "CHANGE";

        public RefundStatus() {
        }
    }

    public static class WxpayTradeStatus {
        public static final String SUCCESS = "SUCCESS";
        public static final String PAY_ERROR = "PAYERROR";
        public static final String USER_PAYING = "USERPAYING";
        public static final String CLOSED = "CLOSED";
        public static final String NOTPAY = "NOTPAY";
        public static final String REFUND = "REFUND";
        public static final String REVOKED = "REVOKED";

        public WxpayTradeStatus() {
        }
    }

    public static class RefundChannel {
        public static final String ORIGINAL = "ORIGINAL";
        public static final String BALANCE = "BALANCE";
        public static final String OTHER_BALANCE = "OTHER_BALANCE";
        public static final String OTHER_BANKCARD = "OTHER_BANKCARD";

        public RefundChannel() {
        }
    }

    public static class RefundAccountSource {
        public static final String RECHARGE_FUNDS = "REFUND_SOURCE_RECHARGE_FUNDS";
        public static final String UNSETTLED_FUNDS = "REFUND_SOURCE_UNSETTLED_FUNDS";

        public RefundAccountSource() {
        }
    }

    public static class ResultCode {
        public static final String SUCCESS = "SUCCESS";
        public static final String FAIL = "FAIL";

        public ResultCode() {
        }
    }

    public static class LimitPay {
        public static final String NO_CREDIT = "no_credit";

        public LimitPay() {
        }
    }

    public static class SignType {
        public static final String HMAC_SHA256 = "HMAC-SHA256";
        public static final String MD5 = "MD5";
        public static final List<String> ALL_SIGN_TYPES = Lists.newArrayList(new String[]{"HMAC-SHA256", "MD5"});

        public SignType() {
        }
    }

    public static class AccountType {
        public static final String BASIC = "Basic";
        public static final String OPERATION = "Operation";
        public static final String FEES = "Fees";

        public AccountType() {
        }
    }

    public static class TradeType {
        public static final String NATIVE = "NATIVE";
        public static final String APP = "APP";
        public static final String JSAPI = "JSAPI";
        public static final String MWEB = "MWEB";
        public static final String MICROPAY = "MICROPAY";

        public TradeType() {
        }

//        public abstract static class Specific<R> {
//            public static WxPayConstants.TradeType.Specific<WxPayNativeOrderResult> NATIVE = new WxPayConstants.TradeType.Specific<WxPayNativeOrderResult>() {
//                public String getType() {
//                    return "NATIVE";
//                }
//            };
//            public static WxPayConstants.TradeType.Specific<WxPayAppOrderResult> APP = new WxPayConstants.TradeType.Specific<WxPayAppOrderResult>() {
//                public String getType() {
//                    return "APP";
//                }
//            };
//            public static WxPayConstants.TradeType.Specific<WxPayMpOrderResult> JSAPI = new WxPayConstants.TradeType.Specific<WxPayMpOrderResult>() {
//                public String getType() {
//                    return "JSAPI";
//                }
//            };
//            public static WxPayConstants.TradeType.Specific<WxPayMwebOrderResult> MWEB = new WxPayConstants.TradeType.Specific<WxPayMwebOrderResult>() {
//                public String getType() {
//                    return "MWEB";
//                }
//            };
//            public static WxPayConstants.TradeType.Specific<WxPayMicropayResult> MICROPAY = new WxPayConstants.TradeType.Specific<WxPayMicropayResult>() {
//                public String getType() {
//                    return "MICROPAY";
//                }
//            };
//
//            public abstract String getType();
//
//            private Specific() {
//            }
//        }
    }

    public static class BillType {
        public static final String MCHT = "MCHT";
        public static final String ALL = "ALL";
        public static final String SUCCESS = "SUCCESS";
        public static final String REFUND = "REFUND";
        public static final String RECHARGE_REFUND = "RECHARGE_REFUND";

        public BillType() {
        }
    }

    public static class TarType {
        public static final String GZIP = "GZIP";

        public TarType() {
        }
    }

    public static class CheckNameOption {
        public static final String NO_CHECK = "NO_CHECK";
        public static final String FORCE_CHECK = "FORCE_CHECK";

        public CheckNameOption() {
        }
    }
}
