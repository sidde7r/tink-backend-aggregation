package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import lombok.Builder;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Builder
@Getter
public class PaymentRequest {

    @JsonProperty @Builder.Default private String speed = NordeaBaseConstants.Transfer.SPEED;

    @JsonProperty @Builder.Default private String currency = NordeaBaseConstants.CURRENCY;

    @JsonProperty private double amount;

    @JsonProperty private String from;

    @JsonProperty("bank_name")
    private String bankName;

    @JsonProperty private String to;

    @JsonProperty("recipient_name")
    private String recipientName;

    @JsonProperty("to_account_number_type")
    private String toAccountNumberType;

    @JsonProperty private String message;
    @JsonProperty private String type;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "CET")
    @JsonProperty
    private Date due;

    @JsonProperty("from_account_number_type")
    private String fromAccountNumberType;

    @JsonIgnore private String id;

    @JsonProperty("own_message")
    private String ownMessage;

    private String reference;
}
