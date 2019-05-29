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
public class ResultSignResponse {
    @JsonIgnore private static final Logger log = LoggerFactory.getLogger(BankIdResponse.class);
    @JsonProperty private String status;

    @JsonProperty("signature_state")
    private String signatureState;

    @JsonProperty("order_reference")
    private String orderReference;

    public BankIdStatus getBankIdStatus() {
        switch (status.toUpperCase()) {
            case NordeaSEConstants.BankIdStatus.PENDING:
                return BankIdStatus.WAITING;
            case NordeaSEConstants.BankIdStatus.SIGN_PENDING:
                return BankIdStatus.WAITING;
            case NordeaSEConstants.BankIdStatus.OK:
                return BankIdStatus.DONE;
            case NordeaSEConstants.BankIdStatus.CANCELLED:
                return BankIdStatus.CANCELLED;
            default:
                log.warn("Unknown bankID status: {}", status);
                return BankIdStatus.FAILED_UNKNOWN;
        }
    }
}
