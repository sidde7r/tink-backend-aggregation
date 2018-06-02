package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferGroupEntity {
    private List<TransactionAccountEntity> externalRecipients;

    public List<TransactionAccountEntity> getExternalRecipients() {
        return externalRecipients;
    }

    public void setExternalRecipients(List<TransactionAccountEntity> externalRecipients) {
        this.externalRecipients = externalRecipients;
    }
}
