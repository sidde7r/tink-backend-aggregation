package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.executors.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.authenticator.rpc.BankIdResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SignatureResponse {
    @JsonIgnore private static final Logger log = LoggerFactory.getLogger(BankIdResponse.class);

    @JsonProperty("signature_state")
    private String signatureState;

    @JsonProperty("order_reference")
    private String orderReference;

    @JsonProperty("order_status")
    private String orderStatus;

    @JsonProperty("text_to_be_signed")
    private String textToBeSigned;

    public BankIdStatus getSignatureState() {
        switch (orderStatus.toUpperCase()) {
            case NordeaSEConstants.BankIdStatus.PENDING:
                return BankIdStatus.WAITING;

            default:
                log.warn("Unknown bankID status: {}", orderStatus);
                return BankIdStatus.FAILED_UNKNOWN;
        }
    }

    public String getOrderReference() {
        return orderReference;
    }
}
