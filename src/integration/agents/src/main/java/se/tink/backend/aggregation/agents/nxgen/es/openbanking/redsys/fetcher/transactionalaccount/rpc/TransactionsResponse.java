package se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.entities.TppMessageEntity;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.fetcher.transactionalaccount.entities.AccountReferenceEntity;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.fetcher.transactionalaccount.entities.AccountReportEntity;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.fetcher.transactionalaccount.entities.BalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class TransactionsResponse {
    @JsonProperty private AccountReferenceEntity account;
    @JsonProperty private AccountReportEntity transactions;
    @JsonProperty private List<BalanceEntity> balances;

    @JsonProperty("_links")
    private Map<String, LinkEntity> links;

    @JsonProperty private String psuMessage;
    @JsonProperty private List<TppMessageEntity> tppMessages;

    @JsonIgnore
    private Amount getLatestBalance() {
        Optional<BalanceEntity> lastBalance =
                balances.stream().max(Comparator.comparing(BalanceEntity::getReferenceDate));
        return lastBalance.map(BalanceEntity::getAmount).orElse(null);
    }

    @JsonIgnore
    public Optional<LinkEntity> getLink(String linkName) {
        return Optional.ofNullable(links.get(linkName));
    }

    public AccountReportEntity getTransactions() {
        return transactions;
    }
}
