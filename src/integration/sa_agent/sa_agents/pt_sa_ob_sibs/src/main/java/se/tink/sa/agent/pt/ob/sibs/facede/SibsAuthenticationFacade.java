package se.tink.sa.agent.pt.ob.sibs.facede;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.tink.sa.agent.pt.ob.sibs.rest.client.SibsConsentRestClient;
import se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.rpc.ConsentRequest;
import se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.rpc.ConsentResponse;
import se.tink.sa.framework.facade.AuthenticationFacade;
import se.tink.sa.model.auth.AuthenticationRequest;
import se.tink.sa.model.auth.AuthenticationResponse;

@Component
public class SibsAuthenticationFacade implements AuthenticationFacade {

    @Autowired private SibsConsentRestClient sibsConsentRestClient;

    @Override
    public AuthenticationResponse getConsent(AuthenticationRequest request) {
        return mapResponse(
                sibsConsentRestClient.getConsent(
                        mapConsentRequest(request), mapBankCode(request), mapState(request)));
    }

    private ConsentRequest mapConsentRequest(AuthenticationRequest request) {
        // TODO: implement
        return null;
    }

    private String mapBankCode(AuthenticationRequest request) {
        // TODO: implement
        return null;
    }

    private String mapState(AuthenticationRequest request) {
        // TODO: implement
        return null;
    }

    private AuthenticationResponse mapResponse(ConsentResponse consentResponse) {
        // TODO: implement
        return null;
    }
}
