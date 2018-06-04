package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransferEntity {
    private ToAccountEntity toAccount;
    private String periodicity;
    private String dateDependency;
    private String noteToRecipient;
    private boolean changedButNotConfirm;

    public ToAccountEntity getToAccount() {
        return toAccount;
    }

    public String getPeriodicity() {
        return periodicity;
    }

    public String getDateDependency() {
        return dateDependency;
    }

    public String getNoteToRecipient() {
        return noteToRecipient;
    }

    public boolean isChangedButNotConfirm() {
        return changedButNotConfirm;
    }
}
