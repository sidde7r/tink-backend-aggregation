package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.filter;

import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.MobileHelloResponseEntity;
import se.tink.backend.aggregation.nxgen.http.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;

public class IngHttpFilter extends Filter {

    @Override
    public HttpResponse handle(HttpRequest httpRequest) throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);
        checkUnavailable(response);
        check503(response);
        return response;
    }

    private void check503(final HttpResponse httpResponse) {
        int httpStatus = httpResponse.getStatus();
        if (httpStatus == 503 && isBankServiceErrorResponseBody(httpResponse)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }
    }

    // Response body looks like this when we had the outage.
    //    {
    //        "mobileResponse": {
    //            "returnCode": "OK",
    //            "sessionData": {
    //                "balanceBeforeLogon": "0",
    //                "twitterLink": "0",
    //                "pbMStatus": "0",
    //                "oldLogonStatus": "0",
    //                "facebookLink": "0",
    //                "oldTokenLogonStatus": "0",
    //                "logonStatus": "0",
    //                "enrollStatus": "0",
    //                "oldEnrollStatus": "0",
    //                "tokenLogon": "0"
    //            }
    //        }
    //    }
    private boolean isBankServiceErrorResponseBody(final HttpResponse httpResponse) {
        try {
            MobileHelloResponseEntity response = httpResponse.getBody(MobileHelloResponseEntity.class);
            if (response.getReturnCode().equalsIgnoreCase("ok")
                    && response.getSessionData().getBalanceBeforeLogon().equalsIgnoreCase("0")
                    && response.getSessionData().getTwitterLink().equalsIgnoreCase("0")
                    && response.getSessionData().getPbMStatus().equalsIgnoreCase("0")
                    && response.getSessionData().getOldLogonStatus().equalsIgnoreCase("0")
                    && response.getSessionData().getFacebookLink().equalsIgnoreCase("0")
                    && response.getSessionData().getOldTokenLogonStatus().equalsIgnoreCase("0")
                    && response.getSessionData().getLogonStatus().equalsIgnoreCase("0")
                    && response.getSessionData().getEnrollStatus().equalsIgnoreCase("0")
                    && response.getSessionData().getOldEnrollStatus().equalsIgnoreCase("0")
                    && response.getSessionData().getTokenLogon().equalsIgnoreCase("0")) {
                return true;
            }
        } catch (HttpClientException e) {
            // Means we could not parse the response to the class we wanted, i.e. Not a bank serive error.
        }
        return false;
    }

    private void checkUnavailable(final HttpResponse httpResponse) {
        int httpStatus = httpResponse.getStatus();
        String responseBody = httpResponse.getBody(String.class);
        boolean isUnavailable = responseBody.toLowerCase().contains("unavailable");
        if (httpStatus >= 500 && isUnavailable) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }
    }
}
