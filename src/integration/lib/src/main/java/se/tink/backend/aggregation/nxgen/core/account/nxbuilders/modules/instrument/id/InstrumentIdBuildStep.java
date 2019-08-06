package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.id;

public interface InstrumentIdBuildStep {
    /** @param marketPlace instrument stock market place e.g. 'NASDAQ' */
    InstrumentIdBuildStep setMarketPlace(String marketPlace);

    /**
     * @param isin An International Securities Identification Number (ISIN) uniquely identifies a
     *     security.
     *     <p>ISINs consist of two alphabetic characters, country code, nine alpha-numeric
     *     characters (National Securities Identifying Number, NSIN, which identifies the security,
     *     padded as necessary with leading zeros), and one numerical check digit
     *     <p>Example: SE0378331005
     */
    InstrumentIdBuildStep setIsin(String isin);

    InstrumentIdModule build();
}
