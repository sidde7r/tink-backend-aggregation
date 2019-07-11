package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities.SecuritiesAccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities.insurance.InsuranceEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities.pension.PartsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities.pension.PensionEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.rpc.FetchInvestmentHoldingsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.rpc.FetchInvestmentsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

public class SkandiaBankenInvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private final SkandiaBankenApiClient apiClient;

    public SkandiaBankenInvestmentFetcher(SkandiaBankenApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        List<InvestmentAccount> investmentAccounts = new ArrayList<>();
        FetchInvestmentsResponse investmentsResponse = apiClient.fetchInvestments();

        investmentAccounts.addAll(
                investmentsResponse.getSecuritiesAccounts().stream()
                        .map(this::toTinkInvestmentAccount)
                        .collect(Collectors.toList()));

        investmentAccounts.addAll(
                investmentsResponse.getPensions().stream()
                        .map(this::toTinkInvestmentAccount)
                        .collect(Collectors.toList()));

        // only support insurances with securities account part
        // TODO: support other types when we find them
        investmentAccounts.addAll(
                investmentsResponse.getInsurances().stream()
                        .filter(InsuranceEntity::hasSecuritiesAccountPart)
                        .map(InsuranceEntity::getSecuritiesAccountPart)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(this::toTinkInvestmentAccount)
                        .collect(Collectors.toList()));

        return investmentAccounts;
    }

    private InvestmentAccount toTinkInvestmentAccount(SecuritiesAccountsEntity accountsEntity) {
        final String investmentAccountNumber = accountsEntity.getEncryptedNumber();
        final FetchInvestmentHoldingsResponse holdingsResponse =
                apiClient.fetchHoldings(investmentAccountNumber);
        return apiClient
                .fetchInvestmentAccountDetails(investmentAccountNumber)
                .toTinkInvestmentAccount(accountsEntity, holdingsResponse);
    }

    private InvestmentAccount toTinkInvestmentAccount(PensionEntity pensionEntity) {
        final List<PartsEntity> updatedParts = new ArrayList<>();
        for (PartsEntity part : pensionEntity.getParts()) {
            if (part.isCanSeeHolding()) {
                part.setPensionFunds(
                        apiClient.fetchPensionHoldings(
                                part.getNumber(),
                                part
                                        .getEncryptedNationalIdentificationNumberOfFirstInsuredPerson()));
            }
            updatedParts.add(part);
        }

        pensionEntity.setParts(updatedParts);
        return pensionEntity.toTinkInvestmentAccount();
    }
}
