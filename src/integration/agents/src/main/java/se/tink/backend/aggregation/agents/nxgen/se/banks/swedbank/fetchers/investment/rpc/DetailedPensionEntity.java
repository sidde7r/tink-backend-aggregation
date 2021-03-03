package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Getter
@JsonObject
public class DetailedPensionEntity {

    @JsonIgnore
    private static final Logger log = LoggerFactory.getLogger(DetailedPensionEntity.class);

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

    public InvestmentAccount toTinkInvestmentAccount(SwedbankSEApiClient apiClient) {
        return InvestmentAccount.nxBuilder()
                .withPortfolios(toTinkPortfolioModule(apiClient))
                .withCashBalance(ExactCurrencyAmount.inSEK(0.0))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountNumber)
                                .withAccountNumber(fullyFormattedNumber)
                                .withAccountName(name)
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifier.Type.SE, fullyFormattedNumber))
                                .build())
                .build();
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
