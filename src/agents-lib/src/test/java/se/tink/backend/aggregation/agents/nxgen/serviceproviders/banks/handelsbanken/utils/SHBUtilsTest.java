package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.utils;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class SHBUtilsTest {
    @Test
    public void testCleanHandelsbankenDescription() {
        String inputStringOne = "PRESSBYR$N";
        String inputStringTwo = "L{KARBES@K";
        String inputStringThree = "B@NOR OCH BLAD";
        assertEquals(
                "PRESSBYRÅN", SHBUtils.unescapeAndCleanTransactionDescription(inputStringOne));
        assertEquals(
                "LÄKARBESÖK", SHBUtils.unescapeAndCleanTransactionDescription(inputStringTwo));
        assertEquals(
                "BÖNOR OCH BLAD", SHBUtils.unescapeAndCleanTransactionDescription(inputStringThree));
    }
}
