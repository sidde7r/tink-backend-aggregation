package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.filter;

import static se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.LogTags.INVALID_TOKEN;

import com.google.common.collect.ImmutableList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.fetcher.transactionalaccount.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
@RequiredArgsConstructor
public class BelfiusTokenErrorFilter extends Filter {

    private static final List<String> INVALID_TOKEN_MESSAGES =
            ImmutableList.of("refresh token is not valid", "access_token is not valid");

    private final PersistentStorage persistentStorage;
    private final Date sessionExpiryDate;

    @Override
    public HttpResponse handle(HttpRequest httpRequest) {
        HttpResponse httpResponse = nextFilter(httpRequest);
        invalidateSessionOnInvalidTokenResponse(httpResponse);
        return httpResponse;
    }

    private void invalidateSessionOnInvalidTokenResponse(HttpResponse httpResponse) {
        if (httpResponse.getStatus() == 401
                && httpResponse.hasBody()
                && INVALID_TOKEN_MESSAGES.stream()
                        .anyMatch(message -> isInvalidTokenMessage(httpResponse, message))) {
            log.info(
                    "{} Error response: {}.\nSession expiry date: {}\nIs session expired prematurely: {}",
                    INVALID_TOKEN,
                    httpResponse.getBody(ErrorResponse.class),
                    sessionExpiryDate,
                    SessionExpiryDateComparator.getSessionExpiryInfo(sessionExpiryDate));
            persistentStorage.clear();
            throw SessionError.SESSION_EXPIRED.exception(
                    httpResponse.getBody(ErrorResponse.class).getErrorDescription());
        }
    }

    private boolean isInvalidTokenMessage(HttpResponse httpResponse, String message) {
        return Optional.ofNullable(httpResponse.getBody(ErrorResponse.class))
                .map(ErrorResponse::getErrorDescription)
                .map(String::toLowerCase)
                .filter(description -> description.contains(message))
                .isPresent();
    }
}
