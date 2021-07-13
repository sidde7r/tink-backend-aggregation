package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount.entity.LinksEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class TransactionsBaseResponse {

    @JsonProperty("_links")
    private LinksEntity links;

    private AccountEntity account;
    private List<BalanceEntity> balances;
    private TransactionsEntity transactions;
}
