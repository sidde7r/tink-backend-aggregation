package se.tink.backend.aggregation.nxgen.controllers.transfer.nxgen.model;

import org.junit.Test;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import static org.assertj.core.api.Assertions.assertThat;

public class TransferSourceTest {
    @Test
    public void testWithoutTransferableBuilder() {
        TransferSource source = TransferSource.builder()
                .withIdentifier(new SwedishIdentifier("33001212121214"))
                .isTransferable(false)
                .withKeyValue("foo", "bar")
                .build();

        assertThat(source.getAccountIdentifier().getIdentifier()).isEqualTo("33001212121214");
        assertThat(source.getValueByKey("foo")).isEqualTo("bar");
        assertThat(source.isTransferable()).isFalse();
    }

    @Test
    public void testWithTransferableBuilder() {
        TransferSource source = TransferSource.builder()
                .withIdentifier(new SwedishIdentifier("33001212121214"))
                .isTransferable(true)
                .withKeyValue("foo", "bar")
                .build();

        assertThat(source.getAccountIdentifier().getIdentifier()).isEqualTo("33001212121214");
        assertThat(source.getValueByKey("foo")).isEqualTo("bar");
        assertThat(source.isTransferable()).isTrue();
    }
}
