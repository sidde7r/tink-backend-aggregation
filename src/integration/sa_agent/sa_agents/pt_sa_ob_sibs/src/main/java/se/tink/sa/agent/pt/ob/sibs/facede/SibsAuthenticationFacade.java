package se.tink.sa.agent.pt.ob.sibs.facede;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.tink.sa.agent.pt.ob.sibs.SibsConstants;
import se.tink.sa.agent.pt.ob.sibs.SibsMappingContextKeys;
import se.tink.sa.agent.pt.ob.sibs.SibsUtils;
import se.tink.sa.agent.pt.ob.sibs.mapper.authentication.rpc.ConsentResponseMapper;
import se.tink.sa.agent.pt.ob.sibs.mapper.authentication.rpc.ConsentStatusResponseMapper;
import se.tink.sa.agent.pt.ob.sibs.rest.client.SibsConsentRestClient;
import se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.entity.ConsentAccessEntity;
import se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.rpc.ConsentRequest;
import se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.rpc.ConsentResponse;
import se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.rpc.ConsentStatusRequest;
import se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.rpc.ConsentStatusResponse;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.framework.facade.AuthenticationFacade;
import se.tink.sa.model.auth.AuthenticationRequest;
import se.tink.sa.model.auth.AuthenticationResponse;
import se.tink.sa.model.auth.GetConsentStatusRequest;
import se.tink.sa.model.auth.GetConsentStatusResponse;

@Component
public class SibsAuthenticationFacade implements AuthenticationFacade {

    private static final String BANK_CODE = "BANK_CODE";

    @Autowired private SibsConsentRestClient sibsConsentRestClient;

    @Autowired private ConsentResponseMapper consentResponseMapper;

    @Autowired private ConsentStatusResponseMapper consentStatusResponseMapper;

    @Override
    public AuthenticationResponse getConsent(AuthenticationRequest request) {
        String bankCode = request.getRequestCommon().getExternalParametersOrThrow(BANK_CODE);
        String state = request.getRequestCommon().getSecurityInfo().getState();
        ConsentResponse consentResponse =
                sibsConsentRestClient.getConsent(getConsentRequest(), bankCode, state);

        MappingContext mappingContext =
                MappingContext.newInstance()
                        .put(SibsMappingContextKeys.REQUEST_COMMON, request.getRequestCommon());
        return consentResponseMapper.map(consentResponse, mappingContext);
    }

    @Override
    public GetConsentStatusResponse getConsentStatus(GetConsentStatusRequest request) {
        ConsentStatusRequest consentStatusRequest = getConsentStatusRequest(request);
        String bankCode = request.getRequestCommon().getExternalParametersOrThrow(BANK_CODE);

        ConsentStatusResponse consentStatusResponse =
                sibsConsentRestClient.checkConsentStatus(consentStatusRequest, bankCode);

        MappingContext mappingContext =
                MappingContext.newInstance()
                        .put(SibsMappingContextKeys.REQUEST_COMMON, request.getRequestCommon());
        return consentStatusResponseMapper.map(consentStatusResponse, mappingContext);
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

    private ConsentStatusRequest getConsentStatusRequest(GetConsentStatusRequest request) {
        ConsentStatusRequest consentStatusRequest = new ConsentStatusRequest();
        consentStatusRequest.setConsentId(
                request.getRequestCommon().getSecurityInfo().getConsentId());
        return consentStatusRequest;
    }
}
