package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import lombok.Builder;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@Builder
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

    @JsonFormat(pattern = "yyyyMMdd", timezone = "Europe/Stockholm")
    private Date bookingDate;

    private String currency;
    private String forcableErrorsRC;
    private String payeeName;
    private String regNoFromExt;
    private boolean savePayee;
    private boolean sendMessageToReceiver;
    private boolean sendReceiptToFrom;
    private String textFrom;
    private String textTo;

    // Payment Specific
    private String cardType;
    private String creditorId;
    private String creditorName;
    private String creditorReference;
    private String messageToReceiverText;
    private String registerPayment;
}
