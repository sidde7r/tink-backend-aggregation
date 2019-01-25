package se.tink.backend.aggregation.agents.banks.danskebank.v2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.system.rpc.Instrument;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaperEntity {
    @JsonProperty("PaperId")
    private String paperId;
    @JsonProperty("PaperIsFund")
    private Boolean paperIsFund;
    @JsonProperty("PaperName")
    private String paperName;
    @JsonProperty("PaperPriceUpdateInterval")
    private Integer paperPriceUpdateInterval;
    @JsonProperty("GainChangeNominal")
    private Double gainChangeNominal;
    @JsonProperty("GainChangePercent")
    private Double gainChangePercent;
    @JsonProperty("PaperCount")
    private Double paperCount;
    @JsonProperty("PaperCountDecimals")
    private Integer paperCountDecimals;
    @JsonProperty("PaperCountText")
    private String paperCountText;
    @JsonProperty("PaperPrice")
    private Double paperPrice;
    @JsonProperty("PaperPriceChangeNominal")
    private Double paperPriceChangeNominal;
    @JsonProperty("PaperPriceChangePercent")
    private Double paperPriceChangePercent;
    @JsonProperty("PaperPriceDecimals")
    private Double paperPriceDecimals;
    @JsonProperty("PaperTotalValue")
    private Double paperTotalValue;
    @JsonProperty("PaperTotalValueDecimals")
    private Integer paperTotalValueDecimals;
    @JsonProperty("PortfolioShare")
    private Double portfolioShare;
    @JsonProperty("PortfolioShareDecimals")
    private Integer portfolioShareDecimals;
    @JsonProperty("Untreated")
    private Double untreated;
    @JsonProperty("UntreatedText")
    private String untreatedText;

    public String getPaperId() {
        return paperId;
    }

    public void setPaperId(String paperId) {
        this.paperId = paperId;
    }

    public Boolean getPaperIsFund() {
        return paperIsFund;
    }

    public void setPaperIsFund(Boolean paperIsFund) {
        this.paperIsFund = paperIsFund;
    }

    public String getPaperName() {
        return paperName;
    }

    public void setPaperName(String paperName) {
        this.paperName = paperName;
    }

    public Integer getPaperPriceUpdateInterval() {
        return paperPriceUpdateInterval;
    }

    public void setPaperPriceUpdateInterval(Integer paperPriceUpdateInterval) {
        this.paperPriceUpdateInterval = paperPriceUpdateInterval;
    }

    public Double getGainChangeNominal() {
        return gainChangeNominal;
    }

    public void setGainChangeNominal(Double gainChangeNominal) {
        this.gainChangeNominal = gainChangeNominal;
    }

    public Double getGainChangePercent() {
        return gainChangePercent;
    }

    public void setGainChangePercent(Double gainChangePercent) {
        this.gainChangePercent = gainChangePercent;
    }

    public Double getPaperCount() {
        return paperCount;
    }

    public void setPaperCount(Double paperCount) {
        this.paperCount = paperCount;
    }

    public Integer getPaperCountDecimals() {
        return paperCountDecimals;
    }

    public void setPaperCountDecimals(Integer paperCountDecimals) {
        this.paperCountDecimals = paperCountDecimals;
    }

    public String getPaperCountText() {
        return paperCountText;
    }

    public void setPaperCountText(String paperCountText) {
        this.paperCountText = paperCountText;
    }

    public Double getPaperPrice() {
        return paperPrice;
    }

    public void setPaperPrice(Double paperPrice) {
        this.paperPrice = paperPrice;
    }

    public Double getPaperPriceChangeNominal() {
        return paperPriceChangeNominal;
    }

    public void setPaperPriceChangeNominal(Double paperPriceChangeNominal) {
        this.paperPriceChangeNominal = paperPriceChangeNominal;
    }

    public Double getPaperPriceChangePercent() {
        return paperPriceChangePercent;
    }

    public void setPaperPriceChangePercent(Double paperPriceChangePercent) {
        this.paperPriceChangePercent = paperPriceChangePercent;
    }

    public Double getPaperPriceDecimals() {
        return paperPriceDecimals;
    }

    public void setPaperPriceDecimals(Double paperPriceDecimals) {
        this.paperPriceDecimals = paperPriceDecimals;
    }

    public Double getPaperTotalValue() {
        return paperTotalValue;
    }

    public void setPaperTotalValue(Double paperTotalValue) {
        this.paperTotalValue = paperTotalValue;
    }

    public Integer getPaperTotalValueDecimals() {
        return paperTotalValueDecimals;
    }

    public void setPaperTotalValueDecimals(Integer paperTotalValueDecimals) {
        this.paperTotalValueDecimals = paperTotalValueDecimals;
    }

    public Double getPortfolioShare() {
        return portfolioShare;
    }

    public void setPortfolioShare(Double portfolioShare) {
        this.portfolioShare = portfolioShare;
    }

    public Integer getPortfolioShareDecimals() {
        return portfolioShareDecimals;
    }

    public void setPortfolioShareDecimals(Integer portfolioShareDecimals) {
        this.portfolioShareDecimals = portfolioShareDecimals;
    }

    public Double getUntreated() {
        return untreated;
    }

    public void setUntreated(Double untreated) {
        this.untreated = untreated;
    }

    public String getUntreatedText() {
        return untreatedText;
    }

    public void setUntreatedText(String untreatedText) {
        this.untreatedText = untreatedText;
    }

    private Double getProfit() {
        // GainChangeNominal is not always set. Instead multiply the priceChangePerPaper with paperCount.
        return getGainChangeNominal() != null ? getGainChangeNominal() :
                getPaperPriceChangeNominal() * getPaperCount();
    }

    private Double calculateAverageAcquisitionPrice() {
        Double totalValue = getPaperTotalValue();
        Double totalChange = getProfit();
        Double paperCount = getPaperCount();

        if (totalValue == null || totalChange == null || paperCount == null) {
            return null;
        }

        return (totalValue - totalChange) / paperCount;
    }

    public Optional<Instrument> toInstrument() {
        Instrument instrument = new Instrument();

        if (getPaperCount().doubleValue() == 0) {
            return Optional.empty();
        }

        instrument.setAverageAcquisitionPrice(calculateAverageAcquisitionPrice());
        instrument.setIsin(getPaperId());
        instrument.setMarketValue(getPaperTotalValue());
        instrument.setName(getPaperName());
        instrument.setPrice(getPaperPrice());
        instrument.setProfit(getProfit());
        instrument.setQuantity(getPaperCount());
        instrument.setRawType(String.format("Paper is fund: %s", String.valueOf(getPaperIsFund())));
        instrument.setType(getInstrumentType());
        instrument.setUniqueIdentifier(getPaperId());

        return Optional.of(instrument);
    }

    private Instrument.Type getInstrumentType() {
        if (getPaperIsFund()) {
            return Instrument.Type.FUND;
        } else {
            return Instrument.Type.OTHER;
        }
    }
}
