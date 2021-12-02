package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.filter;

import static se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.LogTags.REFRESH_TOKEN_ERROR;
import static se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.Urls.TOKEN_PATH;

import java.util.Date;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@Slf4j
@RequiredArgsConstructor
public class BelfiusUnknownRefreshTokenErrorLoggingFilter extends Filter {

    private final Date sessionExpiryDate;

    @Override
    public HttpResponse handle(HttpRequest httpRequest) {
        HttpResponse httpResponse = nextFilter(httpRequest);
        logErrorsOnRefreshTokenEndpoint(httpRequest, httpResponse);
        return httpResponse;
    }

    private void logErrorsOnRefreshTokenEndpoint(
            HttpRequest httpRequest, HttpResponse httpResponse) {
        if (httpResponse.getStatus() >= 400
                && TOKEN_PATH.equals(httpRequest.getUrl().get())
                && hasRequestRefreshTokenBody(httpRequest)) {
            log.warn(
                    "{} Unknown error on refresh token endpoint. Error response: {}.\nSession expiry date: {}\nIs session expired prematurely: {}",
                    REFRESH_TOKEN_ERROR,
                    httpResponse.getBody(String.class),
                    sessionExpiryDate,
                    SessionExpiryDateComparator.getSessionExpiryInfo(sessionExpiryDate));
        }
    }

    private boolean hasRequestRefreshTokenBody(HttpRequest httpRequest) {
        return Optional.ofNullable(httpRequest.getBody())
                .map(Object::toString)
                .filter(body -> body.contains("grant_type=refresh_token"))
                .isPresent();
    }
}
