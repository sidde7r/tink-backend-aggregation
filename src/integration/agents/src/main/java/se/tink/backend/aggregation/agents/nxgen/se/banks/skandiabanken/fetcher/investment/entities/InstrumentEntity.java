package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InstrumentEntity {
    @JsonProperty("AskPrice")
    private BigDecimal askPrice;

    @JsonProperty("AskVolume")
    private BigDecimal askVolume;

    @JsonProperty("BidPrice")
    private BigDecimal bidPrice;

    @JsonProperty("BidVolume")
    private BigDecimal bidVolume;

    @JsonProperty("Capitalization")
    private String capitalization = "";

    @JsonProperty("ClosingPaid")
    private BigDecimal closingPaid;

    @JsonProperty("DateOfAllTimeLow")
    private String dateOfAllTimeLow = "";

    @JsonProperty("EPS")
    private BigDecimal ePS;

    @JsonProperty("FirstPaidAtOpening")
    private BigDecimal firstPaidAtOpening;

    @JsonProperty("HighestPaidYTD")
    private BigDecimal highestPaidYTD;

    @JsonProperty("ICBCombinedCode")
    private String iCBCombinedCode = "";

    @JsonProperty("IsMainMarket")
    private boolean isMainMarket;

    @JsonProperty("Issuer")
    private String issuer = "";

    @JsonProperty("ListInfo")
    private String listInfo = "";

    @JsonProperty("LowestPaidYTD")
    private BigDecimal lowestPaidYTD;

    @JsonProperty("NumberOfOptionsPerShare")
    private BigDecimal numberOfOptionsPerShare;

    @JsonProperty("OrganisationId")
    private int organisationId;

    @JsonProperty("PriceChangeOneDay")
    private BigDecimal priceChangeOneDay;

    @JsonProperty("PE")
    private BigDecimal pE;

    @JsonProperty("PEForecast")
    private BigDecimal pEForecast;

    @JsonProperty("PEQ")
    private BigDecimal pEQ;

    @JsonProperty("ProfitMargin")
    private BigDecimal profitMargin;

    @JsonProperty("PS")
    private BigDecimal pS;

    @JsonProperty("RoundLot")
    private int roundLot;

    @JsonProperty("RSI14")
    private BigDecimal rSI14;

    @JsonProperty("Sector")
    private int sector;

    @JsonProperty("ShortName")
    private String shortName = "";

    @JsonProperty("TickSize")
    private BigDecimal tickSize;

    @JsonProperty("TickerVIPUrl")
    private String tickerVIPUrl = "";

    @JsonProperty("TurnoverAmount")
    private BigDecimal turnoverAmount;

    @JsonProperty("UnderlyingSecurity")
    private String underlyingSecurity = "";

    @JsonProperty("Volatility")
    private BigDecimal volatility;

    @JsonProperty("Yield")
    private BigDecimal yield;

    @JsonProperty("HasCorporateActions")
    private boolean hasCorporateActions;

    @JsonProperty("HighPaidToday")
    private BigDecimal highPaidToday;

    @JsonProperty("InstrumentClass")
    private String instrumentClass = "";

    @JsonProperty("InstrumentType")
    private String instrumentType = "";

    @JsonProperty("LatestPaid")
    private BigDecimal latestPaid;

    @JsonProperty("LowPaidToday")
    private BigDecimal lowPaidToday;

    @JsonProperty("OrderBookStatus")
    private int orderBookStatus;

    @JsonProperty("ReturnOneDay")
    private BigDecimal returnOneDay;

    @JsonProperty("ReturnOneMonth")
    private BigDecimal returnOneMonth;

    @JsonProperty("ReturnOneWeek")
    private BigDecimal returnOneWeek;

    @JsonProperty("ReturnOneYear")
    private BigDecimal returnOneYear;

    @JsonProperty("ReturnSixMonths")
    private BigDecimal returnSixMonths;

    @JsonProperty("ReturnThreeMonths")
    private BigDecimal returnThreeMonths;

    @JsonProperty("ReturnYearEnd")
    private BigDecimal returnYearEnd;

    @JsonProperty("Time")
    private String time = "";

    @JsonProperty("TradedVolume")
    private BigDecimal tradedVolume;

    @JsonProperty("TSID")
    private int tSID;

    @JsonProperty("SIXMarketCode")
    private String sIXMarketCode = "";

    @JsonProperty("AssetClassification")
    private int assetClassification;

    @JsonProperty("Currency")
    private String currency = "";

    @JsonProperty("DisplayDecimals")
    private int displayDecimals;

    @JsonProperty("GraphURLLarge")
    private String graphURLLarge = "";

    @JsonProperty("GraphURLSmall")
    private String graphURLSmall = "";

    @JsonProperty("HasAdditionalInformation")
    private boolean hasAdditionalInformation;

    @JsonProperty("IdentityId")
    private String identityId = "";

    @JsonProperty("IsComplex")
    private boolean isComplex;

    @JsonProperty("ISIN")
    private String isin = "";

    @JsonProperty("IsTradable")
    private boolean isTradable;

    @JsonProperty("IsTradedByPercent")
    private boolean isTradedByPercent;

    @JsonProperty("MIC")
    private String mic = "";

    @JsonProperty("Name")
    private String name = "";

    @JsonIgnore
    public String getInstrumentType() {
        return instrumentType;
    }

    @JsonIgnore
    private double getPrice() {
        return askPrice.doubleValue();
    }

    @JsonIgnore
    public Instrument toTinkInstrument() {
        Instrument tinkInstrument = new Instrument();
        tinkInstrument.setUniqueIdentifier(createUniqueId());
        tinkInstrument.setIsin(isin);
        tinkInstrument.setMarketPlace(mic);
        tinkInstrument.setCurrency(currency);
        tinkInstrument.setName(name);
        tinkInstrument.setPrice(getPrice());
        tinkInstrument.setTicker(tickSize.toString());
        tinkInstrument.setType(
                SkandiaBankenConstants.INSTRUMENT_TYPE_MAP
                        .translate(getInstrumentType())
                        .orElse(Instrument.Type.OTHER));
        tinkInstrument.setRawType(getInstrumentType());
        return tinkInstrument;
    }

    @JsonIgnore
    private String createUniqueId() {
        return String.format("%s_%s_%s", isin, currency, mic);
    }
}
