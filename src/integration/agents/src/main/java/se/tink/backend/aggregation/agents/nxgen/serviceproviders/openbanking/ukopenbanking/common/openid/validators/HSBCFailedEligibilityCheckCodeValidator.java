package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.validators;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public interface HSBCFailedEligibilityCheckCodeValidator {
    static boolean validate(HttpResponse httpResponse) {
        boolean isBadRequestStatus = httpResponse.getStatus() == HttpStatus.SC_BAD_REQUEST;
        if (!isBadRequestStatus) {
            return false;
        }
        ErrorResponse errorResponse = httpResponse.getBody(ErrorResponse.class);
        return errorResponse.getErrorCodes().contains("UK.HSBC.FailedEligibilityCheck");
    }
}
