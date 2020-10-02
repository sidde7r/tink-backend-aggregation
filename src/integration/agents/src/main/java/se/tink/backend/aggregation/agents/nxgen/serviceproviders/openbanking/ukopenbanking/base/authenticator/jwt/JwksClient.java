package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.jwt;

import com.nimbusds.jose.jwk.JWKSet;
import java.text.ParseException;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class JwksClient {

    private final TinkHttpClient httpClient;
    private JWKSet cachedJWKS;

    public JwksClient(TinkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public synchronized JWKSet get(URL endpoint) throws ParseException {
        if (cachedJWKS != null) {
            return cachedJWKS;
        }
        JWKSet parse = JWKSet.parse(httpClient.request(endpoint).get(String.class));
        this.cachedJWKS = parse;
        return parse;
    }
}
