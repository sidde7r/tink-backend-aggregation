package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@Setter
@JsonObject
public class BusinessDataEntity {

    @JsonProperty("EInvoiceMarking")
    private boolean eInvoiceMarking;

    private String accountNameFrom;
    private String accountNameTo;
    private String accountNoExtFrom;
    private String accountNoIntFrom;
    private String accountNoIntTo;
    private String accountNoToExt;
    private String accountProductFrom;
    private boolean allowDuplicateTransfer;
    private double amount;
    private String bankName;
    private String bookingDate;
    private String currency;
    private String forcableErrorsRC;
    private String payeeName;
    private String regNoFromExt;
    private boolean savePayee;
    private boolean sendMessageToReceiver;
    private boolean sendReceiptToFrom;
    private String textFrom;
    private String textTo;

    public BusinessDataEntity seteInvoiceMarking(boolean eInvoiceMarking) {
        this.eInvoiceMarking = eInvoiceMarking;
        return this;
    }

    public BusinessDataEntity setAccountNameFrom(String accountNameFrom) {
        this.accountNameFrom = accountNameFrom;
        return this;
    }

    public BusinessDataEntity setAccountNameTo(String accountNameTo) {
        this.accountNameTo = accountNameTo;
        return this;
    }

    public BusinessDataEntity setAccountNoExtFrom(String accountNoExtFrom) {
        this.accountNoExtFrom = accountNoExtFrom;
        return this;
    }

    public BusinessDataEntity setAccountNoIntFrom(String accountNoIntFrom) {
        this.accountNoIntFrom = accountNoIntFrom;
        return this;
    }

    public BusinessDataEntity setAccountNoIntTo(String accountNoIntTo) {
        this.accountNoIntTo = accountNoIntTo;
        return this;
    }

    public BusinessDataEntity setForcableErrorsRC(String forcableErrorsRC) {
        this.forcableErrorsRC = forcableErrorsRC;
        return this;
    }

    public BusinessDataEntity setAccountNoToExt(String accountNoToExt) {
        this.accountNoToExt = accountNoToExt;
        return this;
    }

    public BusinessDataEntity setAccountProductFrom(String accountProductFrom) {
        this.accountProductFrom = accountProductFrom;
        return this;
    }

    public BusinessDataEntity setAllowDuplicateTransfer(boolean allowDuplicateTransfer) {
        this.allowDuplicateTransfer = allowDuplicateTransfer;
        return this;
    }

    public BusinessDataEntity setAmount(double amount) {
        this.amount = amount;
        return this;
    }

    public BusinessDataEntity setBankName(String bankName) {
        this.bankName = bankName;
        return this;
    }

    public BusinessDataEntity setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
        return this;
    }

    public BusinessDataEntity setCurrency(String currency) {
        this.currency = currency;
        return this;
    }

    public BusinessDataEntity setPayeeName(String payeeName) {
        this.payeeName = payeeName;
        return this;
    }

    public BusinessDataEntity setRegNoFromExt(String regNoFromExt) {
        this.regNoFromExt = regNoFromExt;
        return this;
    }

    public BusinessDataEntity setSavePayee(boolean savePayee) {
        this.savePayee = savePayee;
        return this;
    }

    public BusinessDataEntity setSendMessageToReceiver(boolean sendMessageToReceiver) {
        this.sendMessageToReceiver = sendMessageToReceiver;
        return this;
    }

    public BusinessDataEntity setSendReceiptToFrom(boolean sendReceiptToFrom) {
        this.sendReceiptToFrom = sendReceiptToFrom;
        return this;
    }

    public BusinessDataEntity setTextFrom(String textFrom) {
        this.textFrom = textFrom;
        return this;
    }

    public BusinessDataEntity setTextTo(String textTo) {
        this.textTo = textTo;
        return this;
    }
}
