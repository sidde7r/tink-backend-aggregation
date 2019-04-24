package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.authenticator.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.rpc.BankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants.BankIdResponseStatus;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.authenticator.entities.LoginEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankIdCollectResponse {
    private static final Logger LOGGER = LoggerFactory.getLogger(BankIdResponse.class);

    private List<Object> recommendedTargetCustomers;
    private String name;
    private String state;
    private List<LoginEntity> logins;
    private String transactionId;

    public List<Object> getRecommendedTargetCustomers() {
        return Optional.ofNullable(recommendedTargetCustomers).orElseGet(Collections::emptyList);
    }

    public String getName() {
        return name;
    }

    public String getState() {
        return state;
    }

    public List<LoginEntity> getLogins() {
        return Optional.ofNullable(logins).orElseGet(Collections::emptyList);
    }

    public String getTransactionId() {
        return transactionId;
    }

    public BankIdStatus getBankIdStatus() {
        final BankIdResponseStatus status = BankIdResponseStatus.fromStatusCode(getState());

        switch (status) {
            case COMPLETE:
                return BankIdStatus.DONE;
            case ALREADY_IN_PROGRESS:
            case USER_SIGN:
            case STARTED:
                return BankIdStatus.WAITING;
            case NO_CLIENT:
                return BankIdStatus.NO_CLIENT;
            case CANCELLED:
                return BankIdStatus.CANCELLED;
            case TIMEOUT:
                return BankIdStatus.TIMEOUT;
            default:
                LOGGER.warn("Unknown bankID status: {}", status);
                return BankIdStatus.FAILED_UNKNOWN;
        }
    }
}
