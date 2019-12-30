package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client;

import javax.ws.rs.core.HttpHeaders;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;

public class BoursoramaAuthenticationFilter extends Filter {

    private OAuth2Token oAuth2Token;

    public void setTokenToUse(OAuth2Token oAuth2Token) {
        this.oAuth2Token = oAuth2Token;
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {

        httpRequest.getHeaders().add(HttpHeaders.AUTHORIZATION, oAuth2Token.toAuthorizeHeader());
        return nextFilter(httpRequest);
    }
}
