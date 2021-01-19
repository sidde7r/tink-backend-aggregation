package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.rpc;

import static io.vavr.Predicates.instanceOf;

import io.vavr.API;
import io.vavr.collection.List;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
public enum AccountDetailsErrorCode {
    UNKNOWN_ERROR("");

    private final String errorCode;

    AccountDetailsErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public static AccountDetailsErrorCode getErrorCode(Throwable throwable) {
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
                                            .getOrElse(AccountDetailsErrorCode.UNKNOWN_ERROR);
                                }),
                        API.Case(
                                API.$(),
                                () -> {
                                    log.error("Unknown error has been occurred.", throwable);
                                    return AccountDetailsErrorCode.UNKNOWN_ERROR;
                                }));
    }
}
