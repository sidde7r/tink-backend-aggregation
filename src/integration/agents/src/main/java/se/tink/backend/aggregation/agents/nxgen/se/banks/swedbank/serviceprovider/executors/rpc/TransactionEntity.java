package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.ErrorDetailsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionEntity extends AbstractExecutorTransactionEntity {
    private String noteToSender;
    private boolean canSign;
    private boolean counterSigning;
    private List<ErrorDetailsEntity> rejectionCauses;

    public String getNoteToSender() {
        return noteToSender;
    }

    public boolean isCanSign() {
        return canSign;
    }

    public boolean isCounterSigning() {
        return counterSigning;
    }

    public List<ErrorDetailsEntity> getRejectionCauses() {
        return rejectionCauses;
    }
}
