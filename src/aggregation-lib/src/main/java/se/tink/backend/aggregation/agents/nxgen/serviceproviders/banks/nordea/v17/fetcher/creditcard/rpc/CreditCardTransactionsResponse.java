package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.creditcard.entities.CreditCardTransactionEntities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.creditcard.entities.CreditCardTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.creditcard.entities.InvoicePeriod;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.rpc.NordeaResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditCardTransactionsResponse extends NordeaResponse {
    @JsonProperty("getCreditCardTransactionsOut")
    private CreditCardTransactionEntities transactionsEntity;

    public CreditCardTransactionEntities getTransactionsEntity() {
        return transactionsEntity != null ? transactionsEntity : new CreditCardTransactionEntities();
    }

    public List<CreditCardTransactionEntity> getTransactions() {
        return getTransactionsEntity().getTransactions();
    }

    public String getContinueKey() {
        return getTransactionsEntity().getContinueKey();
    }

    public List<InvoicePeriod> getInvoicePeriods() {
        return getTransactionsEntity().getInvoicePeriods();
    }

    @JsonIgnore
    public Optional<String> getNextInvoicePeriod() {
        return getInvoicePeriods().stream().findFirst().map(InvoicePeriod::getInvoicePeriod);
    }
}
