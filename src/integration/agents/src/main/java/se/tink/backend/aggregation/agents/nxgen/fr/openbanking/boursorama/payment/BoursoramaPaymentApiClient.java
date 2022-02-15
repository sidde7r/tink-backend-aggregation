package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.payment;

import java.net.URISyntaxException;
import java.util.List;
import javax.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentValidationException;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.BoursoramaConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.BoursoramaConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.authenticator.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.payment.dto.BoursoramaGetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.payment.dto.PispTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.FrOpenBankingPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.validator.CreatePaymentRequestValidator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2TokenBase;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@AllArgsConstructor
public class BoursoramaPaymentApiClient implements FrOpenBankingPaymentApiClient {

    private final TinkHttpClient client;
    private final String clientId;
    private final CreatePaymentRequestValidator createPaymentRequestValidator;
    private final SessionStorage sessionStorage;

    @Override
    public void fetchToken() {
        if (!isTokenValid()) {
            getAndSaveToken();
        }
    }

    @Override
    public CreatePaymentResponse createPayment(CreatePaymentRequest request)
            throws PaymentValidationException {
        createPaymentRequestValidator.validate(request);
        return createRequestWithToken(Urls.CREATE_PAYMENT)
                .post(CreatePaymentResponse.class, BoursoramaPaymentDtoConverter.convert(request));
    }

    @Override
    public String findPaymentId(String authorizationUrl) {
        final List<NameValuePair> queryParams;
        try {
            queryParams = new URIBuilder(authorizationUrl).getQueryParams();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Cannot parse URL: " + authorizationUrl, e);
        }

        return queryParams.stream()
                .filter(param -> param.getName().equalsIgnoreCase("params[resourceId]"))
                .map(NameValuePair::getValue)
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Cannot find params[resourceId] in URL: "
                                                + authorizationUrl));
    }

    @Override
    public GetPaymentResponse getPayment(String paymentId) {
        final BoursoramaGetPaymentResponse response =
                createRequestWithToken(Urls.GET_PAYMENT.parameter("paymentId", paymentId))
                        .get(BoursoramaGetPaymentResponse.class);

        return BoursoramaPaymentDtoConverter.convert(response);
    }

    private boolean isTokenValid() {
        return sessionStorage
                .get(BoursoramaConstants.PISP_OAUTH_TOKEN, OAuth2Token.class)
                .map(OAuth2TokenBase::isValid)
                .orElse(false);
    }

    private void getAndSaveToken() {
        final OAuth2Token token = getToken();
        sessionStorage.put(BoursoramaConstants.PISP_OAUTH_TOKEN, token);
    }

    private OAuth2Token getToken() {
        final PispTokenRequest request = new PispTokenRequest(clientId);
        final TokenResponse response =
                client.request(Urls.PISP_TOKEN)
                        .accept(MediaType.APPLICATION_JSON)
                        .type(MediaType.APPLICATION_JSON)
                        .post(TokenResponse.class, request);

        return OAuth2Token.create(
                response.getTokenType(), response.getAccessToken(), null, response.getExpiresIn());
    }

    private RequestBuilder createRequestWithToken(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .addBearerToken(getTokenFromStorage());
    }

    private OAuth2Token getTokenFromStorage() {
        return sessionStorage
                .get(BoursoramaConstants.PISP_OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(() -> new PaymentException("PISP OAuth2 token is missing"));
    }
}
