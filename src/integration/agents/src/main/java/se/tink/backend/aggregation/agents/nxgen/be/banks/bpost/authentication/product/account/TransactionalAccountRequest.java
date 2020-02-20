package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.product.account;

import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.AbstractRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.entity.BPostBankAuthContext;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

public class TransactionalAccountRequest extends AbstractRequest<BPostBankAccountsResponseDTO> {

    static final String URL_PATH = "/bpb/services/rest/v2/accounts";
    private static final String BODY_TEMPLATE = "{\"principalIdentification\": \"%s\"}";
    private final String login;

    public TransactionalAccountRequest(BPostBankAuthContext authContext) {
        super(URL_PATH, authContext);
        this.login = authContext.getLogin();
    }

    @Override
    public RequestBuilder withBody(RequestBuilder requestBuilder) {
        return requestBuilder.body(String.format(BODY_TEMPLATE, login));
    }

    @Override
    public BPostBankAccountsResponseDTO execute(RequestBuilder requestBuilder)
            throws RequestException {
        return requestBuilder.post(BPostBankAccountsResponseDTO.class);
    }
}
