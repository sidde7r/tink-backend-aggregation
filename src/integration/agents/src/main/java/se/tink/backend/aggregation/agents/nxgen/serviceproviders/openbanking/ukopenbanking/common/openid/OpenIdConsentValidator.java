package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid;

import java.util.function.Predicate;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public interface OpenIdConsentValidator {

    static boolean hasInvalidConsent(HttpResponse response) {
        if (Validator.STATUS.predicate().negate().test(response)) {
            return false;
        }
        return Validator.ERROR_CODE
                .predicate()
                .and(Validator.ERROR_MESSAGE.predicate())
                .test(response);
    }

    enum Validator {
        STATUS(
                response -> {
                    int status = response.getStatus();
                    return status == HttpStatus.SC_FORBIDDEN || status == HttpStatus.SC_BAD_REQUEST;
                }),
        ERROR_CODE(
                response -> {
                    ErrorResponse errorBody = response.getBody(ErrorResponse.class);
                    return errorBody.hasErrorCode("UK.OBIE.Resource.NotFound")
                            || errorBody.hasErrorCode("UK.OBIE.Resource.InvalidConsentStatus");
                }),
        ERROR_MESSAGE(
                response -> {
                    ErrorResponse errorBody = response.getBody(ErrorResponse.class);
                    return errorBody.messageContains("Consent");
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
