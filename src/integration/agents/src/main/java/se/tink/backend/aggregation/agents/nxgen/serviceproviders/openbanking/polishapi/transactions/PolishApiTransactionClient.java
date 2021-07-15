package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.transactions;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Transactions;

import java.time.LocalDate;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.transactions.dto.responses.TransactionsResponse;

public interface PolishApiTransactionClient {
    TransactionsResponse fetchTransactionsByDate(
            String accountNumber,
            LocalDate from,
            LocalDate to,
            Transactions.TransactionTypeRequest transactionType);

    TransactionsResponse fetchTransactionsByNextPage(
            String nextPage,
            String accountNumber,
            LocalDate from,
            LocalDate to,
            Transactions.TransactionTypeRequest transactionType);
}
