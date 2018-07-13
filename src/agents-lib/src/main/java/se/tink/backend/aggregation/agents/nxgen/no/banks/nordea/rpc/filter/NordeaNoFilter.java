package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.rpc.filter;

import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.NordeaNoConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.rpc.filter.NordeaV17Filter;

public class NordeaNoFilter extends NordeaV17Filter {
    public NordeaNoFilter() {
        super(NordeaNoConstants.MARKET_CODE);
    }
}
