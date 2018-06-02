package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisteredPaymentResponse extends AbstractResponse {
    private String amount;
    private List<TransferTransactionGroupEntity> registeredTransactions;
    private LinksEntity links;

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public List<TransferTransactionGroupEntity> getRegisteredTransactions() {
        return registeredTransactions;
    }

    public void setRegisteredTransactions(
            List<TransferTransactionGroupEntity> registeredTransactions) {
        this.registeredTransactions = registeredTransactions;
    }

    public LinksEntity getLinks() {
        return links;
    }

    public void setLinks(LinksEntity links) {
        this.links = links;
    }
}
