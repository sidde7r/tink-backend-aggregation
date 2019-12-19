package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.AbstractAccountEntity;

public class ToAccountEntity extends AbstractAccountEntity {
    private String amount;

    public String getAmount() {
        return amount;
    }
}
