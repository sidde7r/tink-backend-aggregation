package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.id;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public final class InstrumentIdModule {
    private final String uniqueIdentifier;
    private final String isin;
    private final String marketPlace;
    private final String name;

    private InstrumentIdModule(
            String isin, String marketPlace, String name, String uniqueIdentifier) {
        Preconditions.checkNotNull(isin, "isin must not be null.");
        Preconditions.checkArgument(isin.matches("[A-Z]{2}[a-zA-Z0-9]{9}\\d"));
        Preconditions.checkNotNull(marketPlace, "MarketPlace must not be null.");
        Preconditions.checkNotNull(name, "Name must not be null.");
        Preconditions.checkNotNull(uniqueIdentifier, "Instrument identifier must not be null.");
        Preconditions.checkArgument(
                !Strings.isNullOrEmpty(uniqueIdentifier),
                "Instrument identifier must not be empty.");

        this.uniqueIdentifier = uniqueIdentifier;
        this.isin = isin;
        this.marketPlace = marketPlace;
        this.name = name;
    }

    /**
     * Use {@link InstrumentIdModule#of(String, String, String)} to set the uniqueIdentifier to
     * (isin + market)
     *
     * @param isin An International Securities Identification Number (ISIN) uniquely identifies a
     *     security.
     *     <p>ISINs consist of two alphabetic characters, country code, nine alpha-numeric
     *     characters (National Securities Identifying Number, NSIN, which identifies the security,
     *     padded as necessary with leading zeros), and one numerical check digit
     *     <p>Example: SE0378331005
     * @param marketPlace instrument stock market place e.g. 'NASDAQ'
     * @param name instrument name e.g. 'Apple Inc.'
     * @param uniqueIdentifier Normally the uniqueIdentifier should be isin + market. If isin and
     *     market is hard to get hold of and the bank / broker have some other way to identify the
     *     instrument we can use that.
     */
    public static InstrumentIdModule of(
            String isin, String marketPlace, String name, String uniqueIdentifier) {
        return new InstrumentIdModule(isin, marketPlace, name, uniqueIdentifier);
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
    public static InstrumentIdModule of(String isin, String marketPlace, String name) {
        return new InstrumentIdModule(isin, marketPlace, name, isin + marketPlace);
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
}
