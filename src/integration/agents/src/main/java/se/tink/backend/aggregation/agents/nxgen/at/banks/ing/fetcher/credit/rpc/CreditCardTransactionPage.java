package se.tink.backend.aggregation.agents.nxgen.at.banks.ing.fetcher.credit.rpc;

import java.util.Collection;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.IngAtConstants;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.utils.IngAtCreditCardTransactionParser;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public final class CreditCardTransactionPage implements PaginatorResponse {

    private final Account account;

    public CreditCardTransactionPage(final Account account) {
        this.account = account;
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        String body = account.getFromTemporaryStorage(IngAtConstants.Storage.TRANSACTIONS.name());
        IngAtCreditCardTransactionParser parser = new IngAtCreditCardTransactionParser(body);
        return parser.getTransactions();
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(false);
    }
}
