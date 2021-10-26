package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys;

import com.google.common.collect.Sets;
import java.util.Set;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.DefaultResponseStatusHandler;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@AllArgsConstructor
public class RedsysHttpResponseStatusHandler extends DefaultResponseStatusHandler {

    private static final Set<ErrorResponseToExceptionMapping> ERROR_RESPONSE_TO_EXCEPTION_MAPPINGS =
            Sets.newHashSet(
                    new ErrorResponseToExceptionMapping(
                            500,
                            "Internal Server Error",
                            BankServiceError.BANK_SIDE_FAILURE.exception(),
                            false),
                    new ErrorResponseToExceptionMapping(
                            500,
                            "INTERNAL_SERVER_ERROR",
                            BankServiceError.BANK_SIDE_FAILURE.exception(),
                            false),
                    new ErrorResponseToExceptionMapping(
                            401, "TOKEN_EXPIRED", SessionError.SESSION_EXPIRED.exception(), true));

    private PersistentStorage persistentStorage;

    @Override
    public void handleResponse(HttpRequest httpRequest, HttpResponse httpResponse) {
        if (httpResponse.getStatus() >= 400) {
            ErrorResponse error = ErrorResponse.fromResponse(httpResponse);
            if (error != null) {
                ERROR_RESPONSE_TO_EXCEPTION_MAPPINGS.stream()
                        .filter(
                                o ->
                                        o.getHttpResponseStatus() == httpResponse.getStatus()
                                                && error.hasErrorCode(o.getErrorCode()))
                        .findAny()
                        .ifPresent(
                                o -> {
                                    if (o.isCleanPersistentStorage()) {
                                        persistentStorage.clear();
                                    }
                                    throw o.getException();
                                });
            }
        }
        super.handleResponse(httpRequest, httpResponse);
    }
}
