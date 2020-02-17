package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request;

import java.util.Optional;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.AbstractRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.BPostBankConstants;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class BeginSessionRequest extends AbstractRequest<String> {

    static final String URL_PATH = "/bpb/services/rest/v2/session";

    public BeginSessionRequest() {
        super(URL_PATH);
    }

    @Override
    public RequestBuilder withBody(RequestBuilder requestBuilder) {
        return requestBuilder;
    }

    @Override
    public String execute(RequestBuilder requestBuilder) throws RequestException {
        HttpResponse response = requestBuilder.get(HttpResponse.class);
        return Optional.ofNullable(
                        response.getHeaders().get(BPostBankConstants.CSRF_TOKEN_HEADER_KEY))
                .map(h -> h.get(0))
                .orElseThrow(
                        () ->
                                new RequestException(
                                        BPostBankConstants.CSRF_TOKEN_HEADER_KEY
                                                + "header not found"));
    }
}
