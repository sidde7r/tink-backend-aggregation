package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.control.Try.run;

import com.google.common.collect.ImmutableList;
import io.vavr.control.Try;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants.ErrorMessages;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;

public class SkandiabankenHttpFilter extends Filter {

    private static final ImmutableList<String> BANK_SIDE_FAILURES =
            ImmutableList.of("connection reset", "connect timed out", "read timed out");

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {

        HttpResponse resp =
                Try.of(() -> nextFilter(httpRequest))
                        .onFailure(HttpClientException.class, handleHttpClientException())
                        .get();
        handleBankServiceError(resp);
        return resp;
    }

    private Consumer<HttpClientException> handleHttpClientException() {
        return e ->
                BANK_SIDE_FAILURES.stream()
                        .filter((f -> f.contains(e.getMessage().toLowerCase())))
                        .findAny()
                        .ifPresent(
                                f -> {
                                    throw BankServiceError.BANK_SIDE_FAILURE.exception();
                                });
    }

    private void handleBankServiceError(HttpResponse response) {
        Match(response.getStatus())
                .of(
                        Case(
                                $(HttpStatus.SC_INTERNAL_SERVER_ERROR),
                                run(() -> checkBankServiceError(response))),
                        Case($(), run(() -> {})));
    }

    private void checkBankServiceError(HttpResponse response) {
        String body = response.getBody(String.class);
        if (matchesErrorMessage(ErrorMessages.TECHNICAL_ERROR, body)
                || matchesErrorMessage(ErrorMessages.TECHNICAL_DIFFICULTIES, body)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }
    }

    private boolean matchesErrorMessage(String errorMessage, String source) {
        return Pattern.compile(Pattern.quote(errorMessage), Pattern.CASE_INSENSITIVE)
                .matcher(source)
                .find();
    }
}
