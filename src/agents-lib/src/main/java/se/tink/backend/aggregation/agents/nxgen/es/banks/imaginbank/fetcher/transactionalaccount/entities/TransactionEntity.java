package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.google.common.base.Strings;
import java.util.regex.Matcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.entities.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.entities.DateEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {

    @JsonUnwrapped
    private BalanceEntity amount;

    @JsonProperty("concepto")
    private String description;

    @JsonProperty("remitente")
    private String sender;

    @JsonProperty("fechaValor")
    private DateEntity valueDate;

    @JsonProperty("fechaOperacion")
    private DateEntity transactionDate;

    @JsonIgnore
    private String getTransactionDescription() {
        if (ImaginBankConstants.TransactionDescriptions.TRANSFER.equalsIgnoreCase(description)) {
            Matcher m = ImaginBankConstants.TransactionDescriptions.CLEAN_TRANSFER_MSG.matcher(sender);
            if (m.matches() && m.groupCount() == 2) {
                return String.format("%s %s", description, m.group(2));
            } else if (!Strings.isNullOrEmpty(sender)) {
                return String.format("%s %s", description, sender);
            }
        }

        return description;
    }

    public Transaction toTinkTransaction(){
        return Transaction.builder()
                .setAmount(amount)
                .setDescription(getTransactionDescription())
                .setDate(transactionDate.toTinkDate())
                .build();
    }
}
