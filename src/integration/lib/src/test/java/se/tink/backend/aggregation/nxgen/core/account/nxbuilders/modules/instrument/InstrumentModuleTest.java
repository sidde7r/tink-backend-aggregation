package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import org.junit.Test;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule.InstrumentType;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.id.InstrumentIdModule;

public class InstrumentModuleTest {

    private static final InstrumentIdModule INSTRUMENT_ID_MODULE =
            InstrumentIdModule.of("SE0378331005", "SE", "name", "12345");

    @Test(expected = NullPointerException.class)
    public void missingType() {
        InstrumentModule.builder().withType(null);
    }

    @Test(expected = NullPointerException.class)
    public void missingIdModule() {
        InstrumentModule.builder().withType(InstrumentType.FUND).withId(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeAverageAcquisitionPrice() {
        InstrumentModule.builder()
                .withType(InstrumentType.FUND)
                .withId(INSTRUMENT_ID_MODULE)
                .withMarketPrice(5d)
                .withMarketValue(20d)
                .withAverageAcquisitionPrice(-7.7);
    }

    @Test(expected = NullPointerException.class)
    public void missingCurrency() {
        InstrumentModule.builder()
                .withType(InstrumentType.FUND)
                .withId(INSTRUMENT_ID_MODULE)
                .withMarketPrice(5d)
                .withMarketValue(20d)
                .withAverageAcquisitionPrice(7d)
                .withCurrency(null);
    }

    @Test
    public void buildTest() {
        InstrumentModule instrumentModule =
                InstrumentModule.builder()
                        .withType(InstrumentType.FUND)
                        .withId(INSTRUMENT_ID_MODULE)
                        .withMarketPrice(5d)
                        .withMarketValue(20d)
                        .withAverageAcquisitionPrice(7d)
                        .withCurrency("SEK")
                        .withQuantity(20d)
                        .withProfit(100d)
                        .build();

        assertEquals(InstrumentType.FUND, instrumentModule.getType());
        assertSame(INSTRUMENT_ID_MODULE, instrumentModule.getInstrumentIdModule());
        assertEquals(5, instrumentModule.getPrice(), 0);
        assertEquals(20, instrumentModule.getMarketValue(), 0);
        assertTrue(
                instrumentModule.getAverageAcquisitionPrice().compareTo(BigDecimal.valueOf(7))
                        == 0);
        assertEquals("SEK", instrumentModule.getCurrency());
        assertEquals(20, instrumentModule.getQuantity(), 0);
        assertEquals(100, instrumentModule.getProfit(), 0);
        assertNull(instrumentModule.getTicker());
        assertNull(instrumentModule.getRawType());
    }

    @Test
    public void buildWithMissingAapAndProfit() {
        InstrumentModule instrumentModule =
                InstrumentModule.builder()
                        .withType(InstrumentType.FUND)
                        .withId(INSTRUMENT_ID_MODULE)
                        .withMarketPrice(5d)
                        .withMarketValue(20d)
                        .withAverageAcquisitionPrice(null)
                        .withCurrency("SEK")
                        .withQuantity(20d)
                        .withProfit(null)
                        .setTicker("ticker")
                        .build();

        assertEquals(InstrumentType.FUND, instrumentModule.getType());
        assertSame(INSTRUMENT_ID_MODULE, instrumentModule.getInstrumentIdModule());
        assertEquals(5, instrumentModule.getPrice(), 0);
        assertEquals(20, instrumentModule.getMarketValue(), 0);
        assertEquals("SEK", instrumentModule.getCurrency());
        assertEquals(20, instrumentModule.getQuantity(), 0);
        assertEquals("ticker", instrumentModule.getTicker());
        assertNull(instrumentModule.getRawType());
        assertNull(instrumentModule.getAverageAcquisitionPrice());
        assertNull(instrumentModule.getProfit());

        Instrument systemInstrument = instrumentModule.toSystemInstrument();
        assertNull(systemInstrument.getAverageAcquisitionPrice());
        assertNull(systemInstrument.getProfit());
    }

    @Test
    public void buildWithTicker() {
        InstrumentModule instrumentModule =
                InstrumentModule.builder()
                        .withType(InstrumentType.FUND)
                        .withId(INSTRUMENT_ID_MODULE)
                        .withMarketPrice(5d)
                        .withMarketValue(20d)
                        .withAverageAcquisitionPrice(7d)
                        .withCurrency("SEK")
                        .withQuantity(20d)
                        .withProfit(100d)
                        .setTicker("ticker")
                        .build();

        assertEquals(InstrumentType.FUND, instrumentModule.getType());
        assertSame(INSTRUMENT_ID_MODULE, instrumentModule.getInstrumentIdModule());
        assertEquals(5, instrumentModule.getPrice(), 0);
        assertEquals(20, instrumentModule.getMarketValue(), 0);
        assertTrue(
                instrumentModule.getAverageAcquisitionPrice().compareTo(BigDecimal.valueOf(7))
                        == 0);
        assertEquals("SEK", instrumentModule.getCurrency());
        assertEquals(20, instrumentModule.getQuantity(), 0);
        assertEquals(100, instrumentModule.getProfit(), 0);
        assertEquals("ticker", instrumentModule.getTicker());
        assertNull(instrumentModule.getRawType());
    }

    @Test
    public void buildWithRawTypeTest() {
        InstrumentModule instrumentModule =
                InstrumentModule.builder()
                        .withType(InstrumentType.FUND)
                        .withId(INSTRUMENT_ID_MODULE)
                        .withMarketPrice(5d)
                        .withMarketValue(20d)
                        .withAverageAcquisitionPrice(7d)
                        .withCurrency("SEK")
                        .withQuantity(20d)
                        .withProfit(100d)
                        .setTicker("ticker")
                        .setRawType("fund")
                        .build();

        assertEquals(InstrumentType.FUND, instrumentModule.getType());
        assertSame(INSTRUMENT_ID_MODULE, instrumentModule.getInstrumentIdModule());
        assertEquals(5, instrumentModule.getPrice(), 0);
        assertEquals(20, instrumentModule.getMarketValue(), 0);
        assertTrue(
                instrumentModule.getAverageAcquisitionPrice().compareTo(BigDecimal.valueOf(7))
                        == 0);
        assertEquals("SEK", instrumentModule.getCurrency());
        assertEquals(20, instrumentModule.getQuantity(), 0);
        assertEquals(100, instrumentModule.getProfit(), 0);
        assertEquals("ticker", instrumentModule.getTicker());
        assertEquals("fund", instrumentModule.getRawType());
    }

    @Test
    public void toSystemInstrumentTest() {
        InstrumentModule instrumentModule =
                InstrumentModule.builder()
                        .withType(InstrumentType.STOCK)
                        .withId(INSTRUMENT_ID_MODULE)
                        .withMarketPrice(5d)
                        .withMarketValue(20d)
                        .withAverageAcquisitionPrice(7d)
                        .withCurrency("SEK")
                        .withQuantity(20d)
                        .withProfit(100d)
                        .setTicker("ticker")
                        .setRawType("STOCK")
                        .build();

        se.tink.backend.aggregation.agents.models.Instrument systemInstrument =
                instrumentModule.toSystemInstrument();

        assertEquals(
                se.tink.backend.aggregation.agents.models.Instrument.Type.STOCK,
                systemInstrument.getType());
        assertEquals(
                instrumentModule.getInstrumentIdModule().getUniqueIdentifier(),
                systemInstrument.getUniqueIdentifier());
        assertEquals(
                instrumentModule.getInstrumentIdModule().getIsin(), systemInstrument.getIsin());
        assertEquals(
                instrumentModule.getInstrumentIdModule().getName(), systemInstrument.getName());
        assertEquals(
                instrumentModule.getInstrumentIdModule().getMarketPlace(),
                systemInstrument.getMarketPlace());
        assertEquals(instrumentModule.getPrice(), systemInstrument.getPrice(), 0);
        assertEquals(instrumentModule.getMarketValue(), systemInstrument.getMarketValue(), 0);
        assertTrue(
                instrumentModule
                                .getAverageAcquisitionPrice()
                                .compareTo(
                                        BigDecimal.valueOf(
                                                systemInstrument.getAverageAcquisitionPrice()))
                        == 0);
        assertEquals(instrumentModule.getCurrency(), systemInstrument.getCurrency());
        assertEquals(instrumentModule.getQuantity(), systemInstrument.getQuantity(), 0);
        assertEquals(instrumentModule.getProfit(), systemInstrument.getProfit(), 0);
        assertEquals(instrumentModule.getTicker(), systemInstrument.getTicker());
        assertEquals(instrumentModule.getRawType(), systemInstrument.getRawType());
    }
}
