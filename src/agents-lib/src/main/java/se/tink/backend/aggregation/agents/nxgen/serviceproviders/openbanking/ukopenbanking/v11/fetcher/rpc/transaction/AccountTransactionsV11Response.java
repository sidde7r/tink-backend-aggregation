package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.fetcher.rpc.transaction;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.rpc.BaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.fetcher.entities.transaction.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.UkOpenBankingV11Constants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class AccountTransactionsV11Response extends BaseResponse<List<TransactionEntity>> {

    private String nextKey() {
        return searchLink(UkOpenBankingV11Constants.Links.NEXT)
                .orElse(null);
    }

    private List<? extends Transaction> toTinkTransactions() {
        return getData().stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    private List<? extends Transaction> toCreditCardTransactions(CreditCardAccount account) {
        return getData().stream()
                .map(e -> e.toCreditCardTransaction(account))
                .collect(Collectors.toList());
    }

    public static TransactionKeyPaginatorResponse<String> toAccountTransactionPaginationResponse(
            AccountTransactionsV11Response response, TransactionalAccount account) {
        return new TransactionKeyPaginatorResponseImpl<>(response.toTinkTransactions(), response.nextKey());
    }

    public static TransactionKeyPaginatorResponse<String> toCreditCardPaginationResponse(
            AccountTransactionsV11Response response, CreditCardAccount account) {
        return new TransactionKeyPaginatorResponseImpl<>(response.toCreditCardTransactions(account),
                response.nextKey());
    }

}
