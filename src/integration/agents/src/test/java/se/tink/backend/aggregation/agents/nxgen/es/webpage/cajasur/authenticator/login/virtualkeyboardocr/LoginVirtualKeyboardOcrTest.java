package se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.login.virtualkeyboardocr;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.imageio.ImageIO;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.CajasurTestConstants;

public class LoginVirtualKeyboardOcrTest {

    @Test
    public void virtualKeyboardValueForNumbersSequenceTest() throws IOException {
        // given
        final String passwordValueComputedByCajasurWebPage = "092809";
        BufferedImage keyboardImage =
                ImageIO.read(
                        new BufferedInputStream(
                                new ByteArrayInputStream(
                                        Files.readAllBytes(
                                                Paths.get(
                                                        CajasurTestConstants.TEST_DATA_PATH,
                                                        "virtual_keyboard_login.gif")))));
        final String password = "937193";
        LoginVirtualKeyboardOcr objectUnderTest = new LoginVirtualKeyboardOcr(keyboardImage);

        // when
        String keyboardedPassword =
                objectUnderTest.getVirtualKeyboardValueForNumbersSequence(password);

        // then
        Assertions.assertThat(keyboardedPassword).isEqualTo(passwordValueComputedByCajasurWebPage);
    }
}
