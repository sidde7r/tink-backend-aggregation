package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.rpc;

import java.util.Optional;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

@JsonObject
public class InvalidPinResponse {

    private String pin;

    public static Optional<InvalidPinResponse> from(HttpResponseException e) {
        HttpResponse response = e.getResponse();
        if (response.getStatus() != HttpStatus.SC_BAD_REQUEST) {
            return Optional.empty();
        }
        return Optional.of(response.getBody(InvalidPinResponse.class))
                .filter(InvalidPinResponse::hasPinMessage);
    }

    private boolean hasPinMessage() {
        return pin != null;
    }

    public AuthenticationException exception(HttpResponseException e) {
        return LoginError.INCORRECT_CREDENTIALS.exception(
                new LocalizableKey(String.format(SdcConstants.Session.INVALID_LOGIN_MESSAGE, pin)),
                e);
    }
}
