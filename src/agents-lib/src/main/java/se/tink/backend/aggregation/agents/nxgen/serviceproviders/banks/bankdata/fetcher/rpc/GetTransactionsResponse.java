package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities.BankdataAccountBalance;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities.BankdataTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.parser.BankdataTransactionParser;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

@JsonObject
public class GetTransactionsResponse implements PaginatorResponse {
    private static final BankdataTransactionParser parser = new BankdataTransactionParser();

    private int numOfTransactions;
    private List<BankdataTransactionEntity> lstTransactions;
    private double reservedAmount;
    private boolean moreTransactionsAvailable;
    private BankdataAccountBalance accountBalance;

    @Override
    public Collection<Transaction> getTinkTransactions() {
        if (this.numOfTransactions > 0) {
            return this.lstTransactions.stream()
                    .map(parser::parseTransaction)
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(this.moreTransactionsAvailable);
    }

    public List<UpcomingTransaction> getTinkUpcomingTransactions() {
        if (this.numOfTransactions > 0) {
            return this.lstTransactions.stream()
                    .map(parser::parseUpcomingTransaction)
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
