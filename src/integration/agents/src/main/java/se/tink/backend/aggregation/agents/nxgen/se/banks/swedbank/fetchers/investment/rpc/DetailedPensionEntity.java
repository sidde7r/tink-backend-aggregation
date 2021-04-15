package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.contexts.SystemUpdater;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.backend.aggregation.source_info.AccountSourceInfo;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.strings.StringUtils;

@Getter
@Slf4j
@JsonObject
public class DetailedPensionEntity {

    @JsonIgnore private static final String API_CLIENT_ERROR_MESSAGE = "No API client provided.";

    private String fullyFormattedNumber;
    private List<Object> settlements;
    private AmountEntity totalValue;
    private boolean isTrad;
    private String productId;
    private TotalEquitiesEntity totalEquities;
    private AmountEntity marketValue;
    private boolean holdingsFetched;
    private List<PlacementEntity> placements;
    private AmountEntity acquisitionValue;
    private String accountNumber;
    private String type;
    private String clearingNumber;
    private String encompassedHoldings;
    private ChartDataEntity chartData;
    private List<OperationEntity> operations;
    private PerformanceEntity performance;
    private String portfolio;
    private String name;
    private List<DetailedHoldingEntity> holdings;
    private String id;

    public InvestmentAccount toTinkInvestmentAccount(
            SwedbankSEApiClient apiClient,
            List<Account> requestedAccounts,
            SystemUpdater systemUpdater) {
        final String uniqueIdentifier =
                migrateAndGetUniqueIdentifier(requestedAccounts, systemUpdater);
        return InvestmentAccount.nxBuilder()
                .withPortfolios(toTinkPortfolioModule(apiClient))
                .withCashBalance(ExactCurrencyAmount.inSEK(0.0))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(uniqueIdentifier)
                                .withAccountNumber(fullyFormattedNumber)
                                .withAccountName(name)
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.SE, fullyFormattedNumber))
                                .build())
                .sourceInfo(createAccountSourceInfo())
                .build();
    }

    private AccountSourceInfo createAccountSourceInfo() {
        return AccountSourceInfo.builder()
                .bankProductName(name)
                .bankProductCode(productId)
                .bankAccountType(type)
                .build();
    }

    private String migrateAndGetUniqueIdentifier(
            List<Account> accounts, SystemUpdater systemUpdater) {
        final String oldUniqueIdentifier = StringUtils.removeNonAlphaNumeric(accountNumber);
        final String newUniqueIdentifier = StringUtils.removeNonAlphaNumeric(fullyFormattedNumber);
        final Optional<Account> accountWithOldId =
                findAccountWithBankId(accounts, oldUniqueIdentifier);
        final Optional<Account> accountWithNewId =
                findAccountWithBankId(accounts, newUniqueIdentifier);

        if (accountWithOldId.isPresent()) {
            final String oldAccountId = accountWithOldId.get().getId();
            if (accountWithNewId.isPresent()) {
                // duplicate exists already! should be deleted manually
                log.warn(
                        "duplicate pension account: {}, {}",
                        oldAccountId,
                        accountWithNewId.get().getId());
                // use old identifier
                return accountNumber;
            }
            // has old ID only, migrate to new
            log.warn("migrating pension account {} to new identifier", oldAccountId);
            systemUpdater.updateAccountMetaData(oldAccountId, newUniqueIdentifier);
        }

        return fullyFormattedNumber;
    }

    private Optional<Account> findAccountWithBankId(
            List<Account> requestedAccounts, String bankId) {
        if (requestedAccounts == null || requestedAccounts.isEmpty()) {
            return Optional.empty();
        }
        return requestedAccounts.stream()
                .filter(account -> account.getBankId().equals(bankId))
                .findFirst();
    }

    public PortfolioModule toTinkPortfolioModule(SwedbankSEApiClient apiClient) {

        return PortfolioModule.builder()
                .withType(getTinkPortfolioType())
                .withUniqueIdentifier(accountNumber)
                .withCashValue(
                        (getTotalEquities() == null)
                                ? 0.0
                                : getTotalEquities()
                                        .getBuyingPower()
                                        .toTinkAmount()
                                        .getDoubleValue())
                .withTotalProfit(getPerformance().getAmount().toTinkAmount().getDoubleValue())
                .withTotalValue(getTotalValue().toTinkAmount().getDoubleValue())
                .withInstruments(toTinkInstruments(apiClient))
                .build();
    }

    private List<InstrumentModule> toTinkInstruments(SwedbankSEApiClient apiClient) {
        Preconditions.checkNotNull(apiClient, API_CLIENT_ERROR_MESSAGE);

        return Optional.ofNullable(placements).orElseGet(Collections::emptyList).stream()
                .map(placementEntity -> placementEntity.toTinkPensionInstruments(apiClient))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private PortfolioModule.PortfolioType getTinkPortfolioType() {
        if (this.type == null) {
            return PortfolioModule.PortfolioType.OTHER;
        }

        switch (SwedbankBaseConstants.InvestmentAccountType.fromAccountType(type)) {
            case OCCUPATIONAL_PENSION:
            case INDIVIDUAL_SAVINGS_PENSION:
                return PortfolioModule.PortfolioType.PENSION;
            default:
                log.warn("Unknown portfolio type: {}", type);
                return PortfolioModule.PortfolioType.OTHER;
        }
    }
}
