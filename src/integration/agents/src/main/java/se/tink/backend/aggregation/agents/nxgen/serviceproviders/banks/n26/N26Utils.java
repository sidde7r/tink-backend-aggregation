package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26;

import io.vavr.control.Either;
import io.vavr.control.Try;
import java.io.UnsupportedEncodingException;
import java.util.MissingResourceException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.errors.AgentBaseError;
import se.tink.backend.aggregation.agents.exceptions.errors.AgentError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.session.utils.UnknownError;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.Storage;

public class N26Utils {
    private static Logger logger = LoggerFactory.getLogger(N26Utils.class);

    public static <T> Try<T> handleUnsupportedEncodingException(UnsupportedEncodingException ex) {
        logger.error("Unable to encode {}", ex);
        return Try.failure(new IllegalStateException("Unable to encode ", ex));
    }

    public static Optional<AgentBaseError> mapError(
            HttpResponseException ex, ErrorResponse errorResponse) {

        String error = errorResponse.getError();
        if (N26Constants.Errors.continueList.contains(error)) {
            return Optional.empty();
        }

        Optional<AgentError> translate = N26Constants.Errors.errorsMap.translate(error);
        return translate
                .map(
                        a -> {
                            logger.error(
                                    "Unable to authenticate error {} description {}",
                                    error,
                                    errorResponse.getErrorDescription(),
                                    ex);
                            return Optional.<AgentBaseError>of(a);
                        })
                .orElseGet(
                        () -> {
                            logger.error(
                                    "Unknown error {} with description {}",
                                    error,
                                    errorResponse.getErrorDescription(),
                                    ex);
                            return Optional.of(new UnknownError(ex));
                        });
    }

    public static <T> Try<Either<ErrorResponse, T>> handleAgentError(
            HttpResponseException exception) {

        return Try.<Either<ErrorResponse, T>>failure(exception)
                .recoverWith(
                        HttpResponseException.class,
                        ex -> {
                            if (ex.getResponse().getStatus() == 500) {
                                Try.failure(BankServiceError.BANK_SIDE_FAILURE.exception(ex));
                            }
                            final ErrorResponse errResponse =
                                    ex.getResponse().getBody(ErrorResponse.class);

                            return mapError(ex, errResponse)
                                    .map(
                                            e ->
                                                    Try.<Either<ErrorResponse, T>>failure(
                                                            e.exception(ex)))
                                    .orElseGet(() -> Try.success(Either.left(errResponse)));
                        });
    }

    public static <T> T getFromStorage(Storage storage, String tag, Class<T> className) {
        return storage.get(tag, className)
                .orElseThrow(
                        () ->
                                new MissingResourceException(
                                        "Missing in storage: ", className.getName(), tag));
    }
}
