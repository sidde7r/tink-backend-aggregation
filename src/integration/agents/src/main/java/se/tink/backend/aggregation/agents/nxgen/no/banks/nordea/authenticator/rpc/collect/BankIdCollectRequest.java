package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.rpc.collect;

import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoConstants;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;

public class BankIdCollectRequest extends HttpRequestImpl {
    public BankIdCollectRequest(String sessionId) {
        super(
                HttpMethod.POST,
                NordeaNoConstants.Url.BANKID_POLL.get(),
                new BankIdCollectRequestBody(sessionId));
    }
}
