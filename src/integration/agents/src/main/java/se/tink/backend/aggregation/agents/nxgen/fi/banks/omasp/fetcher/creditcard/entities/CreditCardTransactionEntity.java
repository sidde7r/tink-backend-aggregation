package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.creditcard.entities;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.entities.DateEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

@JsonObject
public class CreditCardTransactionEntity {
    private String id;
    private String title;
    private DateEntity date;
    private AmountEntity sum;
    private boolean foreignCurrency;
    // `sumInCurrency` is null - cannot define it!
    private String type;

    public AggregationTransaction toTinkTransaction(CreditCardAccount creditCardAccount) {
        return CreditCardTransaction.builder()
                .setCreditAccount(creditCardAccount)
                .setAmount(sum.toTinkAmount())
                .setDate(date.getValue())
                .setDescription(title)
                .build();
    }
}
