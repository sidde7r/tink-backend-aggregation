package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import lombok.Builder;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@Builder
@JsonObject
public class ValidatePaymentDateRequest {
    @JsonProperty("BookingDate")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Europe/Stockholm")
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
}
