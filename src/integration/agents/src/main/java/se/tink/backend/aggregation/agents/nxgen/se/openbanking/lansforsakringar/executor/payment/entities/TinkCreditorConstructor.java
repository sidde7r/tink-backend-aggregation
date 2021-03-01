package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities;

import se.tink.libraries.payment.rpc.Creditor;

public interface TinkCreditorConstructor {

    Creditor toTinkCreditor();
}
