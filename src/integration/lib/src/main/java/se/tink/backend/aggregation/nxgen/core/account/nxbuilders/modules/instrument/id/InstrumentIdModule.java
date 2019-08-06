package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.id;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Objects;
import javax.annotation.Nonnull;

public final class InstrumentIdModule {
    private final String uniqueIdentifier;
    private final String isin;
    private final String marketPlace;
    private final String name;

    private InstrumentIdModule(Builder builder) {

        this.uniqueIdentifier = builder.uniqueIdentifier;
        this.isin = builder.isin;
        this.marketPlace = builder.marketPlace;
        this.name = builder.name;
    }

    public static InstrumentUniqueIdStep<InstrumentIdBuildStep> builder() {
        return new Builder();
    }

    /**
     * Use {@link InstrumentIdModule#of(String, String, String)} to set the uniqueIdentifier to
     * (isin + market)
     *
     * @param isin (nullable) An International Securities Identification Number (ISIN) uniquely
     *     identifies a security.
     *     <p>ISINs consist of two alphabetic characters, country code, nine alpha-numeric
     *     characters (National Securities Identifying Number, NSIN, which identifies the security,
     *     padded as necessary with leading zeros), and one numerical check digit
     *     <p>Example: SE0378331005
     * @param marketPlace (nullable) instrument stock market place e.g. 'NASDAQ'
     * @param name instrument name e.g. 'Apple Inc.'
     * @param uniqueIdentifier Normally the uniqueIdentifier should be isin + market. If isin and
     *     market is hard to get hold of and the bank / broker have some other way to identify the
     *     instrument we can use that.
     */
    public static InstrumentIdModule of(
            String isin,
            String marketPlace,
            @Nonnull String name,
            @Nonnull String uniqueIdentifier) {
        return builder()
                .withUniqueIdentifier(uniqueIdentifier)
                .withName(name)
                .setIsin(isin)
                .setMarketPlace(marketPlace)
                .build();
    }

    /**
     * Sets the uniqueIdentifier to isin + market
     *
     * @param isin An International Securities Identification Number (ISIN) uniquely identifies a
     *     security.
     *     <p>ISINs consist of two alphabetic characters, country code, nine alpha-numeric
     *     characters (National Securities Identifying Number, NSIN, which identifies the security,
     *     padded as necessary with leading zeros), and one numerical check digit
     *     <p>Example: SE0378331005
     * @param marketPlace instrument stock market place e.g. 'NASDAQ'
     * @param name instrument name e.g. 'Apple Inc.'
     */
    public static InstrumentIdModule of(
            @Nonnull String isin, @Nonnull String marketPlace, @Nonnull String name) {
        // marketPlace should not be null because it will be used to generate the unique identifier
        Preconditions.checkNotNull(marketPlace, "MarketPlace must not be null.");
        return of(isin, marketPlace, name, isin + marketPlace);
    }

    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    public String getIsin() {
        return isin;
    }

    public String getMarketPlace() {
        return marketPlace;
    }

    public String getName() {
        return name;
    }

    private static class Builder
            implements InstrumentNameStep<InstrumentIdBuildStep>,
                    InstrumentUniqueIdStep<InstrumentIdBuildStep>,
                    InstrumentIdBuildStep {
        private String uniqueIdentifier;
        private String isin;
        private String marketPlace;
        private String name;

        @Override
        public InstrumentNameStep<InstrumentIdBuildStep> withUniqueIdentifier(
                @Nonnull String uniqueIdentifier) {
            Preconditions.checkNotNull(uniqueIdentifier, "Instrument identifier must not be null.");
            Preconditions.checkArgument(
                    !Strings.isNullOrEmpty(uniqueIdentifier),
                    "Instrument identifier must not be empty.");
            this.uniqueIdentifier = uniqueIdentifier;
            return this;
        }

        @Override
        public InstrumentIdBuildStep withName(@Nonnull String name) {
            Preconditions.checkNotNull(name, "Name must not be null.");
            this.name = name;
            return this;
        }

        @Override
        public InstrumentIdBuildStep setIsin(String isin) {
            Preconditions.checkArgument(
                    Objects.isNull(isin) || isin.matches("[A-Z]{2}[a-zA-Z0-9]{9}\\d"),
                    "Invalid ISIN");
            this.isin = isin;
            return this;
        }

        @Override
        public InstrumentIdBuildStep setMarketPlace(String marketPlace) {
            this.marketPlace = marketPlace;
            return this;
        }

        @Override
        public InstrumentIdModule build() {
            return new InstrumentIdModule(this);
        }
    }
}
