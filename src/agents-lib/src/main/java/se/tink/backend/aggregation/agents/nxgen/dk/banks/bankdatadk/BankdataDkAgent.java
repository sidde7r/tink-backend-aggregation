package se.tink.backend.aggregation.agents.nxgen.dk.banks.bankdatadk;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataAgent;
import se.tink.backend.aggregation.rpc.CredentialsRequest;

public class BankdataDkAgent extends BankdataAgent {

    public BankdataDkAgent(CredentialsRequest request, AgentContext context) {
        super(request, context);
    }
}
