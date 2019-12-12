package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.detail;

import se.tink.backend.aggregation.nxgen.http.HttpResponse;

public class HttpResponseChecker {

    public static void checkIfSuccessfulResponse(
            HttpResponse response, int codeOfSuccess, String errorMessage) {
        int requestStatus = response.getStatus();
        if (requestStatus != codeOfSuccess) {
            throw new IllegalStateException(
                    String.format("%s. Error response code: %d", errorMessage, requestStatus));
        }
    }
}
