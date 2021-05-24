package se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.accounts.checkingaccount.rpc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.accounts.checkingaccount.entities.TransactionEntity;

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
