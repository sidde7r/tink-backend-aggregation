package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionEntity extends AbstractExecutorTransactionEntity {
    private String noteToSender;
    private boolean canSign;
    private boolean counterSigning;

    public String getNoteToSender() {
        return noteToSender;
    }

    public boolean isCanSign() {
        return canSign;
    }

    public boolean isCounterSigning() {
        return counterSigning;
    }
}
