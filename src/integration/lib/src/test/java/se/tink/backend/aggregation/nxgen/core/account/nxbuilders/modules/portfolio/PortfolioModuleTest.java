package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule.InstrumentType;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.id.InstrumentIdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule.PortfolioType;

public class PortfolioModuleTest {

    private static final InstrumentModule INSTRUMENT_MODULE =
            InstrumentModule.builder()
                    .withType(InstrumentType.FUND)
                    .withId(InstrumentIdModule.of("SE0378331005", "SE", "name"))
                    .withMarketPrice(5d)
                    .withMarketValue(20d)
                    .withAverageAcquisitionPrice(7d)
                    .withCurrency("SEK")
                    .withQuantity(20d)
                    .withProfit(100d)
                    .build();

    @Test(expected = NullPointerException.class)
    public void missingType() {
        PortfolioModule.builder().withType(null);
    }

    @Test(expected = NullPointerException.class)
    public void missingIdentifier() {
        PortfolioModule.builder().withType(PortfolioType.ISK).withUniqueIdentifier(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyIdentifier() {
        PortfolioModule.builder().withType(PortfolioType.ISK).withUniqueIdentifier("");
    }

    @Test(expected = NullPointerException.class)
    public void missingInstrumentsArray() {
        InstrumentModule[] array = null;
        PortfolioModule.builder()
                .withType(PortfolioType.ISK)
                .withUniqueIdentifier("1123")
                .withCashValue(100d)
                .withTotalProfit(50d)
                .withTotalValue(130d)
                .withInstruments(array);
    }

    @Test(expected = NullPointerException.class)
    public void missingInstrument() {
        List<InstrumentModule> instrumentModules = new ArrayList<>();
        instrumentModules.add(null);
        PortfolioModule.builder()
                .withType(PortfolioType.ISK)
                .withUniqueIdentifier("1123")
                .withCashValue(100d)
                .withTotalProfit(50d)
                .withTotalValue(130d)
                .withInstruments(instrumentModules);
    }

    @Test
    public void buildWithInstrumentArrayTest() {

        PortfolioModule portfolioModule =
                PortfolioModule.builder()
                        .withType(PortfolioType.ISK)
                        .withUniqueIdentifier("1123")
                        .withCashValue(100d)
                        .withTotalProfit(50d)
                        .withTotalValue(130d)
                        .withInstruments(INSTRUMENT_MODULE)
                        .build();

        assertEquals(PortfolioType.ISK, portfolioModule.getType());
        assertEquals("1123", portfolioModule.getUniqueIdentifier());
        assertEquals(100, portfolioModule.getCashValue(), 0);
        assertEquals(50, portfolioModule.getTotalProfit(), 0);
        assertEquals(130, portfolioModule.getTotalValue(), 0);
        assertTrue(portfolioModule.getInstrumentModules() instanceof ImmutableList);
        assertEquals(1, portfolioModule.getInstrumentModules().size());
        assertSame(INSTRUMENT_MODULE, portfolioModule.getInstrumentModules().get(0));
    }

    @Test
    public void buildWithInstrumentListTest() {
        PortfolioModule portfolioModule =
                PortfolioModule.builder()
                        .withType(PortfolioType.ISK)
                        .withUniqueIdentifier("1123")
                        .withCashValue(100d)
                        .withTotalProfit(50d)
                        .withTotalValue(130d)
                        .withInstruments(Lists.newArrayList(INSTRUMENT_MODULE))
                        .build();

        assertEquals(PortfolioType.ISK, portfolioModule.getType());
        assertEquals("1123", portfolioModule.getUniqueIdentifier());
        assertEquals(100, portfolioModule.getCashValue(), 0);
        assertEquals(50, portfolioModule.getTotalProfit(), 0);
        assertEquals(130, portfolioModule.getTotalValue(), 0);
        assertTrue(portfolioModule.getInstrumentModules() instanceof ImmutableList);
        assertEquals(1, portfolioModule.getInstrumentModules().size());
        assertSame(INSTRUMENT_MODULE, portfolioModule.getInstrumentModules().get(0));
    }

    @Test
    public void buildWithRawType() {

        PortfolioModule portfolioModule =
                PortfolioModule.builder()
                        .withType(PortfolioType.ISK)
                        .withUniqueIdentifier("1123")
                        .withCashValue(100d)
                        .withTotalProfit(50d)
                        .withTotalValue(130d)
                        .withInstruments(INSTRUMENT_MODULE)
                        .setRawType("ISK")
                        .build();

        assertEquals(PortfolioType.ISK, portfolioModule.getType());
        assertEquals("1123", portfolioModule.getUniqueIdentifier());
        assertEquals(100, portfolioModule.getCashValue(), 0);
        assertEquals(50, portfolioModule.getTotalProfit(), 0);
        assertEquals(130, portfolioModule.getTotalValue(), 0);
        assertEquals("ISK", portfolioModule.getRawType());
        assertSame(INSTRUMENT_MODULE, portfolioModule.getInstrumentModules().get(0));
    }
}
