package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.models.Portfolio.Type;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;

public final class PortfolioModule {
    private final String uniqueIdentifier;
    private final double totalProfit;
    private final double cashValue;
    private final double totalValue;
    private final PortfolioType type;
    private final String rawType;
    private final List<InstrumentModule> instrumentModules;

    private PortfolioModule(Builder builder) {
        this.uniqueIdentifier = builder.uniqueIdentifier;
        this.totalProfit = builder.totalProfit;
        this.cashValue = builder.cashValue;
        this.totalValue = builder.totalValue;
        this.type = builder.type;
        this.rawType = builder.rawType;
        this.instrumentModules = builder.instrumentModules;
    }

    public static PortfolioTypeStep<PortfolioBuildStep> builder() {
        return new Builder();
    }

    public Portfolio toSystemPortfolio() {
        Portfolio systemPortfolio = new Portfolio();
        systemPortfolio.setUniqueIdentifier(this.uniqueIdentifier);
        systemPortfolio.setTotalProfit(this.totalProfit);
        systemPortfolio.setCashValue(this.cashValue);
        systemPortfolio.setTotalValue(this.totalValue);
        systemPortfolio.setType(this.type.toSystemType());
        systemPortfolio.setRawType(this.rawType);
        systemPortfolio.setInstruments(
                this.instrumentModules.stream()
                        .map(InstrumentModule::toSystemInstrument)
                        .collect(Collectors.toList()));
        return systemPortfolio;
    }

    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    public double getTotalProfit() {
        return totalProfit;
    }

    public double getCashValue() {
        return cashValue;
    }

    public double getTotalValue() {
        return totalValue;
    }

    public PortfolioType getType() {
        return type;
    }

    public String getRawType() {
        return rawType;
    }

    List<InstrumentModule> getInstrumentModules() {
        return ImmutableList.copyOf(instrumentModules);
    }

    private static class Builder
            implements PortfolioIdStep<PortfolioBuildStep>,
                    PortfolioTypeStep<PortfolioBuildStep>,
                    TotalProfitStep<PortfolioBuildStep>,
                    TotalValueStep<PortfolioBuildStep>,
                    CashValueStep<PortfolioBuildStep>,
                    WithInstrumentStep<PortfolioBuildStep>,
                    PortfolioBuildStep {

        private List<InstrumentModule> instrumentModules = Lists.newArrayList();
        private PortfolioType type;
        private String uniqueIdentifier;
        private double cashValue;
        private double totalProfit;
        private double totalValue;
        private String rawType;

        @Override
        public PortfolioIdStep<PortfolioBuildStep> withType(@Nonnull PortfolioType type) {
            Preconditions.checkNotNull(type, "PortfolioType must not be null.");
            this.type = type;
            return this;
        }

        @Override
        public CashValueStep<PortfolioBuildStep> withUniqueIdentifier(@Nonnull String identifier) {
            Preconditions.checkNotNull(identifier, "Portfolio Identifier must not be null.");
            Preconditions.checkArgument(
                    !Strings.isNullOrEmpty(identifier), "Portfolio Identifier must not be empty.");
            this.uniqueIdentifier = identifier;
            return this;
        }

        @Override
        public TotalProfitStep<PortfolioBuildStep> withCashValue(double cashValue) {
            this.cashValue = cashValue;
            return this;
        }

        @Override
        public TotalValueStep<PortfolioBuildStep> withTotalProfit(double totalProfit) {
            this.totalProfit = totalProfit;
            return this;
        }

        @Override
        public WithInstrumentStep<PortfolioBuildStep> withTotalValue(double totalValue) {
            this.totalValue = totalValue;
            return this;
        }

        private void addInstrument(@Nonnull InstrumentModule instrumentModule) {
            Preconditions.checkNotNull(instrumentModule, "Instrument must not be null.");
            instrumentModules.add(instrumentModule);
        }

        @Override
        public PortfolioBuildStep withInstruments(@Nonnull InstrumentModule... instrumentModules) {
            Preconditions.checkNotNull(instrumentModules, "Instruments Array must not be null.");
            return withInstruments(Arrays.asList(instrumentModules));
        }

        @Override
        public PortfolioBuildStep withInstruments(
                @Nonnull List<InstrumentModule> instrumentModules) {
            Preconditions.checkNotNull(instrumentModules, "Instruments List must not be null.");
            Preconditions.checkArgument(
                    instrumentModules.size() > 0, "Instruments must not be empty.");
            instrumentModules.forEach(this::addInstrument);
            return this;
        }

        @Override
        public PortfolioBuildStep setRawType(@Nonnull String rawType) {
            this.rawType = rawType;
            return this;
        }

        @Override
        public PortfolioModule build() {
            return new PortfolioModule(this);
        }
    }

    public enum PortfolioType {
        ISK(Type.ISK),
        KF(Type.KF),
        DEPOT(Type.DEPOT),
        PENSION(Type.PENSION),
        OTHER(Type.OTHER);

        Type systemType;

        PortfolioType(Type type) {
            this.systemType = type;
        }

        public Type toSystemType() {
            return this.systemType;
        }
    }
}
