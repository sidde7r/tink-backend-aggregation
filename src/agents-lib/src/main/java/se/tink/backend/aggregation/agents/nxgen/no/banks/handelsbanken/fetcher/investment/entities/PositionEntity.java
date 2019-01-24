package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.investment.entities;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.HandelsbankenNOConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.agents.models.Instrument;

@JsonObject
public class PositionEntity {
    private double profit;
    private double costPrice;
    private double realizedProfit;
    private Integer dividend;
    private String lastUpdate;
    private SecurityEntity security;
    private String uri;
    private Integer redeemedVolume;
    private CsdAccountEntity csdAccount;
    private String marketValueDateTime;
    private String marketValueSource;
    private double volume;
    private double marketValuePerUnit;
    private double transactionFee;
    private double unrealizedProfit;
    private double marketValue;
    private double avgCostPrice;
    private boolean tradeAmountMissingForCostPrice;
    private List<DividendHistoryEntity> dividendHistoryList;

    public double getProfit() {
        return profit;
    }

    public double getCostPrice() {
        return costPrice;
    }

    public double getRealizedProfit() {
        return realizedProfit;
    }

    public Integer getDividend() {
        return dividend;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public SecurityEntity getSecurity() {
        return security;
    }

    public String getUri() {
        return uri;
    }

    public Integer getRedeemedVolume() {
        return redeemedVolume;
    }

    public CsdAccountEntity getCsdAccount() {
        return csdAccount;
    }

    public String getMarketValueDateTime() {
        return marketValueDateTime;
    }

    public String getMarketValueSource() {
        return marketValueSource;
    }

    public double getVolume() {
        return volume;
    }

    public double getMarketValuePerUnit() {
        return marketValuePerUnit;
    }

    public double getTransactionFee() {
        return transactionFee;
    }

    public double getUnrealizedProfit() {
        return unrealizedProfit;
    }

    public double getMarketValue() {
        return marketValue;
    }

    public double getAvgCostPrice() {
        return avgCostPrice;
    }

    public boolean isTradeAmountMissingForCostPrice() {
        return tradeAmountMissingForCostPrice;
    }

    public List<DividendHistoryEntity> getDividendHistoryList() {
        return dividendHistoryList;
    }

    public Instrument toInstrument() {
        Instrument instrument = new Instrument();

        instrument.setUniqueIdentifier(security.getIsin() + HandelsbankenNOConstants.InvestmentConstants.HB_NORWAY);
        instrument.setIsin(security.getIsin());
        instrument.setTicker(security.getSecurityTicker());
        instrument.setName(security.getSecurityName());
        instrument.setQuantity(volume);
        instrument.setType(getTinkSecurityType());
        instrument.setRawType(security.getSecurityType());
        instrument.setPrice(marketValuePerUnit);
        instrument.setMarketValue(marketValue);
        instrument.setProfit(profit);
        instrument.setAverageAcquisitionPrice(avgCostPrice);

        return instrument;
    }

    private Instrument.Type getTinkSecurityType() {
        switch (security.getSecurityType().toLowerCase()) {
        case "stock":
            return Instrument.Type.STOCK;
        case "equityfund":
            return Instrument.Type.FUND;
        default:
            return Instrument.Type.OTHER;
        }
    }
}
