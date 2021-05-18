package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class PermissionsEntity {
    private boolean canView;
    private boolean canViewTransactions;
    @Getter private boolean canPayFromAccount;
    private boolean canTransferFromAccount;
    private boolean canTransferToAccount;
}
