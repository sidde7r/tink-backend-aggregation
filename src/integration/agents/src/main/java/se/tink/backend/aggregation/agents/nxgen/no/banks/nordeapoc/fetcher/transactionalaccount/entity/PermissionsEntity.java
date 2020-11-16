package se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Getter
public class PermissionsEntity {
    private Boolean canDepositToAccount;
    private Boolean canPayFromAccount;
    private Boolean canTransferFromAccount;
    private Boolean canTransferToAccount;
    private Boolean canView;
    private Boolean canViewTransactions;
}
