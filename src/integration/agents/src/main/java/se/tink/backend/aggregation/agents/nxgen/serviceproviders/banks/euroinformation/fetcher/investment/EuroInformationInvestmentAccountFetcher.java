package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.investment;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.assertj.core.util.Lists;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.entities.SecurityAccountListEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.investment.rpc.InvestmentAccountOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.investment.rpc.InvestmentAccountsListResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.utils.EuroInformationUtils;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class EuroInformationInvestmentAccountFetcher implements AccountFetcher<InvestmentAccount> {
    private final EuroInformationApiClient apiClient;
    private final SessionStorage sessionStorage;

    private EuroInformationInvestmentAccountFetcher(
            EuroInformationApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    public static EuroInformationInvestmentAccountFetcher create(
            EuroInformationApiClient apiClient, SessionStorage sessionStorage) {
        return new EuroInformationInvestmentAccountFetcher(apiClient, sessionStorage);
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        InvestmentAccountsListResponse investmentAccountsListResponse =
                this.sessionStorage
                        .get(
                                EuroInformationConstants.Tags.INVESTMENT_ACCOUNTS,
                                InvestmentAccountsListResponse.class)
                        .orElseGet(() -> apiClient.requestInvestmentAccounts());

        String returnCode = investmentAccountsListResponse.getReturnCode();
        if (!EuroInformationUtils.isSuccess(returnCode)) {
            return Collections.emptyList();
        }

        List<SecurityAccountListEntity> accountListEntities =
                Optional.ofNullable(investmentAccountsListResponse.getSecurityAccountList())
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                SerializationUtils.serializeToString(
                                                        investmentAccountsListResponse)));

        Collection<InvestmentAccount> accountsDetails = Lists.newArrayList();
        accountListEntities.stream()
                .forEach(
                        a -> {
                            // TODO: Temporary get only one page
                            int page = 1;
                            InvestmentAccountOverviewResponse investmentAccount =
                                    apiClient.requestAccountDetails(a.getNumber(), page);
                            ExactCurrencyAmount amount =
                                    EuroInformationUtils.parseAmount(
                                            investmentAccount
                                                    .getSecurityAccountOverview()
                                                    .getOverview()
                                                    .getAmount());
                            String accountNumber =
                                    investmentAccount
                                            .getSecurityAccountOverview()
                                            .getOverview()
                                            .getNumber();
                            accountsDetails.add(
                                    InvestmentAccount.builder(accountNumber)
                                            .setExactBalance(amount)
                                            .setAccountNumber(accountNumber)
                                            .build());
                        });
        return accountsDetails;
    }
}
