package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.entities;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.system.rpc.Instrument;
import static java.util.Optional.empty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class SecurityHoldingContainerTest {

    private Optional<Instrument> actual;
    private CustodyHoldings holdingDetail;
    private Quantity holdingQuantity;
    private Double quantityValue;

    @Before
    public void setUp() throws Exception {
        holdingDetail = new CustodyHoldings();
        holdingQuantity = new Quantity();
        quantityValue = 1d;
    }

    @Test
    public void happyFlow() {
        mapToInstrument();

        assertTrue(actual.isPresent());
    }

    @Test
    public void withoutDetails() {
        holdingDetail = null;

        mapToInstrument();

        assertThat(actual, is(empty()));
    }

    @Test
    public void withoutQuantity() {
        holdingQuantity = null;

        mapToInstrument();

        assertThat(actual, is(empty()));
    }

    @Test
    public void withoutQuantityValue() {
        quantityValue = null;

        mapToInstrument();

        assertThat(actual, is(empty()));
    }

    @Test
    public void withQuantityValue0() {
        quantityValue = 0d;

        mapToInstrument();

        assertThat(actual, is(empty()));
    }

    private void mapToInstrument() {
        SecurityHoldingContainer securityHoldingContainer = new SecurityHoldingContainer();
        securityHoldingContainer.setHoldingDetail(holdingDetail);
        if (holdingDetail != null) {
            holdingDetail.setHoldingQuantity(holdingQuantity);
        }
        if (holdingQuantity != null) {
            holdingQuantity.setQuantityFormatted(quantityValue);
        }
        actual = securityHoldingContainer.toInstrument();
    }
}
