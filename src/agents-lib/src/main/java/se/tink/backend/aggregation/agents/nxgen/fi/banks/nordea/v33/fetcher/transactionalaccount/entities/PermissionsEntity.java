package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PermissionsEntity {
    @JsonProperty("can_view")
    private boolean canView;
    @JsonProperty("can_view_transactions")
    private boolean canViewTransactions;
    @JsonProperty("can_pay_from_account")
    private boolean canPayFromAccount;
    @JsonProperty("can_transfer_from_account")
    private boolean canTransferFromAccount;
    @JsonProperty("can_transfer_to_account")
    private boolean canTransferToAccount;
    @JsonProperty("can_pay_pgbg_from_account")
    private boolean canPayPgbgFromAccount;

    public boolean isCanView() {
        return canView;
    }

    public boolean isCanViewTransactions() {
        return canViewTransactions;
    }

    public boolean isCanPayFromAccount() {
        return canPayFromAccount;
    }

    public boolean isCanTransferFromAccount() {
        return canTransferFromAccount;
    }

    public boolean isCanTransferToAccount() {
        return canTransferToAccount;
    }

    public boolean isCanPayPgbgFromAccount() {
        return canPayPgbgFromAccount;
    }
}
