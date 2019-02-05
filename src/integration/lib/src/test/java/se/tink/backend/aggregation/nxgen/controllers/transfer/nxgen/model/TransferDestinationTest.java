package se.tink.backend.aggregation.nxgen.controllers.transfer.nxgen.model;

import org.junit.Test;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import static org.assertj.core.api.Assertions.assertThat;

public class TransferDestinationTest {
    @Test
    public void testCorrectBuilder() {
        TransferDestination destination = TransferDestination.builder()
                .withIdentifier(new SwedishIdentifier("33001212121213"))
                .withKeyValue("foo", "bar")
                .build();

        assertThat(destination.getAccountIdentifier().getIdentifier()).isEqualTo("33001212121213");
        assertThat(destination.getValueByKey("foo")).isEqualTo("bar");
    }
}
