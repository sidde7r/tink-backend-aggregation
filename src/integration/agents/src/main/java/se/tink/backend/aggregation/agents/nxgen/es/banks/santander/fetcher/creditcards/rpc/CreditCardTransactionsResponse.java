package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.creditcards.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.creditcards.entities.CreditCardRepositionEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.creditcards.entities.CreditCardTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.InfoEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

@JsonObject
@XmlRootElement(name = "methodResult")
public class CreditCardTransactionsResponse {
    private InfoEntity info;

    @JsonProperty("limiteCredito")
    private AmountEntity creditLimit;
    @JsonProperty("importeSalDispto")
    private AmountEntity balance;
    @JsonProperty("finLista")
    private String endOfList;
    @JsonProperty("repos")
    private CreditCardRepositionEntity reposition;
    @JsonProperty("lista")
    private List<CreditCardTransactionEntity> transactionList;

    public InfoEntity getInfo() {
        return info;
    }

    public AmountEntity getBalance() {
        return balance;
    }

    public String getEndOfList() {
        return endOfList;
    }

    public List<CreditCardTransactionEntity> getTransactionList() {
        return transactionList;
    }

    public CreditCardRepositionEntity getReposition() {
        return reposition;
    }

    @JsonIgnore
    public Collection<CreditCardTransaction> getTinkTransactions() {
        return Optional.ofNullable(transactionList).orElse(Collections.emptyList()).stream()
                .map(CreditCardTransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public Optional<Boolean> canFetchMore() {
        return Optional.of(SantanderEsConstants.Indicators.NO.equalsIgnoreCase(endOfList));
    }
}
