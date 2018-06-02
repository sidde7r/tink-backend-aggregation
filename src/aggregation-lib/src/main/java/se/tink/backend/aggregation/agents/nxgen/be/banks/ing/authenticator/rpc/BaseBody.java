package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants;

public class BaseBody extends MultivaluedMapImpl {

    public BaseBody() {
        add(IngConstants.Session.ValuePairs.DSE_TYPE.getKey(),
                IngConstants.Session.ValuePairs.DSE_TYPE.getValue());
    }
}
