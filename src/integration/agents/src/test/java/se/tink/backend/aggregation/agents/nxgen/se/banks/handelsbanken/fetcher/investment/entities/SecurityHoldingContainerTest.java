package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.models.Instrument;
import static java.util.Optional.empty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class SecurityHoldingContainerTest {

    private Optional<Instrument> actual;
    private CustodyHoldings holdingDetail;
    private Quantity holdingQuantity;
    private Double quantityValue;
    private SecurityHoldingIdentifier identifier;

    @Before
    public void setUp() throws Exception {
        SecurityIdentifier securityIdentifier = new SecurityIdentifier();
        holdingDetail = new CustodyHoldings();
        holdingDetail.setSecurityIdentifier(securityIdentifier);
        holdingQuantity = new Quantity();
        quantityValue = 1d;
        identifier = new SecurityHoldingIdentifier();
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
    public void withoutSummaryAndIdentifier() {
        identifier = null;

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

        if (identifier != null) {
            securityHoldingContainer.setSecurityIdentifier(identifier);
        }

        if (holdingDetail != null) {
            holdingDetail.setHoldingQuantity(holdingQuantity);
        }
        if (holdingQuantity != null) {
            holdingQuantity.setQuantityFormatted(quantityValue);
        }
        actual = securityHoldingContainer.toInstrument();
    }
}
