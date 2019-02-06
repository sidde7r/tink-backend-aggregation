package se.tink.backend.aggregation.nxgen.controllers.transfer.nxgen.model;

import org.junit.Test;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import static org.assertj.core.api.Assertions.assertThat;

public class OutboxItemTest {
    @Test
    public void testCorrectBuilder() {
        OutboxItem item = OutboxItem.builder()
                .withSource(TransferSource.builder()
                        .withIdentifier(new SwedishIdentifier("33001212121212"))
                        .build())
                .withDestination(TransferDestination.builder()
                        .withIdentifier(new SwedishIdentifier("33001212121213"))
                        .build())
                .withAmount(Amount.inSEK(1000))
                .build();

        assertThat(item.getAmount().getCurrency()).isEqualTo("SEK");
        assertThat(item.getAmount().getValue()).isEqualTo(1000);
        assertThat(item.getSource().getAccountIdentifier().getIdentifier()).isEqualTo("33001212121212");
        assertThat(item.getDestination().getAccountIdentifier().getIdentifier()).isEqualTo("33001212121213");
    }
}
