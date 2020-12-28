package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument;

import com.google.common.base.Preconditions;
import java.math.BigDecimal;
import java.util.Objects;
import javax.annotation.Nonnull;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Instrument.Type;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.id.InstrumentIdModule;
import se.tink.libraries.amount.ExactCurrencyAmount;

public final class InstrumentModule {
    private final InstrumentIdModule instrumentIdModule;
    private final BigDecimal averageAcquisitionPrice;
    private final String currency;
    private final double marketValue;
    private final double price;
    private final double quantity;
    private final Double profit;
    private final String ticker;
    private final InstrumentType type;
    private final String rawType;

    public Instrument toSystemInstrument() {
        Instrument systemInstrument = new Instrument();
        systemInstrument.setUniqueIdentifier(this.instrumentIdModule.getUniqueIdentifier());
        systemInstrument.setIsin(this.instrumentIdModule.getIsin());
        systemInstrument.setMarketPlace(this.instrumentIdModule.getMarketPlace());
        if (Objects.nonNull(averageAcquisitionPrice)) {
            systemInstrument.setAverageAcquisitionPriceFromAmount(
                    ExactCurrencyAmount.of(this.averageAcquisitionPrice, this.currency));
        }
        systemInstrument.setCurrency(this.currency);
        systemInstrument.setMarketValue(this.marketValue);
        systemInstrument.setName(this.instrumentIdModule.getName());
        systemInstrument.setPrice(this.price);
        systemInstrument.setQuantity(this.quantity);
        systemInstrument.setProfit(this.profit);
        systemInstrument.setTicker(this.ticker);
        systemInstrument.setType(this.type.toSystemType());
        systemInstrument.setRawType(this.rawType);
        return systemInstrument;
    }

    private InstrumentModule(Builder builder) {
        this.instrumentIdModule = builder.instrumentIdModule;
        this.averageAcquisitionPrice = builder.averageAcquisitionPrice;
        this.currency = builder.currency;
        this.marketValue = builder.marketValue;
        this.price = builder.price;
        this.quantity = builder.quantity;
        this.profit = builder.profit;
        this.ticker = builder.ticker;
        this.type = builder.type;
        this.rawType = builder.rawType;
    }

    public static InstrumentTypeStep<InstrumentBuildStep> builder() {
        return new Builder();
    }

    public InstrumentIdModule getInstrumentIdModule() {
        return instrumentIdModule;
    }

    public BigDecimal getAverageAcquisitionPrice() {
        return averageAcquisitionPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public double getMarketValue() {
        return marketValue;
    }

    public double getPrice() {
        return price;
    }

    public double getQuantity() {
        return quantity;
    }

    public Double getProfit() {
        return profit;
    }

    public String getTicker() {
        return ticker;
    }

    public InstrumentType getType() {
        return type;
    }

    public String getRawType() {
        return rawType;
    }

    private static class Builder
            implements InstrumentBuildStep,
                    InstrumentTypeStep<InstrumentBuildStep>,
                    InstrumentIdStep<InstrumentBuildStep>,
                    MarketPriceStep<InstrumentBuildStep>,
                    CurrencyStep<InstrumentBuildStep>,
                    MarketValueStep<InstrumentBuildStep>,
                    AcquisitionPriceStep<InstrumentBuildStep>,
                    QuantityStep<InstrumentBuildStep>,
                    ProfitStep<InstrumentBuildStep> {
        private InstrumentIdModule instrumentIdModule;
        private BigDecimal averageAcquisitionPrice;
        private String currency;
        private double marketValue;
        private double price;
        private double quantity;
        private Double profit;
        private String ticker;
        private InstrumentType type;
        private String rawType;

        @Override
        public CurrencyStep<InstrumentBuildStep> withAverageAcquisitionPrice(
                Double averageAcquisitionPrice) {
            if (Objects.nonNull(averageAcquisitionPrice)) {
                Preconditions.checkArgument(
                        averageAcquisitionPrice >= 0,
                        "Average Acquisition Price  must not be negative.");
                this.averageAcquisitionPrice = BigDecimal.valueOf(averageAcquisitionPrice);
            }
            return this;
        }

        @Override
        public InstrumentBuildStep setRawType(String rawType) {
            this.rawType = rawType;
            return this;
        }

        @Override
        public InstrumentModule build() {
            return new InstrumentModule(this);
        }

        @Override
        public MarketValueStep<InstrumentBuildStep> withMarketPrice(double marketPrice) {
            this.price = marketPrice;
            return this;
        }

        @Override
        public AcquisitionPriceStep<InstrumentBuildStep> withMarketValue(double marketValue) {
            this.marketValue = marketValue;
            return this;
        }

        @Override
        public InstrumentBuildStep withProfit(Double profit) {
            this.profit = profit;
            return this;
        }

        @Override
        public ProfitStep<InstrumentBuildStep> withQuantity(double quantity) {
            this.quantity = quantity;
            return this;
        }

        @Override
        public InstrumentBuildStep setTicker(String ticker) {
            this.ticker = ticker;
            return this;
        }

        @Override
        public MarketPriceStep<InstrumentBuildStep> withId(
                @Nonnull InstrumentIdModule instrumentIdModule) {
            Preconditions.checkNotNull(instrumentIdModule, "InstrumentIdModule must not be null.");
            this.instrumentIdModule = instrumentIdModule;
            return this;
        }

        @Override
        public InstrumentIdStep<InstrumentBuildStep> withType(@Nonnull InstrumentType type) {
            Preconditions.checkNotNull(type, "InstrumentType must not be null.");
            this.type = type;
            return this;
        }

        @Override
        public QuantityStep<InstrumentBuildStep> withCurrency(@Nonnull String currency) {
            Preconditions.checkNotNull(currency, "Currency must not be null.");
            this.currency = currency;
            return this;
        }
    }

    public enum InstrumentType {
        FUND(Type.FUND),
        STOCK(Type.STOCK),
        OTHER(Type.OTHER);

        private final Type systemType;

        InstrumentType(Type type) {
            this.systemType = type;
        }

        public Type toSystemType() {
            return this.systemType;
        }
    }
}
