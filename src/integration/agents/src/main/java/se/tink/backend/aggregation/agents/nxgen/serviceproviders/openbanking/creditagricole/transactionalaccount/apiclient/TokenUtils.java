package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient;

import java.util.UUID;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants.ApiServices;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.authenticator.rpc.TokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.configuration.CreditAgricoleBaseConfiguration;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

class TokenUtils {

    static TokenResponse get(
            final CreditAgricoleBaseConfiguration creditAgricoleConfiguration,
            final TinkHttpClient client,
            final String code) {
        final String clientId = creditAgricoleConfiguration.getClientId();
        final String redirectUri = creditAgricoleConfiguration.getRedirectUrl();

        final TokenRequest request =
                new TokenRequest.TokenRequestBuilder()
                        .scope(QueryValues.SCOPE)
                        .grantType(QueryValues.GRANT_TYPE)
                        .code(code)
                        .redirectUri(redirectUri)
                        .clientId(clientId)
                        .build();

        return client.request(getUrl(creditAgricoleConfiguration.getBaseUrl()))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.CORRELATION_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.CATS_CONSOMMATEUR, HeaderValues.CATS_CONSOMMATEUR)
                .header(HeaderKeys.CATS_CONSOMMATEURORIGINE, HeaderValues.CATS_CONSOMMATEURORIGINE)
                .header(HeaderKeys.CATS_CANAL, HeaderValues.CATS_CANAL)
                .post(TokenResponse.class, request.toData());
    }

    static TokenResponse refresh(
            final CreditAgricoleBaseConfiguration creditAgricoleConfiguration,
            final TinkHttpClient client,
            final String refreshToken) {
        final String clientId = creditAgricoleConfiguration.getClientId();
        final String redirectUri = creditAgricoleConfiguration.getRedirectUrl();

        final TokenRequest request =
                new TokenRequest.TokenRequestBuilder()
                        .scope(QueryValues.SCOPE)
                        .grantType(QueryValues.REFRESH_TOKEN)
                        .refreshToken(refreshToken)
                        .redirectUri(redirectUri)
                        .clientId(clientId)
                        .build();

        return client.request(getUrl(creditAgricoleConfiguration.getBaseUrl()))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.CORRELATION_ID, UUID.randomUUID().toString())
                .header(HeaderKeys.CATS_CONSOMMATEUR, HeaderValues.CATS_CONSOMMATEUR)
                .header(HeaderKeys.CATS_CONSOMMATEURORIGINE, HeaderValues.CATS_CONSOMMATEURORIGINE)
                .header(HeaderKeys.CATS_CANAL, HeaderValues.CATS_CANAL)
                .post(TokenResponse.class, request.toData());
    }

    private static String getUrl(final String baseUrl) {
        return baseUrl + ApiServices.TOKEN;
    }
}
