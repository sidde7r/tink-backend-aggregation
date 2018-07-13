package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.creditcard.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.creditcard.entities.CreditCardTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.creditcard.entities.CreditCardTransactionsEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.rpc.AlandsBankenResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditCardTransactionsResponse extends AlandsBankenResponse {

    private CreditCardTransactionsEntity data;

    public List<CreditCardTransactionEntity> getCreditTransactions() {
        return data.getCreditTransactions();
    }
}
