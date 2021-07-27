package com.payline.payment.alipay.bean.response;

import com.payline.payment.alipay.bean.object.Trade;
import com.payline.payment.alipay.exception.InvalidDataException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class NotificationMessage {
    private String currency;
    private String notifyId;
    private Date notifyTime;
    private String outTradeNo;
    private String totalFee;
    private String tradNo;
    private Trade.TradeStatus tradeStatus;

    public NotificationMessage(String currency, String notifyId, Date notifyTime, String outTradeNo, String totalFee, String tradNo, Trade.TradeStatus tradeStatus) {
        this.currency = currency;
        this.notifyId = notifyId;
        this.notifyTime = notifyTime;
        this.outTradeNo = outTradeNo;
        this.totalFee = totalFee;
        this.tradNo = tradNo;
        this.tradeStatus = tradeStatus;
    }

    public String getCurrency() {
        return currency;
    }

    public String getNotifyId() {
        return notifyId;
    }

    public Date getNotifyTime() {
        return notifyTime;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public String getTotalFee() {
        return totalFee;
    }

    public String getTradNo() {
        return tradNo;
    }

    public Trade.TradeStatus getTradeStatus() {
        return tradeStatus;
    }

    public static NotificationMessage fromMap(Map<String, String> map) {
        try {
            String currency = map.get("currency");
            String notifyId = map.get("notify_id");
            Date notifyTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(map.get("notify_time"));
            String outTradeNo = map.get("out_trade_no");
            String totalFee = map.get("total_fee");
            String tradNo = map.get("trade_no");
            Trade.TradeStatus tradeStatus = Trade.TradeStatus.valueOf(map.get("trade_status"));

            return new NotificationMessage(currency, notifyId,notifyTime,outTradeNo,totalFee,tradNo,tradeStatus);
        } catch (ParseException e) {
            throw new InvalidDataException("bad notify_time: " + map.get("notify_time"));
        }
    }
}
