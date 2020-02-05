package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request;

import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.AbstractRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.BPostBankAuthContext;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request.dto.RegistrationResponseDTO;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

public class RegistrationChallengeResponseRequest extends AbstractRequest<RegistrationResponseDTO> {

    static final String PATH = "/bpb/services/rest/v2/registerauth";
    private static final String BODY_TEMPLATE =
            "{\"dataMap\": {\"code\": \"%s\"},\"orderReference\": \"%s\",\"deviceRootedHash\": \"%s\",\"user\": {\"loginName\": \"%s\"}}";
    private BPostBankAuthContext authContext;
    private String challengeResponseCode;

    public RegistrationChallengeResponseRequest(
            BPostBankAuthContext authContext, String challengeResponseCode) {
        super(PATH, authContext);
        this.authContext = authContext;
        this.challengeResponseCode = challengeResponseCode;
    }

    @Override
    public RequestBuilder withBody(RequestBuilder requestBuilder) {
        return requestBuilder.body(
                String.format(
                        BODY_TEMPLATE,
                        challengeResponseCode,
                        authContext.getOrderReference(),
                        authContext.getDeviceRootedHash(),
                        authContext.getLogin()));
    }

    @Override
    public RegistrationResponseDTO execute(RequestBuilder requestBuilder) throws RequestException {
        return AuthenticationResponseStateVerifier.checkIsNotError(
                requestBuilder.post(RegistrationResponseDTO.class));
    }
}
