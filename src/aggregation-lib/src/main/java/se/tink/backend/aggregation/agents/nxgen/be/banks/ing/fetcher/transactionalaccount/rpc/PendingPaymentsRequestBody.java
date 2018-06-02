package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.rpc;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants;

public class PendingPaymentsRequestBody extends MultivaluedMapImpl {

    public PendingPaymentsRequestBody(String acc) {
        add(IngConstants.Fetcher.ACC, acc);
        add(IngConstants.Session.ValuePairs.DSE_TYPE.getKey(), IngConstants.Session.ValuePairs.DSE_TYPE.getValue());
    }
}
