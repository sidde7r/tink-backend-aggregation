package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.utils;

import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class TriodosUtils {

    public static void checkErrorResponseBodyType(HttpResponse httpResponse) {
        if (!MediaType.APPLICATION_JSON_TYPE.isCompatible(httpResponse.getType())) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(
                    "Invalid error response format : " + httpResponse.getBody(String.class));
        }
    }
}
