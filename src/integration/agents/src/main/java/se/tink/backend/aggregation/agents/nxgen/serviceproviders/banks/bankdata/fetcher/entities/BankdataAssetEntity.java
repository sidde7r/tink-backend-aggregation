package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities;

import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc.AssetDetailsResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankdataAssetEntity {
    private String securityId;
    private String name;
    private double stockPrice;
    private double deltaStockPricePct;
    private int assetType;
    private double ratePoint;
    private double stockValue;
    private boolean showDeltaStockPricePct;
    private String isinCode;
    private String currency;
    private double numberOfTradable;
    private double numberOfShares;

    public String getSecurityId() {
        return securityId;
    }

    public String getName() {
        return name;
    }

    public double getStockPrice() {
        return stockPrice;
    }

    public double getDeltaStockPricePct() {
        return deltaStockPricePct;
    }

    public int getAssetType() {
        return assetType;
    }

    public double getRatePoint() {
        return ratePoint;
    }

    public double getStockValue() {
        return stockValue;
    }

    public boolean isShowDeltaStockPricePct() {
        return showDeltaStockPricePct;
    }

    public String getIsinCode() {
        return isinCode;
    }

    public String getCurrency() {
        return currency;
    }

    public double getNumberOfTradable() {
        return numberOfTradable;
    }

    public double getNumberOfShares() {
        return numberOfShares;
    }

    public boolean isKnownType() {
        return BankdataConstants.INSTRUMENT_TYPES.containsKey(assetType);
    }

    public Instrument.Type getType() {
        return BankdataConstants.INSTRUMENT_TYPES.getOrDefault(assetType, Instrument.Type.OTHER);
    }

    public Instrument toTinkInstrument(AssetDetailsResponse assetDetails) {
        String marketPlace = assetDetails.getStockExchangeName();

        Instrument instrument = new Instrument();
        instrument.setUniqueIdentifier(isinCode + marketPlace);
        instrument.setType(getType());
        instrument.setRawType(String.valueOf(assetType));
        instrument.setCurrency(currency);
        instrument.setIsin(isinCode);
        instrument.setMarketPlace(marketPlace);
        instrument.setMarketValue(stockValue);
        instrument.setName(name);
        instrument.setPrice(stockPrice);
        instrument.setProfit(assetDetails.getReturns());
        instrument.setQuantity(assetDetails.getDepositAsset().getQuantity());
        return instrument;
    }
}
