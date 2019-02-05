package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.checkingaccount.rpc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.checkingaccount.entities.TransactionEntity;

import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionResponse {

    private List<TransactionEntity> transactionList;
    private List<String> position;

    public List<TransactionEntity> getTransactionList() {
        return transactionList;
    }

    public List<String> getPosition() {
        return position;
    }
}
