package se.tink.backend.sms.gateway.gateway.rpc;

import org.junit.Test;
import se.tink.backend.sms.gateways.rpc.SmsRequest;
import static org.assertj.core.api.Assertions.assertThat;

public class SmsRequestTest {

    @Test
    public void testSmsRequestBuilder() {
        SmsRequest request = SmsRequest.builder()
                .sender("sender")
                .to("to")
                .message("this is a message")
                .build();

        assertThat(request.getSender()).isEqualTo("sender");
        assertThat(request.getTo()).isEqualTo("to");
        assertThat(request.getMessage()).isEqualTo("this is a message");
    }
}
