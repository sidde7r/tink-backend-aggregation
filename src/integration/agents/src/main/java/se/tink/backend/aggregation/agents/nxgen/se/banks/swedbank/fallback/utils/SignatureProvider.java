package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fallback.utils;

import java.util.Map;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidasidentity.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;

public interface SignatureProvider {
    String getDigestHeaderValue(HttpRequest request);

    String generateSignatureHeader(
            Map<String, Object> headers,
            EidasProxyConfiguration eidasProxyConf,
            EidasIdentity eidasIdentity,
            String qSealc);
}
