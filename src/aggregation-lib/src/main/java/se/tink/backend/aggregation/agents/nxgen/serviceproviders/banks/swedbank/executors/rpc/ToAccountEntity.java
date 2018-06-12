package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.AbstractAccountEntity;

public class ToAccountEntity extends AbstractAccountEntity {
    private String amount;

    public String getAmount() {
        return amount;
    }
}
