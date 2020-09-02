package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenBase64EncodedRegistrationCallPayloadEntity;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenDeviceId;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenJsonRegistrationCallPayloadEntity;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator.RegistrationCall.Payload;

public class DataEncoderTest {

    private DataEncoder tested = new DataEncoder();

    @Test
    public void base64EncodeShouldReturnProperString() {
        // given
        byte[] input = givenJsonRegistrationCallPayloadEntity();

        // when
        String result = tested.base64Encode(input);

        // then
        assertThat(result).isEqualTo(givenBase64EncodedRegistrationCallPayloadEntity());
    }

    @Test
    public void base64UrlEncodeShouldReturnProperString() {
        // given
        byte[] input = givenJsonRegistrationCallPayloadEntity();

        // when
        String result = tested.base64UrlEncode(input);

        // then
        assertThat(result).isEqualTo(givenBase64EncodedRegistrationCallPayloadEntity());
    }

    @Test
    public void serializeAndBase64ShouldReturnProperString() {
        // given
        Payload givenObj = Payload.withDefaultValues(givenDeviceId());

        // when
        String result = tested.serializeAndBase64(givenObj);

        // then
        assertThat(result).isEqualTo(givenBase64EncodedRegistrationCallPayloadEntity());
    }
}
