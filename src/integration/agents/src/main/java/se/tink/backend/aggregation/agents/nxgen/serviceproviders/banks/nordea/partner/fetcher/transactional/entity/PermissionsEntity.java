package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.entity;

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

    @JsonProperty("can_deposit_to_account")
    private boolean canDepositToAccount;

    @JsonProperty("can_pay_pgbg_from_account")
    private boolean canPayPgbgFromAccount;

    @JsonProperty("require_4eye_signing")
    private boolean require4EyeSigning;
}
