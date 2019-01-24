package se.tink.backend.aggregation.agents.banks.skandiabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Optional;
import se.tink.backend.system.rpc.Instrument;
import se.tink.libraries.strings.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HoldingEntity {
    private String currency;
    private String id;
    private String marketValue;
    private String netAssetValue;
    private int order;
    private String procurementValue;
    private String progress;
    private String shares;
    private double sharesRaw;
    private StockEntity stock;
    private FundEntity fund;
    private int type; // 1 is fund, 2 is stock

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getMarketValue() {
        return marketValue == null || marketValue.isEmpty() ? null : StringUtils.parseAmount(marketValue);
    }

    public void setMarketValue(String marketValue) {
        this.marketValue = marketValue;
    }

    public Double getNetAssetValue() {
        return netAssetValue == null || netAssetValue.isEmpty() ? null : StringUtils.parseAmount(netAssetValue);
    }

    public void setNetAssetValue(String netAssetValue) {
        this.netAssetValue = netAssetValue;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public double getProcurementValue() {
        return procurementValue == null || procurementValue.isEmpty() ? 0 : StringUtils.parseAmount(procurementValue);
    }

    public void setProcurementValue(String procurementValue) {
        this.procurementValue = procurementValue;
    }

    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public String getShares() {
        return shares;
    }

    public void setShares(String shares) {
        this.shares = shares;
    }

    public double getSharesRaw() {
        return sharesRaw;
    }

    public void setSharesRaw(double sharesRaw) {
        this.sharesRaw = sharesRaw;
    }

    public StockEntity getStock() {
        return stock;
    }

    public void setStock(StockEntity stock) {
        this.stock = stock;
    }

    public FundEntity getFund() {
        return fund;
    }

    public void setFund(FundEntity fund) {
        this.fund = fund;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Optional<Instrument> fundToInstrument() {
        Instrument instrument = new Instrument();

        if (getSharesRaw() == 0) {
            return Optional.empty();
        }

        instrument.setAverageAcquisitionPrice(getProcurementValue() / getSharesRaw());
        instrument.setCurrency(getFund().getCurrency());
        instrument.setIsin(getFund().getIsin());
        instrument.setMarketValue(getMarketValue());
        instrument.setName(getFund().getName());
        instrument.setPrice(getNetAssetValue());
        instrument.setProfit(getMarketValue() - getProcurementValue());
        instrument.setQuantity(getSharesRaw());
        instrument.setRawType(String.format("type - code: %s, id: %s", getType(), getId()));
        instrument.setType(Instrument.Type.FUND);
        instrument.setUniqueIdentifier(getId());

        return Optional.of(instrument);
    }

    public Optional<Instrument> stockToInstrument() {
        Instrument instrument = new Instrument();

        if (getSharesRaw() == 0) {
            return Optional.empty();
        }

        instrument.setAverageAcquisitionPrice(getProcurementValue() / getSharesRaw());
        instrument.setCurrency(getStock().getCurrency());
        instrument.setIsin(getStock().getIsin());
        instrument.setMarketPlace(getStock().getMarket());
        instrument.setMarketValue(getMarketValue());
        instrument.setName(getStock().getName());
        instrument.setPrice(getNetAssetValue());
        instrument.setProfit(getMarketValue() - getProcurementValue());
        instrument.setQuantity(getSharesRaw());
        instrument.setRawType(String.format("type - code: %s, id: %s", getType(), getId()));
        instrument.setType(Instrument.Type.STOCK);
        instrument.setUniqueIdentifier(getId());

        return Optional.of(instrument);
    }
}
