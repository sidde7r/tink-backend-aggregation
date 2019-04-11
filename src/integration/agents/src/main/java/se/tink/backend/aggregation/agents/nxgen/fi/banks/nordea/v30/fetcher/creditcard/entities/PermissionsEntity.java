package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PermissionsEntity {

    @JsonProperty("can_view_transactions")
    private boolean canViewTransactions;

    @JsonProperty("can_transfer_from_credit")
    private boolean canTransferFromCredit;
}
