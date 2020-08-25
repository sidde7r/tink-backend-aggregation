package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ValidatePaymentDateRequest {
    @JsonProperty("BookingDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date bookingDate;

    @JsonProperty("CountryCode")
    private String countryCode;

    @JsonProperty("IsCurrencyTransaction")
    private boolean isCurrencyTransaction;

    @JsonProperty("PayType")
    private String payType;

    @JsonProperty("ReceiverAccount")
    private String receiverAccount;

    @JsonProperty("TransferType")
    private String transferType;

    public ValidatePaymentDateRequest(
            Date bookingDate,
            String countryCode,
            boolean isCurrencyTransaction,
            String payType,
            String receiverAccount,
            String transferType) {
        this.bookingDate = bookingDate;
        this.countryCode = countryCode;
        this.isCurrencyTransaction = isCurrencyTransaction;
        this.payType = payType;
        this.receiverAccount = receiverAccount;
        this.transferType = transferType;
    }
}
