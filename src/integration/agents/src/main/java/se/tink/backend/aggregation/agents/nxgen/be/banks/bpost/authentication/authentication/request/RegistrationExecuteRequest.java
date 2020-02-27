package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request;

import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.AbstractRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request.dto.RegistrationResponseDTO;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.entity.BPostBankAuthContext;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

public class RegistrationExecuteRequest extends AbstractRequest<RegistrationResponseDTO> {

    static final String URL_PATH = "/bpb/services/rest/v2/register/execute";
    private static final String BODY_TEMPLATE =
            "{\"userID\": \"%s\",\"deviceVersion\": \"12.4\",\"orderReference\": \"%s\",\"mobileRegistrationType\": \"1\",\"deviceUniqueID\": \"%s\",\"bpoEmailAddress\": \"%s\",\"devicePlatform\": \"iOS\",\"deviceType\": \"1\",\"deviceName\": \"iPhone9,3\",\"language\": \"nl\",\"deviceCredential\": \"%s\",\"deviceAlias\": \"iPhone9,3\"}";
    private BPostBankAuthContext authContext;

    public RegistrationExecuteRequest(BPostBankAuthContext authContext) {
        super(URL_PATH, authContext);
        this.authContext = authContext;
    }

    @Override
    public RequestBuilder withBody(RequestBuilder requestBuilder) {
        return requestBuilder.body(
                String.format(
                        BODY_TEMPLATE,
                        authContext.getLogin(),
                        authContext.getOrderReference(),
                        authContext.getDeviceUniqueId(),
                        authContext.getEmail(),
                        authContext.getDeviceCredential()));
    }

    @Override
    public RegistrationResponseDTO execute(RequestBuilder requestBuilder) throws RequestException {
        return AuthenticationResponseStateVerifier.checkIsNotError(
                requestBuilder.post(RegistrationResponseDTO.class));
    }
}
