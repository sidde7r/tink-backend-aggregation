package se.tink.sa.agent.pt.ob.sibs.facede;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.tink.sa.agent.pt.ob.sibs.SibsConstants;
import se.tink.sa.agent.pt.ob.sibs.SibsMappingContextKeys;
import se.tink.sa.agent.pt.ob.sibs.SibsUtils;
import se.tink.sa.agent.pt.ob.sibs.mapper.authentication.rpc.ConsentResponseMapper;
import se.tink.sa.agent.pt.ob.sibs.rest.client.SibsConsentRestClient;
import se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.entity.ConsentAccessEntity;
import se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.rpc.ConsentRequest;
import se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.rpc.ConsentResponse;
import se.tink.sa.framework.facade.AuthenticationFacade;
import se.tink.sa.framework.mapper.MappingContext;
import se.tink.sa.model.auth.AuthenticationRequest;
import se.tink.sa.model.auth.AuthenticationResponse;

@Component
public class SibsAuthenticationFacade implements AuthenticationFacade {

    private static final String BANK_CODE = "BANK_CODE";
    private static final String STATE = "STATE";

    @Autowired private SibsConsentRestClient sibsConsentRestClient;

    @Autowired private ConsentResponseMapper consentResponseMapper;

    @Override
    public AuthenticationResponse getConsent(AuthenticationRequest request) {
        String bankCode = request.getCallbackDataOrDefault(BANK_CODE, null);
        String state = request.getCallbackDataOrDefault(STATE, null);
        ConsentResponse consentResponse =
                sibsConsentRestClient.getConsent(getConsentRequest(), bankCode, state);

        MappingContext mappingContext =
                MappingContext.newInstance()
                        .put(SibsMappingContextKeys.REQUEST_COMMON, request.getRequestCommon());
        return consentResponseMapper.mapToTransferModel(consentResponse, mappingContext);
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
