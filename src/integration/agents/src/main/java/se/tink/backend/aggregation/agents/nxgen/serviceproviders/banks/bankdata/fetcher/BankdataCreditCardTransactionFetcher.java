package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities.BankdataAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc.GetTransactionsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc.GetTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

@RequiredArgsConstructor
public class BankdataCreditCardTransactionFetcher
        implements TransactionPagePaginator<CreditCardAccount> {

    private final BankdataApiClient bankClient;

    @Override
    public GetTransactionsResponse getTransactionsFor(CreditCardAccount account, int page) {

        GetTransactionsRequest getTransactionsRequest =
                new GetTransactionsRequest()
                        .addAccount(
                                account.getFromTemporaryStorage(
                                        BankdataAccountEntity.REGISTRATION_NUMBER_TEMP_STORAGE_KEY),
                                account.getFromTemporaryStorage(
                                        BankdataAccountEntity.ACCOUNT_NUMBER_TEMP_STORAGE_KEY))
                        .setPage(page);

        return bankClient.getTransactions(getTransactionsRequest);
    }
}
