package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.executor.rpc;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants;

public class ExecuteExternalTransferBody extends MultivaluedMapImpl {

    public ExecuteExternalTransferBody(String otp) {
        add(IngConstants.Session.OTP, otp);
        add(IngConstants.Session.ValuePairs.DSE_TYPE.getKey(),
                IngConstants.Session.ValuePairs.DSE_TYPE.getValue());
    }
}
