package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.rpc;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants;

public class TransactionsRequestBody extends MultivaluedMapImpl {

    public TransactionsRequestBody(String acc, String startIndex, String endIndex) {
        add(IngConstants.Fetcher.ACC, acc);
        add(IngConstants.Fetcher.START_INDEX, startIndex);
        add(IngConstants.Fetcher.END_INDEX, endIndex);
        add(IngConstants.Session.ValuePairs.DSE_TYPE.getKey(), IngConstants.Session.ValuePairs.DSE_TYPE.getValue());
    }
}
