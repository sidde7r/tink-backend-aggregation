package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request;

import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.AbstractRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request.dto.RegistrationResponseDTO;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.entity.BPostBankAuthContext;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

public class RegistrationInitRequest extends AbstractRequest<RegistrationResponseDTO> {

    static final String URL_PATH = "/bpb/services/rest/v2/registerinit";
    private static final String BODY =
            "{\"securityContext\": {\"authenticationType\": \"1\",\"deviceType\": \"1\"},\"language\": \"nl\"}";

    public RegistrationInitRequest(BPostBankAuthContext authContext) {
        super(URL_PATH, authContext);
    }

    @Override
    public RequestBuilder withBody(RequestBuilder requestBuilder) {
        return requestBuilder.body(BODY);
    }

    @Override
    public RegistrationResponseDTO execute(RequestBuilder requestBuilder) throws RequestException {
        return AuthenticationResponseStateVerifier.checkIsNotError(
                requestBuilder.post(RegistrationResponseDTO.class));
    }
}
