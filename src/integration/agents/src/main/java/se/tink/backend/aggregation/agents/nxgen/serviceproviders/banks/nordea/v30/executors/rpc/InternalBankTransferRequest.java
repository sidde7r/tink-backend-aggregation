package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import lombok.Builder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Builder
public class InternalBankTransferRequest {
    @JsonProperty private int amount;

    @JsonProperty private String from;
    @JsonProperty private String to;

    @JsonProperty("own_message")
    private String message;

    @JsonProperty("to_account_number_type")
    @Builder.Default
    private String toAccountNumberType = NordeaBaseConstants.Transfer.TO_ACCOUNT_TYPE;

    @Builder.Default @JsonProperty private String speed = NordeaBaseConstants.Transfer.SPEED;
    @Builder.Default @JsonProperty private String type = NordeaBaseConstants.Transfer.OWN_TRANSFER;
    @Builder.Default @JsonProperty private String currency = NordeaBaseConstants.CURRENCY;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "CET")
    @JsonProperty
    private Date due;
}
