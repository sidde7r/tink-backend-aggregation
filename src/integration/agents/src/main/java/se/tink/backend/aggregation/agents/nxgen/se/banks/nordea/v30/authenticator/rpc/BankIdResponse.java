package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankIdResponse {
    @JsonIgnore private static final Logger log = LoggerFactory.getLogger(BankIdResponse.class);
    @JsonProperty private String error;
    @JsonProperty private String code;

    public String getError() {
        return error;
    }

    public String getCode() {
        return code;
    }

    public BankIdStatus getBankIdStatus() {
        String status = getError();
        Preconditions.checkState(status != null, "BankID status was null");
        switch (status.toLowerCase()) {
            case NordeaSEConstants.BankIdStatus.OUTSTANDING_TRANSACTION:
                return BankIdStatus.WAITING;

            case NordeaSEConstants.BankIdStatus.USER_SIGN:
                return BankIdStatus.WAITING;

            default:
                log.warn("Unknown bankID status: {}", status);
                return BankIdStatus.FAILED_UNKNOWN;
        }
    }
}
