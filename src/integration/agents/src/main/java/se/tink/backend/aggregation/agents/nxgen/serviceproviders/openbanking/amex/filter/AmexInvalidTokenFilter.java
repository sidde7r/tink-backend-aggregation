package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.filter;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmericanExpressConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.dto.ErrorResponseDto;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.hmac.HmacMultiTokenStorage;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RequiredArgsConstructor
public class AmexInvalidTokenFilter extends Filter {

    private final HmacMultiTokenStorage hmacMultiTokenStorage;

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse response = nextFilter(httpRequest);

        if (response.getStatus() >= 400 && response.getStatus() < 500) {
            ErrorResponseDto errorResponseDto = response.getBody(ErrorResponseDto.class);
            if (AmericanExpressConstants.ErrorMessages.REVOKED_TOKEN_MAPPER
                    .translate(errorResponseDto.getMessage())
                    .orElseThrow(() -> new IllegalStateException("Could not translate error"))) {
                hmacMultiTokenStorage.clearToken();
                throw SessionError.CONSENT_REVOKED.exception();
            }
        }
        return response;
    }
}
