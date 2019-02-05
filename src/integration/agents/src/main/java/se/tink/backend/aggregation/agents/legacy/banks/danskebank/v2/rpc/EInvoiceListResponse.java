package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EInvoiceListResponse extends AbstractResponse {
    @JsonProperty("Transactions")
    private List<EInvoiceListTransactionEntity> transactions;

    public List<EInvoiceListTransactionEntity> getTransactions() {
        if (transactions == null) {
            return Lists.newArrayList();
        }

        return transactions;
    }

    public void setTransactions(
            List<EInvoiceListTransactionEntity> transactions) {
        this.transactions = transactions;
    }
}
