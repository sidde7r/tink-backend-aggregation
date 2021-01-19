package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.rpc;

import static io.vavr.Predicates.instanceOf;

import io.vavr.API;
import io.vavr.collection.List;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
public enum LoanDetailsErrorCode {
    UNKNOWN_ERROR("", "Unknown error has been appeared."),
    NOT_EXISTS("CPPT990E", "The loan is either not fully disbursed or is already paid off.");

    private final String errorCode;
    private final String message;

    LoanDetailsErrorCode(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public static LoanDetailsErrorCode getErrorCode(Throwable throwable) {
        return API.Match(throwable)
                .of(
                        API.Case(
                                API.$(instanceOf(HttpResponseException.class)),
                                () -> {
                                    HttpResponseException responseException =
                                            (HttpResponseException) throwable;
                                    ErrorResponse errorResponse =
                                            responseException
                                                    .getResponse()
                                                    .getBody(ErrorResponse.class);
                                    return List.of(values())
                                            .find(
                                                    code ->
                                                            code.errorCode.equals(
                                                                    errorResponse
                                                                            .getOperationResult()))
                                            .getOrElse(LoanDetailsErrorCode.UNKNOWN_ERROR);
                                }),
                        API.Case(
                                API.$(),
                                () -> {
                                    log.error("Unknown error has been occurred.", throwable);
                                    return LoanDetailsErrorCode.UNKNOWN_ERROR;
                                }));
    }

    public String getMessage() {
        return message;
    }

    public String getErrorCodeValue() {
        return errorCode;
    }
}
