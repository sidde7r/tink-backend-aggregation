package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants.NordeaBankIdStatus;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ResultSignResponse {
    @JsonIgnore private static final Logger log = LoggerFactory.getLogger(ResultSignResponse.class);
    @JsonProperty private String status;

    @JsonProperty("signature_state")
    private String signatureState;

    @JsonProperty("order_reference")
    private String orderReference;

    @JsonIgnore
    public BankIdStatus getBankIdStatus() {
        switch (status.toUpperCase()) {
            case NordeaBankIdStatus.PENDING:
            case NordeaBankIdStatus.SIGN_PENDING:
                return BankIdStatus.WAITING;
            case NordeaBankIdStatus.OK:
                return BankIdStatus.DONE;
            case NordeaBankIdStatus.CANCELLED:
                return BankIdStatus.CANCELLED;
            default:
                log.warn("Unknown bankID status: {}", status);
                return BankIdStatus.FAILED_UNKNOWN;
        }
    }
}
