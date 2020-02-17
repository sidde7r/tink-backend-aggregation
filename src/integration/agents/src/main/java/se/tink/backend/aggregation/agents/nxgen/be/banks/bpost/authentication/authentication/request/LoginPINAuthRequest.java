package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request;

import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.AbstractRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.BPostBankAuthContext;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request.dto.LoginResponseDTO;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

public class LoginPINAuthRequest extends AbstractRequest<LoginResponseDTO> {

    static final String URL_PATH = "/bpb/services/rest/v2/loginPINauth";
    private static final String BODY_TEMPLATE =
            "{\"dataMap\": {\"code\": \"%s\"},\"user\": {\"loginName\": \"%s\"},\"deviceRootedHash\": \"%s\"}";
    private final BPostBankAuthContext authContext;

    public LoginPINAuthRequest(BPostBankAuthContext authContext) {
        super(URL_PATH, authContext);
        this.authContext = authContext;
    }

    @Override
    public RequestBuilder withBody(RequestBuilder requestBuilder) {
        return requestBuilder.body(
                String.format(
                        BODY_TEMPLATE,
                        authContext.getDataMapCode(),
                        authContext.getLogin(),
                        authContext.getDeviceRootedHash()));
    }

    @Override
    public LoginResponseDTO execute(RequestBuilder requestBuilder) throws RequestException {
        return AuthenticationResponseStateVerifier.checkIsNotError(
                requestBuilder.post(LoginResponseDTO.class));
    }
}
