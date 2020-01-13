package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client;

import javax.ws.rs.core.HttpHeaders;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class BoursoramaAuthenticationFilter extends Filter {

    private OAuth2Token oAuth2Token;

    public void setTokenToUse(OAuth2Token oAuth2Token) {
        this.oAuth2Token = oAuth2Token;
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest) {
        httpRequest.getHeaders().add(HttpHeaders.AUTHORIZATION, oAuth2Token.toAuthorizeHeader());
        return nextFilter(httpRequest);
    }
}
