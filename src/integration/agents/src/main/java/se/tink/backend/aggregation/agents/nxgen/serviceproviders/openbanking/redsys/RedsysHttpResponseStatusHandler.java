package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys;

import com.google.common.collect.Sets;
import java.util.Set;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.DefaultResponseStatusHandler;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class RedsysHttpResponseStatusHandler extends DefaultResponseStatusHandler {

    private static final Set<String> BANK_TEMPORARY_UNAVAILABLE_ERROR_CODES =
            Sets.newHashSet("Internal Server Error", "INTERNAL_SERVER_ERROR");

    @Override
    public void handleResponse(HttpRequest httpRequest, HttpResponse httpResponse) {
        if (httpResponse.getStatus() == 500) {
            ErrorResponse error = ErrorResponse.fromResponse(httpResponse);
            if (error != null) {
                for (String code : BANK_TEMPORARY_UNAVAILABLE_ERROR_CODES) {
                    if (error.hasErrorCode(code)) {
                        throw BankServiceError.BANK_SIDE_FAILURE.exception();
                    }
                }
            }
        }
        super.handleResponse(httpRequest, httpResponse);
    }
}
