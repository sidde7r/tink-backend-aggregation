package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank;

import static io.vavr.Predicates.anyOf;
import static io.vavr.Predicates.instanceOf;

import java.util.function.Predicate;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.authenticator.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public final class OpenbankPredicates {
    /** Checks if exception has the prerequisites for being an Openbank ErrorResponse */
    public static final Predicate<HttpResponseException> IS_OPENBANK_ERROR_RESPONSE =
            anyOf(instanceOf(HttpResponseException.class), OpenbankPredicates.IS_BAD_REQUEST);

    /** Checks if exception is a HttpResponseException and if the HttpStatus is a Bad Request */
    public static final Predicate<HttpResponseException> IS_BAD_REQUEST =
            hre -> hre.getResponse().getStatus() == HttpStatus.SC_BAD_REQUEST;

    /** Checks if ErrorResponse has the INCORRECT_CREDENTIALS error code */
    public static final Predicate<ErrorResponse> HAS_INCORRECT_CREDENTIALS =
            errorResponse ->
                    errorResponse.hasErrorCode(OpenbankConstants.ErrorCodes.INCORRECT_CREDENTIALS);

    /** Checks if ErrorResponse has the INVALID_LOGIN_USERNAME_TYPE error code */
    public static final Predicate<ErrorResponse> HAS_INVALID_LOGIN_USERNAME_TYPE =
            errorResponse ->
                    errorResponse.hasErrorCode(
                            OpenbankConstants.ErrorCodes.INVALID_LOGIN_USERNAME_TYPE);
}
