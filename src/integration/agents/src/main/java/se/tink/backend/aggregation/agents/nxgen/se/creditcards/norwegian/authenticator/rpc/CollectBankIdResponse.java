package se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.authenticator.rpc;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.NorwegianConstants.BankIdProgressStatus;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CollectBankIdResponse {

    private static final Logger LOGGER = LoggerFactory.getLogger(CollectBankIdResponse.class);

    private String progressStatus;
    private String completeUrl;

    public String getCompleteUrl() {
        return completeUrl;
    }

    public BankIdStatus getBankIdStatus() {
        final String status = Optional.ofNullable(progressStatus).orElse("null");

        switch (status.toUpperCase()) {
            case BankIdProgressStatus.COMPLETE:
                return BankIdStatus.DONE;
            case BankIdProgressStatus.OUTSTANDING_TRANSACTION:
            case BankIdProgressStatus.USER_SIGN:
                return BankIdStatus.WAITING;
            case BankIdProgressStatus.NO_CLIENT:
                return BankIdStatus.NO_CLIENT;
            default:
                LOGGER.warn("Unknown BankID status: {}", status);
                return BankIdStatus.FAILED_UNKNOWN;
        }
    }
}
