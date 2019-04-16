package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.authenticator.rpc;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.URL;

@SuppressWarnings("unused")
@JsonObject
public class BankIdCollectResponse {
    private static final Logger LOGGER = LoggerFactory.getLogger(BankIdCollectResponse.class);

    private String progressStatus;
    private URL completeUrl;

    public URL getCompleteUrl() {
        return completeUrl;
    }

    public BankIdStatus getBankIdStatus() {
        final String status = Optional.ofNullable(progressStatus).orElse("null");

        switch (status.toUpperCase()) {
            case "COMPLETE":
                return BankIdStatus.DONE;
            case "OUTSTANDING_TRANSACTION":
            case "USER_SIGN":
                return BankIdStatus.WAITING;
            case "NO_CLIENT":
                return BankIdStatus.NO_CLIENT;
            default:
                LOGGER.warn("Unknown BankID status: {}", status);
                return BankIdStatus.FAILED_UNKNOWN;
        }
    }
}
