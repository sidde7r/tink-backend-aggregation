package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.KbcConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.AssetJarsDto;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.AssetsDetailGroupDto;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.AssetsDetailResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.AssetsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.InvestmentPlanDetailResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.InvestmentPlanDto;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto.InvestmentPlansOverviewResponse;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.investment.InvestmentBalanceStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.investment.InvestmentBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class KbcInvestmentAccountFetcher implements AccountFetcher<InvestmentAccount> {

    private final KbcApiClient apiClient;
    private final SessionStorage sessionStorage;

    public KbcInvestmentAccountFetcher(
            KbcApiClient apiClient, final SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        final byte[] cipherKey =
                EncodingUtils.decodeBase64String(
                        sessionStorage.get(KbcConstants.Encryption.AES_SESSION_KEY_KEY));

        // Fetch overviews of investment accounts
        InvestmentPlansOverviewResponse investmentPlansOverview =
                apiClient.fetchInvestmentPlanOverview(cipherKey);
        List<InvestmentPlanDto> investmentPlans =
                Optional.ofNullable(investmentPlansOverview)
                        .map(InvestmentPlansOverviewResponse::getInvestmentPlans)
                        .orElse(Collections.emptyList());

        if (investmentPlans.isEmpty()) {
            return Collections.emptyList();
        }

        // Fetch account detail with investment product names for each investment accounts
        Map<IdModule, InvestmentPlanDetailResponse> investmentPlanDetailMap =
                investmentPlans.stream()
                        .map(
                                investmentPlan ->
                                        new AbstractMap.SimpleEntry<>(
                                                toTinkIdModule(investmentPlan),
                                                apiClient.fetchInvestmentPlanDetail(
                                                        new InvestmentPlanDto(
                                                                investmentPlan
                                                                        .getAgreementNumberIban(),
                                                                investmentPlan
                                                                        .getShowPossessionIndicator()),
                                                        cipherKey)))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // Fetch details of all investment products
        AssetsResponse assetsResponse = apiClient.fetchAssets(cipherKey);
        List<AssetsDetailGroupDto> assetsDetailGroups =
                Optional.ofNullable(assetsResponse.getAssetJars()).orElse(Collections.emptyList())
                        .stream()
                        .map(AssetJarsDto::getReference)
                        .map(
                                reference ->
                                        apiClient.fetchAssetsDetail(
                                                new AssetJarsDto(reference), cipherKey))
                        .map(AssetsDetailResponse::getGroups)
                        .filter(Objects::nonNull)
                        .flatMap(List::stream)
                        .collect(Collectors.toList());

        return investmentPlanDetailMap.entrySet().stream()
                .map(e -> toTinkInvestmentAccount(e, assetsDetailGroups))
                .collect(Collectors.toList());
    }

    private InvestmentAccount toTinkInvestmentAccount(
            Map.Entry<IdModule, InvestmentPlanDetailResponse> investmentEntry,
            List<AssetsDetailGroupDto> assetsDetailGroups) {
        InvestmentPlanDetailResponse investmentPlanDetail = investmentEntry.getValue();
        List<String> investmentProductNames = investmentPlanDetail.toInvestments();
        ExactCurrencyAmount cashBalance = investmentPlanDetail.toCashBalance();
        List<PortfolioModule> portfolioModules =
                assetsDetailGroups.stream()
                        .map(
                                group ->
                                        group.toTinkPortfolioModules(
                                                cashBalance.getDoubleValue(),
                                                investmentProductNames))
                        .flatMap(List::stream)
                        .collect(Collectors.toList());

        InvestmentBalanceStep<InvestmentBuildStep> investmentBalanceStepBuilder =
                portfolioModules.isEmpty()
                        ? InvestmentAccount.nxBuilder().withoutPortfolios()
                        : InvestmentAccount.nxBuilder().withPortfolios(portfolioModules);
        return investmentBalanceStepBuilder
                .withCashBalance(cashBalance)
                .withId(investmentEntry.getKey())
                .build();
    }

    private IdModule toTinkIdModule(InvestmentPlanDto investmentPlan) {
        String iban = investmentPlan.toIban();
        String name = investmentPlan.toName();
        return IdModule.builder()
                .withUniqueIdentifier(iban)
                .withAccountNumber(investmentPlan.toNumber())
                .withAccountName(name)
                .addIdentifier(AccountIdentifier.create(AccountIdentifierType.IBAN, iban))
                .setProductName(name)
                .build();
    }
}
