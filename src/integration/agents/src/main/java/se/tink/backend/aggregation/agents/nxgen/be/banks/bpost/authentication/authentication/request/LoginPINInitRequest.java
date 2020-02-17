package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request;

import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.AbstractRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.BPostBankAuthContext;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request.dto.LoginResponseDTO;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

public class LoginPINInitRequest extends AbstractRequest<LoginResponseDTO> {

    static final String URL_PATH = "/bpb/services/rest/v2/loginPINinit";
    private static final String BODY_TEMPLATE =
            "{\"user\": {\"loginName\": \"%s\"},\"securityContext\": {\"DeviceInstallationID\": \"$DEVICE_INSTALLATION_ID\",\"authenticationType\": \"3\",\"deviceInstallationID\": \"%s\",\"deviceUniqueID\": \"%s\",\"deviceType\": \"1\"},\"language\": \"nl\"}";

    private BPostBankAuthContext authContext;

    public LoginPINInitRequest(BPostBankAuthContext authContext) {
        super(URL_PATH, authContext);
        this.authContext = authContext;
    }

    @Override
    public RequestBuilder withBody(RequestBuilder requestBuilder) {
        return requestBuilder.body(
                String.format(
                        BODY_TEMPLATE,
                        authContext.getLogin(),
                        authContext.getDeviceInstallationId(),
                        authContext.getDeviceUniqueId()));
    }

    @Override
    public LoginResponseDTO execute(RequestBuilder requestBuilder) throws RequestException {
        return AuthenticationResponseStateVerifier.checkIsNotError(
                requestBuilder.post(LoginResponseDTO.class));
    }
}
