package se.tink.backend.aggregation.agents.nxgen.se.openbanking.corporate.seb.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.corporate.seb.SebConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.entities.TransactionsEntity;
import se.tink.backend.aggregation.nxgen.storage.Storage;

public class SebStorage extends Storage {

    public SebStorage() {
        super();
    }

    public void saveCreditCardTransactionResponse(TransactionsEntity transaction) {
        List<TransactionsEntity> transactions = this.getAllCreditCardTransactionEntities();
        transactions.add(transaction);
        saveCreditCardTransactionResponse(transactions);
    }

    public List<TransactionsEntity> getAllCreditCardTransactionEntities() {
        return this.get(
                        SebConstants.Storage.CREDIT_CARD_TRANSACTION_RESPONSE,
                        new TypeReference<List<TransactionsEntity>>() {})
                .orElse(new ArrayList<>());
    }

    private void saveCreditCardTransactionResponse(
            List<TransactionsEntity> creditCardTransactionEntity) {
        this.put(
                SebConstants.Storage.CREDIT_CARD_TRANSACTION_RESPONSE, creditCardTransactionEntity);
    }
}
