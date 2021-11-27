package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.AbstractAccountEntity;

@Getter
public class ToAccountEntity extends AbstractAccountEntity {
    private String amount;
}
