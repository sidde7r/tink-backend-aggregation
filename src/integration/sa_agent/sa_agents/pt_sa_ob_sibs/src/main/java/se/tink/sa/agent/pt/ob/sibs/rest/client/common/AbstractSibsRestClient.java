package se.tink.sa.agent.pt.ob.sibs.rest.client.common;

import java.util.HashMap;
import java.util.Map;
import se.tink.sa.agent.pt.ob.sibs.SibsConstants;
import se.tink.sa.framework.rest.client.AbstractBusinessRestClient;

public class AbstractSibsRestClient extends AbstractBusinessRestClient {

    protected Map<String, String> sibsParamsSet(String bankCode) {
        Map<String, String> params = new HashMap<>();
        params.put(SibsConstants.PathParameterKeys.ASPSP_CDE, bankCode);
        return params;
    }
}
