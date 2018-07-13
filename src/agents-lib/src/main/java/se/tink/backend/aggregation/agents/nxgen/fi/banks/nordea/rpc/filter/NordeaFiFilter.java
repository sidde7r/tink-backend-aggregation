package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.rpc.filter;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.NordeaFiConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.rpc.filter.NordeaV21Filter;

public class NordeaFiFilter extends NordeaV21Filter {
    public NordeaFiFilter() {
        super(NordeaFiConstants.MARKET_CODE);
    }
}
