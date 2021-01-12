package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient;

import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclAgent;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.accesstoken.RefreshTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.accesstoken.RetrieveTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.accesstoken.TokenResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.configuration.LclConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.PispTokenRequest;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

@RequiredArgsConstructor
@Slf4j
public class LclTokenApiClient {

    private static final String TOKEN_PATH = LclAgent.BASE_URL + "/token";

    private final TinkHttpClient httpClient;
    private final LclHeaderValueProvider headerValueProvider;
    private final AgentConfiguration<LclConfiguration> lclConfiguration;

    @SneakyThrows
    public TokenResponseDto retrieveAccessToken(String code) {
        final RetrieveTokenRequest request =
                new RetrieveTokenRequest(
                        LclAgent.CLIENT_ID, lclConfiguration.getRedirectUrl(), code);

        return sendTokenRequestAndGetResponse(request);
    }

    @SneakyThrows
    public Optional<TokenResponseDto> refreshAccessToken(String refreshToken) {
        try {
            final RefreshTokenRequest request =
                    new RefreshTokenRequest(LclAgent.CLIENT_ID, refreshToken);
            final TokenResponseDto response = sendTokenRequestAndGetResponse(request);

            return Optional.ofNullable(response);
        } catch (BankServiceException ex) {
            log.error("Refresh token failed.");
            log.error(ex.getMessage(), ex);
        }

        return Optional.empty();
    }

    @SneakyThrows
    public TokenResponseDto getPispToken() {
        PispTokenRequest request = new PispTokenRequest(LclAgent.CLIENT_ID);

        return httpClient
                .request(TOKEN_PATH)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .post(TokenResponseDto.class, request);
    }

    private RequestBuilder createRequestAndSetHeaders(String url, Object body) {
        final String requestId = UUID.randomUUID().toString();
        final String date = headerValueProvider.getDateHeaderValue();
        final String digest = headerValueProvider.getDigestHeaderValue(body);
        final String signature =
                headerValueProvider.getSignatureHeaderValue(requestId, date, digest);

        return httpClient
                .request(url)
                .header(Psd2Headers.Keys.X_REQUEST_ID, requestId)
                .header(Psd2Headers.Keys.DATE, date)
                .header(Psd2Headers.Keys.DIGEST, digest)
                .header(Psd2Headers.Keys.SIGNATURE, signature);
    }

    private TokenResponseDto sendTokenRequestAndGetResponse(Object body) {
        return createRequestAndSetHeaders(TOKEN_PATH, body)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .post(TokenResponseDto.class, body);
    }
}
