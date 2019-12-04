package se.tink.sa.agent.pt.ob.sibs.facede;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.tink.sa.agent.pt.ob.sibs.SibsConstants;
import se.tink.sa.agent.pt.ob.sibs.SibsUtils;
import se.tink.sa.agent.pt.ob.sibs.rest.client.SibsConsentRestClient;
import se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.entity.ConsentAccessEntity;
import se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.rpc.ConsentRequest;
import se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.rpc.ConsentResponse;
import se.tink.sa.framework.facade.AuthenticationFacade;
import se.tink.sa.model.auth.AuthenticationRequest;
import se.tink.sa.model.auth.AuthenticationResponse;
import se.tink.sa.model.auth.ThirdPartyAppAuthenticationPayload;

@Component
public class SibsAuthenticationFacade implements AuthenticationFacade {

    private static final String BANK_CODE = "BANK_CODE";
    private static final String STATE = "STATE";

    @Autowired private SibsConsentRestClient sibsConsentRestClient;

    @Override
    public AuthenticationResponse getConsent(AuthenticationRequest request) {
        String bankCode = request.getCallbackDataOrDefault(BANK_CODE, null);
        String state = request.getCallbackDataOrDefault(STATE, null);
        ConsentResponse consentResponse =
                sibsConsentRestClient.getConsent(getConsentRequest(), bankCode, state);
        return mapResponse(request, consentResponse);
    }

    private AuthenticationResponse mapResponse(
            AuthenticationRequest request, ConsentResponse consentResponse) {
        AuthenticationResponse.Builder responseBuilder = AuthenticationResponse.newBuilder();
        responseBuilder.setCorrelationId(request.getCorrelationId());

        ThirdPartyAppAuthenticationPayload.Ios.Builder ios =
                ThirdPartyAppAuthenticationPayload.Ios.newBuilder();
        ios.setDeepLinkUrl(consentResponse.getLinks().getRedirect());

        ThirdPartyAppAuthenticationPayload.Builder tppPl =
                ThirdPartyAppAuthenticationPayload.newBuilder();
        tppPl.setIos(ios.build());

        responseBuilder.setPayload(tppPl.build());

        return responseBuilder.build();
    }

    private ConsentRequest getConsentRequest() {
        String valid90Days = SibsUtils.get90DaysValidConsentStringDate();
        return new ConsentRequest(
                new ConsentAccessEntity(SibsConstants.FormValues.ALL_ACCOUNTS),
                true,
                valid90Days,
                SibsConstants.FormValues.FREQUENCY_PER_DAY,
                false);
    }
}
