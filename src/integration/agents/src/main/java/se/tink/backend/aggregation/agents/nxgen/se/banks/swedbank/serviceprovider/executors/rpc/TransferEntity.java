package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class TransferEntity {
    private ToAccountEntity toAccount;
    private String periodicity;
    private String dateDependency;
    private String noteToRecipient;
    private boolean changedButNotConfirm;
}
