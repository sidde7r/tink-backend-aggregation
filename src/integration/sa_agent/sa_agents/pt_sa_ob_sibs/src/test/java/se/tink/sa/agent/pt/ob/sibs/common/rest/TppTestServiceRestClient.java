package se.tink.sa.agent.pt.ob.sibs.common.rest;

import java.util.HashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.tink.sa.agent.pt.ob.sibs.common.model.AuthResp;

@Component
public class TppTestServiceRestClient extends TestServerClient {

    private static final String UNIQUE_PREFIX_TPCB = "tpcb_";
    private static final String PARAM_KEY = "key";
    private static final String PARAM_TIMEOUT = "timeout";

    @Value("${tpp.path.supplemental}")
    private String supplementalPath;

    public AuthResp getAuthResponse(String state) {
        HashMap<String, String> params = new HashMap<>();
        params.put(PARAM_KEY, UNIQUE_PREFIX_TPCB + state);
        params.put(PARAM_TIMEOUT, "180");

        AuthResp authResp =
                restTemplate.getForObject(prepareUrl(supplementalPath), AuthResp.class, params);
        return authResp;
    }
}
