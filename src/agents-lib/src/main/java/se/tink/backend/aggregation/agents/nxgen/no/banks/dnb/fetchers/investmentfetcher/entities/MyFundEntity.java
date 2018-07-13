package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.fetchers.investmentfetcher.entities;

import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.system.rpc.Instrument;

@JsonObject
public class MyFundEntity {
    private String name;
    private double sum;
    private double returnSum;
    private String productSystem;
    private String productId;
    private double shares;
    private String holdingType;
    private String owner;
    private double price;
    private double costPrice;
    private boolean fundSuspended;
    private boolean fundSuspendedBuy;
    private boolean fundSuspendedSell;
    private boolean accountSuspended;
    private double amountAvailable;
    private boolean amountAvailableInProcess;
    private boolean allowedToPurchaseMore;
    private boolean allowedToSell;
    private boolean allowedToSwitch;
    private double returnOfInvestmentPercentage;
    private List<FundAccountInMyFundEntity> fundAccounts;
    private boolean ips;
    private boolean onlyIPS;

    // Note: not from response, set manually.
    private String isin;

    public String getName() {
        return name;
    }

    public double getSum() {
        return sum;
    }

    public double getReturnSum() {
        return returnSum;
    }

    public String getProductSystem() {
        return productSystem;
    }

    public String getProductId() {
        return productId;
    }

    public double getShares() {
        return shares;
    }

    public String getHoldingType() {
        return holdingType;
    }

    public String getOwner() {
        return owner;
    }

    public double getPrice() {
        return price;
    }

    public double getCostPrice() {
        return costPrice;
    }

    public boolean isFundSuspended() {
        return fundSuspended;
    }

    public boolean isFundSuspendedBuy() {
        return fundSuspendedBuy;
    }

    public boolean isFundSuspendedSell() {
        return fundSuspendedSell;
    }

    public boolean isAccountSuspended() {
        return accountSuspended;
    }

    public double getAmountAvailable() {
        return amountAvailable;
    }

    public boolean isAmountAvailableInProcess() {
        return amountAvailableInProcess;
    }

    public boolean isAllowedToPurchaseMore() {
        return allowedToPurchaseMore;
    }

    public boolean isAllowedToSell() {
        return allowedToSell;
    }

    public boolean isAllowedToSwitch() {
        return allowedToSwitch;
    }

    public double getReturnOfInvestmentPercentage() {
        return returnOfInvestmentPercentage;
    }

    public List<FundAccountInMyFundEntity> getFundAccounts() {
        return fundAccounts;
    }

    public boolean isIps() {
        return ips;
    }

    public boolean isOnlyIPS() {
        return onlyIPS;
    }

    public Map<Instrument, String> toTinkInstrument() {

        if (isin == null) {
            throw new IllegalStateException("ISIN is not set");
        }

        Map<Instrument, String> instrumentAccountMap = new HashMap<>();

        fundAccounts.stream().forEach(fundAccountInMyFundEntity -> {

            // NOTE: Since for dnb, One instrument can be shared among multiple portfolio accounts, make it infeasible to
            // trace some data on portfolio accounts level, e.g. profit, quantity etc. So those fields are not set here
            // to avoid wrong information.
            Instrument instrument = new Instrument();
            instrument.setName(name);
            instrument.setType(Instrument.Type.FUND);
            instrument.setPrice(price);
            instrument.setRawType(productSystem + "-" + productId);
            instrument.setMarketValue(fundAccountInMyFundEntity.getSum());
            Preconditions.checkArgument(shares > 0);
            instrument.setAverageAcquisitionPrice(costPrice / shares);

            instrument.setUniqueIdentifier(isin + "-DNB-NORWAY");
            instrument.setIsin(isin);

            instrumentAccountMap.put(instrument, fundAccountInMyFundEntity.getAccountNumber());
        });
        return instrumentAccountMap;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

}
