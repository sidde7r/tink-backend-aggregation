package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.depot;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.models.Instrument;

public class MT535InstrumentTest {

    private MT535Instrument mt535Instrument;

    private static final String finSeg =
            String.join(
                    System.lineSeparator(),
                    ":16R:FIN",
                    ":35B:ISIN LU0635178014",
                    "/DE/ETF127",
                    "COMS.-MSCI EM.M.T.U.ETF I",
                    ":90B::MRKT//ACTU/EUR38,8",
                    ":94B::PRIC//LMAR/XFRA",
                    ":98A::PRIC//20170428",
                    ":93B::AGGR//UNIT/16,",
                    ":19A::HOLD//EUR620,8",
                    ":70E::HOLD//1STK",
                    "23,968293+EUR",
                    ":16S:FIN");

    @Before
    public void setUp() {
        this.mt535Instrument = new MT535Instrument(finSeg);
    }

    @Test
    public void shouldGetExpectedInstrument() {
        String isin = "LU0635178014";
        String ticker = "ETF127";
        Double acquisitionPrice = 23.968293;
        Double marketPrice = 38.8;
        Double quantity = 16.0;
        Double marketValue = quantity * marketPrice;
        Double profit = marketValue - acquisitionPrice;

        Instrument result = mt535Instrument.toTinkInstrument();
        assertEquals(result.getCurrency(), "EUR");
        assertEquals(result.getIsin(), isin);
        assertEquals(result.getMarketPlace(), "XFRA");
        assertEquals(result.getUniqueIdentifier(), isin + "DE" + ticker);
        assertEquals(result.getAverageAcquisitionPrice(), acquisitionPrice);
        assertEquals(result.getQuantity(), quantity);
        assertEquals(result.getPrice(), marketPrice);
        assertEquals(result.getMarketValue(), marketValue);
        assertEquals(result.getName(), "COMS.-MSCI EM.M.T.U.ETF I");
        assertEquals(result.getProfit(), profit);
        assertEquals(result.getTicker(), ticker);
        assertEquals(result.getType(), Instrument.Type.OTHER);
        assertEquals(result.getRawType(), "ETF");
    }
}
