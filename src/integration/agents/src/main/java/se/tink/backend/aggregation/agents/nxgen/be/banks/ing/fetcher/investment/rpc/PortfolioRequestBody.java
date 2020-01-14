package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.investment.rpc;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants;

public class PortfolioRequestBody extends MultivaluedMapImpl {

    public PortfolioRequestBody(String acc) {
        add(IngConstants.Fetcher.ACC, acc);
        add(
                IngConstants.Session.ValuePairs.DSE_TYPE.getKey(),
                IngConstants.Session.ValuePairs.DSE_TYPE.getValue());
    }
}
