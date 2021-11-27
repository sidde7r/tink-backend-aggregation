package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.ErrorDetailsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class TransactionEntity extends AbstractExecutorTransactionEntity {
    private String noteToSender;
    private boolean canSign;
    private boolean counterSigning;
    private List<ErrorDetailsEntity> rejectionCauses;
}
