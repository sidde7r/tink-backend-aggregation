package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transactionalaccount.entity.accounts.AccountId;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transactionalaccount.entity.accounts.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transactionalaccount.entity.accounts.NavigationLinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class AccountEntityResponse {
    private String cashAccountType;
    private AccountId accountId;
    private String resourceId;
    private String product;

    @JsonProperty("_links")
    private NavigationLinksEntity links;

    private String usage;
    private String psuStatus;
    private String name;
    private String bicFi;
    private String currency;
    private String details;
    private String linkedAccount;

    private List<BalanceEntity> balances;
}
