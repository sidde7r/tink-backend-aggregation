package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.rpc;

import java.util.Optional;
import org.eclipse.jetty.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.libraries.i18n.LocalizableKey;

@JsonObject
public class InvalidPinResponse {

    private String pin;

    public static Optional<InvalidPinResponse> from(HttpResponseException e) {
        HttpResponse response = e.getResponse();
        if (response.getStatus() != HttpStatus.BAD_REQUEST_400) {
            return Optional.empty();
        }
        return Optional.of(response.getBody(InvalidPinResponse.class))
                .filter(InvalidPinResponse::hasPinMessage);
    }

    private boolean hasPinMessage() {
        return pin != null;
    }

    public AuthenticationException exception() {
        return LoginError.INCORRECT_CREDENTIALS.exception(
                new LocalizableKey(String.format(SdcConstants.Session.INVALID_LOGIN_MESSAGE, pin)));
    }
}
