package se.tink.backend.rpc.properties;

import org.junit.Test;
import se.tink.libraries.validation.exceptions.InvalidPin6Exception;
import static org.assertj.core.api.Assertions.assertThat;

public class UpdatePropertyCommandTest {
    @Test
    public void testCorrectConstruction() throws InvalidPin6Exception {
        UpdatePropertyCommand command = UpdatePropertyCommand.builder()
                .withUser("userId")
                .withPropertyId("propertyId")
                .withNumberOfRooms(10)
                .withNumberOfSquareMeters(100)
                .build();

        assertThat(command.getUserId()).isEqualTo("userId");
        assertThat(command.getPropertyId()).isEqualTo("propertyId");
        assertThat(command.getNumberOfRooms()).isEqualTo(10);
        assertThat(command.getNumberOfSquareMeters()).isEqualTo(100);
    }

    @Test(expected = IllegalStateException.class)
    public void testMissingUserId() throws InvalidPin6Exception {
        UpdatePropertyCommand.builder()
                .withPropertyId("propertyId")
                .withNumberOfRooms(10)
                .withNumberOfSquareMeters(100)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void testMissingPropertyId() throws InvalidPin6Exception {
        UpdatePropertyCommand.builder()
                .withUser("userId")
                .withNumberOfRooms(10)
                .withNumberOfSquareMeters(100)
                .build();
    }
}
