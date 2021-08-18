package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class BankdataDepositAssetEntity {

    private double numberOfShares;
    private double stockValue;
    private double numberOfTradable;
    private String customerBaseCurrency;

    public double getQuantity() {
        return numberOfTradable + numberOfShares;
    }
}
