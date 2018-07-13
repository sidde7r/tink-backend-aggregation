package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.system.rpc.Instrument;
import se.tink.backend.utils.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecurityHoldingsResponse extends AbstractResponse {
    private String title;
    private String name;
    private SecurityIdentifierEntity identifier;
    private InstrumentSummaryEntity instrumentSummary;
    private CustodyHoldingsEntity holdingDetail;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SecurityIdentifierEntity getIdentifier() {
        if (identifier == null) {
            identifier = new SecurityIdentifierEntity();
        }
        return identifier;
    }

    public void setIdentifier(
            SecurityIdentifierEntity identifier) {
        this.identifier = identifier;
    }

    public InstrumentSummaryEntity getInstrumentSummary() {
        return instrumentSummary;
    }

    public void setInstrumentSummary(
            InstrumentSummaryEntity instrumentSummary) {
        this.instrumentSummary = instrumentSummary;
    }

    public CustodyHoldingsEntity getHoldingDetail() {
        if (holdingDetail == null) {
            holdingDetail = new CustodyHoldingsEntity();
        }
        return holdingDetail;
    }

    public void setHoldingDetail(
            CustodyHoldingsEntity holdingDetail) {
        this.holdingDetail = holdingDetail;
    }

    public Optional<Instrument> toInstrument() {
        Instrument instrument = new Instrument();

        double quantity = StringUtils.parseAmount(getHoldingDetail().getHoldingQuantity().getQuantityFormatted());
        if (quantity == 0) {
            return Optional.empty();
        }

        Map<String, String> instrumentDetails = toInstrumentDetails();

        instrument.setAverageAcquisitionPrice(toAveragePurchasePrice());
        instrument.setCurrency(getIdentifier().getCurrency());
        instrument.setIsin(instrumentDetails.get("isin"));
        instrument.setMarketPlace(instrumentDetails.get("lista"));
        instrument.setMarketValue(toMarketValue());
        instrument.setName(instrumentDetails.get("namn"));
        instrument.setPrice(toMarketPrice());
        instrument.setProfit(toPerformanceChangeAmount());
        instrument.setQuantity(quantity);
        instrument.setRawType(toRawType());
        instrument.setType(getInstrumentType());
        instrument.setUniqueIdentifier(createUniqueIdentifier(instrument));

        return Optional.of(instrument);
    }

    private Map<String, String> toInstrumentDetails() {
        if (instrumentSummary != null) {
            InstrumentDetailWrapper instrumentDetail = instrumentSummary.getInstrumentDetail();
            if (instrumentDetail != null) {
                return instrumentDetail.asMapKeyValueMap();
            }
        }
        return Collections.emptyMap();
    }

    private String createUniqueIdentifier(Instrument instrument) {
        String marketPlace = instrument.getMarketPlace();
        return instrument.getIsin() + (marketPlace == null ? "" : marketPlace.trim());
    }

    private Double toAveragePurchasePrice() {
        AmountEntity averagePurchasePrice = getHoldingDetail().getAveragePurchasePrice();
        return averagePurchasePrice != null ? averagePurchasePrice.getAmount() : null;
    }

    private Double toMarketValue() {
        AmountEntity marketValue = getHoldingDetail().getMarketValue();
        return marketValue != null ? marketValue.getAmount() : null;
    }

    private Double toMarketPrice() {
        AmountEntity marketPrice = getHoldingDetail().getMarketPrice();
        return marketPrice != null ? marketPrice.getAmount() : null;
    }

    private Double toPerformanceChangeAmount() {
        PerformanceEntity performance = getHoldingDetail().getPerformance();
        if (performance != null) {
            AmountEntity changeAmount = performance.getChangeAmount();
            if (changeAmount != null) {
                return changeAmount.getAmount();
            }
        }
        return null;
    }

    private String toRawType() {
        String type = getIdentifier().getType();
        return type != null ? type.toLowerCase() : null;
    }

    private Instrument.Type getInstrumentType() {
        String rawType = toRawType();
        if (rawType == null) {
            return Instrument.Type.OTHER;
        }
        switch (rawType) {
        case "stock":
            return Instrument.Type.STOCK;
        case "fund":
            return Instrument.Type.FUND;
        default:
            return Instrument.Type.OTHER;
        }
    }
}
