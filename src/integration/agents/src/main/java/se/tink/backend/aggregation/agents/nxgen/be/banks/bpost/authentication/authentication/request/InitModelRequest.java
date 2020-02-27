package se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.authentication.authentication.request;

import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bpost.AbstractRequest;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

public class InitModelRequest extends AbstractRequest<Void> {

    private static final String URL_PATH = "bpb/portals/bpost-mobile/mobile/model";

    public InitModelRequest() {
        super(URL_PATH);
    }

    @Override
    public RequestBuilder withBody(RequestBuilder requestBuilder) {
        return requestBuilder;
    }

    @Override
    public Void execute(RequestBuilder requestBuilder) throws RequestException {
        requestBuilder.get(String.class);
        return null;
    }
}
