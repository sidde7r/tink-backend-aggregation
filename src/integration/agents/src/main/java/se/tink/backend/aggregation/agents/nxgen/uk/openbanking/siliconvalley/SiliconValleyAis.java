package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.siliconvalley;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingV31Constants.Time.DEFAULT_OFFSET;

import java.time.Period;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.ScaExpirationValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingV31Constants.Limits;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.hybrid.UkObDateCalculator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.hybrid.UkObTransactionPaginationController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.hybrid.UkObTransactionPaginator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.UkOpenBankingV31Ais;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.rpc.transaction.AccountTransactionsV31Response;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.range.DateRangeCalculator;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SiliconValleyAis extends UkOpenBankingV31Ais {

    private final ScaExpirationValidator scaValidator;
    private final TransactionPaginationHelper transactionPaginationHelper;

    public SiliconValleyAis(
            UkOpenBankingAisConfig ukOpenBankingAisConfig,
            PersistentStorage persistentStorage,
            LocalDateTimeSource localDateTimeSource,
            TransactionPaginationHelper transactionPaginationHelper) {
        super(
                ukOpenBankingAisConfig,
                persistentStorage,
                localDateTimeSource,
                transactionPaginationHelper);
        this.scaValidator = new ScaExpirationValidator(persistentStorage, Limits.SCA_IN_MINUTES);
        this.transactionPaginationHelper = transactionPaginationHelper;
    }

    @Override
    public TransactionPaginator<TransactionalAccount> makeAccountTransactionPaginatorController(
            UkOpenBankingApiClient apiClient,
            AgentComponentProvider componentProvider,
            Provider provider) {
        return new UkObTransactionPaginationController<>(
                new UkObTransactionPaginator<>(
                        apiClient,
                        AccountTransactionsV31Response.class,
                        ((response, account) ->
                                AccountTransactionsV31Response
                                        .toAccountTransactionPaginationResponse(response))),
                constructUkObDateCalculator(),
                Period.ofDays(90));
    }

    @Override
    public TransactionPaginator<CreditCardAccount> makeCreditCardTransactionPaginatorController(
            UkOpenBankingApiClient apiClient,
            AgentComponentProvider componentProvider,
            Provider provider) {
        return new UkObTransactionPaginationController<>(
                new UkObTransactionPaginator<>(
                        apiClient,
                        AccountTransactionsV31Response.class,
                        (AccountTransactionsV31Response::toCreditCardPaginationResponse)),
                constructUkObDateCalculator(),
                Period.ofDays(90));
    }

    protected <T extends Account> UkObDateCalculator<T> constructUkObDateCalculator() {
        return new UkObDateCalculator<>(
                scaValidator,
                new DateRangeCalculator<>(
                        localDateTimeSource, DEFAULT_OFFSET, transactionPaginationHelper),
                Period.ofDays(90),
                Period.ofYears(2));
    }
}
