package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarConstants.Accounts;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule.InstrumentType;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.id.InstrumentIdModule;

@JsonObject
public class IskFundEntity {
    private String isinCode;
    private boolean sellable;
    private String administrationFee;
    private double minimalReinvestmentAmount;
    private double minimumMonthlySavingsAmount;
    private double buyFeeInPercent;
    private double saleFeeInPercent;
    private Date valuationDate;
    private String ppmNumber;
    private FundDetailsEntity fund;

    @JsonIgnore
    public InstrumentModule toTinkInstrument() {
        return InstrumentModule.builder()
                .withType(InstrumentType.FUND)
                .withId(getIdModule())
                .withMarketPrice(fund.getHolding().getPurchaseValue())
                .withMarketValue(fund.getHolding().getTotalMarketValue())
                .withAverageAcquisitionPrice(
                        fund.getHolding().getPurchaseValue()
                                / fund.getHolding().getNumberOfShares())
                .withCurrency(Accounts.CURRENCY)
                .withQuantity(fund.getHolding().getNumberOfShares())
                .withProfit(fund.getHolding().getDevelopment())
                .setRawType(fund.getType())
                .build();
    }

    private InstrumentIdModule getIdModule() {
        return InstrumentIdModule.builder()
                .withUniqueIdentifier(isinCode + fund.getFundId())
                .withName(fund.getName())
                .setIsin(isinCode)
                .setMarketPlace(fund.getCompany())
                .build();
    }
}
