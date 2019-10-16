package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.RevolutConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.id.InstrumentIdModule;

@JsonObject
public class HoldingsItemEntity {

    @JsonProperty("pending")
    private PendingEntity pendingEntity;

    @JsonProperty("assetSource")
    private String assetSource;

    @JsonProperty("assetType")
    private String assetType;

    @JsonProperty("createdDate")
    private long createdDate;

    @JsonProperty("performance")
    private List<Object> performance;

    @JsonProperty("balance")
    private BalanceEntity balanceEntity;

    @JsonProperty("name")
    private String name;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("details")
    private DetailsEntity detailsEntity;

    @JsonProperty("id")
    private String id;

    @JsonProperty("state")
    private String state;

    @JsonProperty("averagePrice")
    private AveragePriceEntity averagePriceEntity;

    @JsonProperty("assetInfo")
    private AssetInfoEntity assetInfoEntity;

    public DetailsEntity getDetailsEntity() {
        return detailsEntity;
    }

    public PendingEntity getPendingEntity() {
        return pendingEntity;
    }

    public String getAssetSource() {
        return assetSource;
    }

    public String getAssetType() {
        return assetType;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public List<Object> getPerformance() {
        return performance;
    }

    public BalanceEntity getBalanceEntity() {
        return balanceEntity;
    }

    public String getName() {
        return name;
    }

    public String getCurrency() {
        return currency;
    }

    public String getId() {
        return id;
    }

    public String getState() {
        return state;
    }

    public AveragePriceEntity getAveragePriceEntity() {
        return averagePriceEntity;
    }

    public AssetInfoEntity getAssetInfoEntity() {
        return assetInfoEntity;
    }

    public boolean isValidInstrument() {
        return RevolutConstants.INVESTMENT_TYPE_MAPPER.translate(assetType).isPresent();
    }

    private InstrumentModule.InstrumentType getType() {
        return RevolutConstants.INVESTMENT_TYPE_MAPPER
                .translate(assetType)
                .orElse(InstrumentModule.InstrumentType.OTHER);
    }

    private InvestmentResultEntity getInvestment(
            List<InvestmentResultEntity> investmentResultEntities) {
        return investmentResultEntities.stream()
                .filter(x -> x.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not find investment"));
    }

    private String getIsin(List<InvestmentResultEntity> investmentResultEntities) {
        return getInvestment(investmentResultEntities).getIsin();
    }

    private InstrumentIdModule getIdModule(List<InvestmentResultEntity> investmentResultEntities) {
        return InstrumentIdModule.builder()
                .withUniqueIdentifier(id)
                .withName(name)
                .setIsin(getIsin(investmentResultEntities))
                .build();
    }

    public InstrumentModule toTinkInstrument(
            List<InvestmentResultEntity> investmentResultEntities) {
        return InstrumentModule.builder()
                .withType(getType())
                .withId(getIdModule(investmentResultEntities))
                .withMarketPrice(getInvestment(investmentResultEntities).getLastPrice())
                .withMarketValue(0) // NOT available in revolut
                .withAverageAcquisitionPrice(
                        getInvestment(investmentResultEntities).getExecutedPrice())
                .withCurrency(currency)
                .withQuantity(Double.parseDouble(balanceEntity.getQuantity()))
                .withProfit(getInvestment(investmentResultEntities).getProfit())
                .setTicker(detailsEntity.getTicker())
                .build();
    }
}
