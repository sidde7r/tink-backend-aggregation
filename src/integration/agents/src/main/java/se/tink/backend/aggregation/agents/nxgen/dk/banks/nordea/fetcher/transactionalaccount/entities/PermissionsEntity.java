package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class PermissionsEntity {
    private Boolean canDepositToAccount;
    private Boolean canPayFromAccount;
    private Boolean canTransferFromAccount;
    private Boolean canTransferToAccount;
    private Boolean canView;
    private Boolean canViewTransactions;

    public Boolean getCanDepositToAccount() {
        return canDepositToAccount;
    }

    public Boolean getCanPayFromAccount() {
        return canPayFromAccount;
    }

    public Boolean getCanTransferFromAccount() {
        return canTransferFromAccount;
    }

    public Boolean getCanTransferToAccount() {
        return canTransferToAccount;
    }

    public Boolean getCanView() {
        return canView;
    }

    public Boolean getCanViewTransactions() {
        return canViewTransactions;
    }
}
