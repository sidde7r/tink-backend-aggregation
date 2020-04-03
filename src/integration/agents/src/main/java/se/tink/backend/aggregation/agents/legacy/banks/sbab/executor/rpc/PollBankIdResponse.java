package se.tink.backend.aggregation.agents.banks.sbab.executor.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.banks.sbab.SBABConstants.BankId;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PollBankIdResponse {
    private static final Logger log = LoggerFactory.getLogger(PollBankIdResponse.class);

    private String id;
    private String status;
    private String reference;

    @JsonIgnore
    public BankIdStatus getBankIdStatus() {
        switch (status.toUpperCase()) {
            case BankId.SUCCESS:
                return BankIdStatus.DONE;
            case BankId.STARTED:
                return BankIdStatus.WAITING;
            case BankId.CANCELED:
                return BankIdStatus.CANCELLED;
            default:
                log.warn("Unknown bankID status: {}", status);
                return BankIdStatus.FAILED_UNKNOWN;
        }
    }
}
