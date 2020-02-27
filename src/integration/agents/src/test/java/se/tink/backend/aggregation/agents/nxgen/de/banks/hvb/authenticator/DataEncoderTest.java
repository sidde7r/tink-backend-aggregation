package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenBasedJson;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenDeviceId;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenJsonObject;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator.RegistrationRequest.Payload;

public class DataEncoderTest {

    private DataEncoder tested = new DataEncoder();

    @Test
    public void base64EncodeShouldReturnProperString() {
        // given
        byte[] input = givenJsonObject();

        // when
        String result = tested.base64Encode(input);

        // then
        Assertions.assertThat(result).isEqualTo(givenBasedJson());
    }

    @Test
    public void base64UrlEncodeShouldReturnProperString() {
        // given
        byte[] input = givenJsonObject();

        // when
        String result = tested.base64UrlEncode(input);

        // then
        Assertions.assertThat(result).isEqualTo(givenBasedJson());
    }

    @Test
    public void serializeAndBase64ShouldReturnProperString() {
        // given
        Payload givenObj = Payload.withDefaultValues(givenDeviceId());

        // when
        String result = tested.serializeAndBase64(givenObj);

        // then
        Assertions.assertThat(result).isEqualTo(givenBasedJson());
    }
}
