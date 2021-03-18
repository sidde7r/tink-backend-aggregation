package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.CaixaApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.CaixaConstants;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities.AssetEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities.AssetsByTypeContainerEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities.InvestmentAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities.QuoteEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule.InstrumentType;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.id.InstrumentIdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule.PortfolioType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;

public class CaixaInvestmentAccountFetcher implements AccountFetcher<InvestmentAccount> {

    private static final Logger log = LoggerFactory.getLogger(CaixaInvestmentAccountFetcher.class);
    private static final int VALUE_DECIMAL_PLACES = 2;

    private final CaixaApiClient apiClient;

    public CaixaInvestmentAccountFetcher(CaixaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {

        return apiClient.fetchInvestmentAccounts().getAccounts().orElse(Collections.emptyList())
                .stream()
                .map(
                        account ->
                                InvestmentAccount.nxBuilder()
                                        .withPortfolios(buildPortfolios(account))
                                        .withZeroCashBalance(account.getCurrency())
                                        .withId(buildId(account))
                                        .build())
                .collect(Collectors.toList());
    }

    private IdModule buildId(InvestmentAccountEntity account) {

        return IdModule.builder()
                .withUniqueIdentifier(account.getFullAccountKey())
                .withAccountNumber(account.getAccountNumber())
                .withAccountName(account.getDescription())
                .addIdentifier(
                        AccountIdentifier.create(
                                AccountIdentifierType.IBAN, account.getFullAccountKey()))
                .build();
    }

    private List<PortfolioModule> buildPortfolios(InvestmentAccountEntity account) {
        return apiClient.fetchInvestmentPortfolio(account.getFullAccountKey())
                .getAssetsByAssetTypeList().stream()
                .map(this::mapToPortfolio)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<PortfolioModule> mapToPortfolio(
            AssetsByTypeContainerEntity assetsByTypeContainerEntity) {

        Optional<InstrumentType> typeOfInstrumentsInPortfolio =
                CaixaConstants.INSTRUMENT_TYPE_MAPPER.translate(
                        assetsByTypeContainerEntity.getAssetType().getAssetTypeId());

        if (!typeOfInstrumentsInPortfolio.isPresent()) {
            log.warn(
                    "Unknown instrument type: {} withd id {}, skipping the mapping.",
                    assetsByTypeContainerEntity.getAssetType().getDescription(),
                    assetsByTypeContainerEntity.getAssetType().getAssetTypeId());
            return Optional.empty();
        }

        return Optional.of(buildPortfolio(assetsByTypeContainerEntity));
    }

    private PortfolioModule buildPortfolio(
            AssetsByTypeContainerEntity assetsByTypeContainerEntity) {

        List<InstrumentModule> instruments = mapToInstruments(assetsByTypeContainerEntity);

        return PortfolioModule.builder()
                .withType(PortfolioType.DEPOT)
                .withUniqueIdentifier(
                        // assets are grouped into porfolios by asset type, so asset type id
                        // is a proper unique identifier
                        assetsByTypeContainerEntity.getAssetType().getAssetTypeId())
                .withCashValue(0) // not supported
                .withTotalProfit(calculateInstrumentsProfit(instruments))
                .withTotalValue(calculateInstrumentsTotalValue(instruments))
                .withInstruments(instruments)
                .setRawType(assetsByTypeContainerEntity.getAssetType().getDescription())
                .build();
    }

    private List<InstrumentModule> mapToInstruments(
            AssetsByTypeContainerEntity assetsByTypeContainerEntity) {

        return assetsByTypeContainerEntity.getAssets().stream()
                .map(this::mapToInstrument)
                .collect(Collectors.toList());
    }

    private Double calculateInstrumentsTotalValue(List<InstrumentModule> instruments) {
        return instruments.stream()
                .map(InstrumentModule::getMarketValue)
                .reduce(Double::sum)
                .orElse(0d);
    }

    private Double calculateInstrumentsProfit(List<InstrumentModule> instruments) {
        return instruments.stream().map(InstrumentModule::getProfit).reduce(Double::sum).orElse(0d);
    }

    private InstrumentModule mapToInstrument(AssetEntity instrument) {
        QuoteEntity details =
                apiClient
                        .fetchMarketDetails(instrument.getAssetType().getAssetTypeId())
                        .getQuotes()
                        .get(0);

        BigDecimal marketValue =
                instrument.getAvailableValuation().movePointLeft(VALUE_DECIMAL_PLACES);
        BigDecimal closingPrice = details.getLast().movePointLeft(details.getDecimalPlaces());
        BigDecimal quantity = calculateQuantity(instrument, marketValue, closingPrice);
        BigDecimal averageAcquisitionPrice =
                instrument.getAverageBuyingUnitaryAmount().movePointLeft(VALUE_DECIMAL_PLACES);

        return InstrumentModule.builder()
                .withType(
                        CaixaConstants.INSTRUMENT_TYPE_MAPPER
                                .translate(instrument.getAssetType().getAssetTypeId())
                                .orElseThrow(IllegalArgumentException::new))
                .withId(getInstrumentId(instrument))
                .withMarketPrice(closingPrice.doubleValue())
                .withMarketValue(marketValue.doubleValue())
                .withAverageAcquisitionPrice(averageAcquisitionPrice.doubleValue())
                .withCurrency(instrument.getCurrency())
                .withQuantity(quantity.doubleValue())
                .withProfit(
                        calculateProfit(marketValue, averageAcquisitionPrice, quantity)
                                .doubleValue())
                .setRawType(instrument.getAssetType().getDescription())
                .build();
    }

    private InstrumentIdModule getInstrumentId(AssetEntity instrument) {
        return InstrumentIdModule.of(
                instrument.getAssetISIN(),
                instrument.getMarket().getQuoteMarketDescription(),
                instrument.getAssetName(),
                instrument.getAssetId());
    }

    private BigDecimal calculateQuantity(
            AssetEntity instrument, BigDecimal marketValue, BigDecimal closingPrice) {

        return Optional.ofNullable(instrument.getAvailableQuantity())
                .map(q -> q.movePointLeft(instrument.getQuantityDecimalPlaces()))
                .orElse(marketValue.divide(closingPrice, RoundingMode.UNNECESSARY));
    }

    private BigDecimal calculateProfit(
            BigDecimal marketValue, BigDecimal averageAcquisitionPrice, BigDecimal quantity) {
        return marketValue.subtract(averageAcquisitionPrice.multiply(quantity));
    }
}
