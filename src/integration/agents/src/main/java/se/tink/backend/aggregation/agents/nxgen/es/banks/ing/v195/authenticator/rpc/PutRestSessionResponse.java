package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PutRestSessionResponse {

    private String ticket;

    private Long timeoutInSeconds;

    private String rememberMeToken;

    private String resultMessage;

    public String getTicket() throws LoginException {
        if (!Optional.ofNullable(ticket).isPresent()) {
            throw LoginError.NOT_SUPPORTED.exception();
        }
        return ticket;
    }

    public Long getTimeoutInSeconds() {
        return timeoutInSeconds;
    }

    public String getRememberMeToken() {
        return rememberMeToken;
    }
}
