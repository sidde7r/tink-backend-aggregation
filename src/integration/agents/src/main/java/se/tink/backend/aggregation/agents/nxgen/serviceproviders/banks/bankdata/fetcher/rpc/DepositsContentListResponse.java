package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities.BankdataAssetEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DepositsContentListResponse {
    private double returns;
    private double value;

    private List<BankdataAssetEntity> danishStocks;
    private List<BankdataAssetEntity> danishBonds;
    private List<BankdataAssetEntity> foreignStocks;
    private List<BankdataAssetEntity> foreignBonds;

    // these are most likely a list of BankDataInstrumentEntity
    private List<Object> extractedBonds;
    private List<Object> danishMiscellaneousAssets;
    private List<Object> foreignMiscellaneousAssets;
    private List<Object> danishPrizeBonds;
    private List<Object> foreignPrizeBonds;

    private String presentationCurrency;

    public double getReturns() {
        return returns;
    }

    public double getValue() {
        return value;
    }

    public List<BankdataAssetEntity> getDanishStocks() {
        return danishStocks;
    }

    public List<BankdataAssetEntity> getForeignStocks() {
        return foreignStocks;
    }

    public List<BankdataAssetEntity> getForeignBonds() {
        return foreignBonds;
    }

    public List<BankdataAssetEntity> getDanishBonds() {
        return danishBonds;
    }

    public List<Object> getExtractedBonds() {
        return extractedBonds;
    }

    public List<Object> getDanishMiscellaneousAssets() {
        return danishMiscellaneousAssets;
    }

    public List<Object> getForeignMiscellaneousAssets() {
        return foreignMiscellaneousAssets;
    }

    public List<Object> getDanishPrizeBonds() {
        return danishPrizeBonds;
    }

    public List<Object> getForeignPrizeBonds() {
        return foreignPrizeBonds;
    }

    public String getPresentationCurrency() {
        return presentationCurrency;
    }
}
