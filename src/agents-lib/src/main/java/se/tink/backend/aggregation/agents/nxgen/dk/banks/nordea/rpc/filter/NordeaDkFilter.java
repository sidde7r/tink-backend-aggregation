package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.rpc.filter;

import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.rpc.filter.NordeaV20Filter;

public class NordeaDkFilter extends NordeaV20Filter {
    public NordeaDkFilter() {
        super(NordeaDkConstants.MARKET_CODE);
    }
}
