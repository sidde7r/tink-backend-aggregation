package se.tink.backend.rpc;

import org.junit.Test;
import se.tink.libraries.validation.exceptions.InvalidPin6Exception;

public class UpdatePin6CommandTest {
    @Test(expected = InvalidPin6Exception.class)
    public void testSamePin6() throws InvalidPin6Exception {
        UpdatePin6Command.builder()
                .withOldPin6("121212")
                .withNewPin6("121212")
                .build();
    }

    @Test(expected = InvalidPin6Exception.class)
    public void testInvalidPin6() throws InvalidPin6Exception {
        UpdatePin6Command.builder()
                .withOldPin6("A23456")
                .withNewPin6("121212")
                .build();
    }

    @Test
    public void testCorrectBuilder() throws InvalidPin6Exception {
        UpdatePin6Command.builder()
                .withSessionId("sessionId")
                .withOldPin6("121212")
                .withNewPin6("121213")
                .build();
    }
}
