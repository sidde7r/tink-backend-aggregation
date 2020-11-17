package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.rpc.transaction;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.rpc.BaseV31Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.entities.transaction.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class AccountTransactionsV31Response extends BaseV31Response<List<TransactionEntity>> {

    public static TransactionKeyPaginatorResponse<String> toAccountTransactionPaginationResponse(
            AccountTransactionsV31Response response, TransactionalAccount account) {
        return new TransactionKeyPaginatorResponseImpl<>(
                response.toTinkTransactions(), response.nextKey());
    }

    public static TransactionKeyPaginatorResponse<String> toCreditCardPaginationResponse(
            AccountTransactionsV31Response response, CreditCardAccount account) {
        return new TransactionKeyPaginatorResponseImpl<>(
                response.toCreditCardTransactions(account), response.nextKey());
    }

    private String nextKey() {
        return searchLink(UkOpenBankingV31Constants.Links.NEXT).orElse(null);
    }

    private List<? extends Transaction> toTinkTransactions() {
        return getData().orElse(Collections.emptyList()).stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    private List<? extends Transaction> toCreditCardTransactions(CreditCardAccount account) {
        return getData().orElse(Collections.emptyList()).stream()
                .map(e -> e.toCreditCardTransaction(account))
                .collect(Collectors.toList());
    }
}
