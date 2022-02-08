package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid;

import java.util.function.Predicate;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.UkObErrorResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public interface OpenIdConsentValidator {

    static boolean hasValidConsent(HttpResponse response) {
        return !hasInvalidConsent(response);
    }

    static boolean hasInvalidConsent(HttpResponse response) {
        if (Validator.STATUS.predicate().negate().test(response)) {
            return false;
        }
        if (Validator.EMPTY_BODY.predicate().negate().test(response)) {
            return false;
        }

        return Validator.ERROR_CODE
                .predicate()
                .and(Validator.ERROR_MESSAGE.predicate())
                .test(response);
    }

    enum Validator {
        EMPTY_BODY(HttpResponse::hasBody),
        STATUS(
                response -> {
                    int status = response.getStatus();
                    return status == HttpStatus.SC_FORBIDDEN || status == HttpStatus.SC_BAD_REQUEST;
                }),
        ERROR_CODE(
                response -> {
                    UkObErrorResponse errorBody = response.getBody(UkObErrorResponse.class);
                    return errorBody.hasErrorCode(ErrorCodes.NOT_FOUND)
                            || errorBody.hasErrorCode(ErrorCodes.INVALID_CONSENT_STATUS)
                            || errorBody.hasErrorCode(ErrorCodes.REAUTHENTICATE);
                }),
        ERROR_MESSAGE(
                response -> {
                    UkObErrorResponse errorBody = response.getBody(UkObErrorResponse.class);
                    return errorBody.messageContains("Consent")
                            || errorBody.messageContains("Reauthenticate");
                });

        private final Predicate<HttpResponse> responsePredicate;

        Validator(Predicate<HttpResponse> responsePredicate) {
            this.responsePredicate = responsePredicate;
        }

        public Predicate<HttpResponse> predicate() {
            return responsePredicate;
        }
    }
}
