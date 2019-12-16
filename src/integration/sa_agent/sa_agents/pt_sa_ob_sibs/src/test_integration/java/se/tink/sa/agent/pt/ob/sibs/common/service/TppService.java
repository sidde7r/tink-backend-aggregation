package se.tink.sa.agent.pt.ob.sibs.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.tink.sa.agent.pt.ob.sibs.common.model.AuthResp;
import se.tink.sa.agent.pt.ob.sibs.common.model.TppOsContainer;
import se.tink.sa.agent.pt.ob.sibs.common.model.TppRequestModel;
import se.tink.sa.agent.pt.ob.sibs.common.rest.OpenTestServiceRestClient;
import se.tink.sa.agent.pt.ob.sibs.common.rest.TppTestServiceRestClient;

@Component
public class TppService {

    @Autowired private OpenTestServiceRestClient openTestServiceRestClient;

    @Autowired private TppTestServiceRestClient tppTestServiceRestClient;

    public AuthResp authUser(String link, String state) {
        openTestServiceRestClient.openTpp(formTinkHttpRequest(link));
        AuthResp authResponse = tppTestServiceRestClient.getAuthResponse(state);
        return authResponse;
    }

    private TppRequestModel formTinkHttpRequest(String link) {
        TppRequestModel req = new TppRequestModel();
        TppOsContainer container = new TppOsContainer();
        container.setIntent(link);
        container.setDeepLinkUrl(container.getIntent());
        req.setAndroid(container);
        req.setIos(container);
        return req;
    }
}
