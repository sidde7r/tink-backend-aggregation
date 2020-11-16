package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.jwt;

import com.nimbusds.jose.jwk.JWKSet;
import java.text.ParseException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class JwksClient {

    private final TinkHttpClient httpClient;
    private JWKSet cachedJWKS;

    public synchronized JWKSet get(URL endpoint) throws ParseException {
        if (Objects.isNull(this.cachedJWKS)) {
            this.cachedJWKS = JWKSet.parse(this.httpClient.request(endpoint).get(String.class));
        }

        return this.cachedJWKS;
    }
}
