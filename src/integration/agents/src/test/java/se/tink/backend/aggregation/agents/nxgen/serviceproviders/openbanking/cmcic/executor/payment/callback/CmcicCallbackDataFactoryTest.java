package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.executor.payment.callback;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class CmcicCallbackDataFactoryTest {

    @Test
    public void shouldDecodeURLEncodedCallbackData() {

        // state=40e1e8c3-2583-447c-9773-68a221a3feed
        // &error=invalid_request
        // &error_description=Compte+%C3%A0+cr%C3%A9diter+identique+au+compte+%C3%A0+d%C3%A9biter
        // &error_uri=

        // given:
        Map<String, String> callbackData = new HashMap<>();
        callbackData.put("state", "40e1e8c3-2583-447c-9773-68a221a3feed");
        callbackData.put("error", "invalid_request");
        callbackData.put(
                "error_description",
                "Compte+%C3%A0+cr%C3%A9diter+identique+au+compte+%C3%A0+d%C3%A9biter");
        callbackData.put("error_uri", "");
        CmcicCallbackDataFactory cmcicCallbackDataFactory = new CmcicCallbackDataFactory();

        // when:
        CmcicCallbackData cmcicCallbackData =
                cmcicCallbackDataFactory.fromCallbackData(callbackData);

        // then:
        String error_description =
                cmcicCallbackData.getExpectedCallbackData().get("error_description");
        assertThat(error_description).isEqualTo("Compte à créditer identique au compte à débiter");
    }

    @Test
    public void shouldProvideExpectedAndUnexpectedCallbackData() {

        // given:
        Map<String, String> callbackData = new HashMap<>();
        callbackData.put("state", "stateValue");
        callbackData.put("error", "errorValue");
        callbackData.put("error_description", "errorDescriptionValue");
        callbackData.put("error_uri", "unexpected");
        CmcicCallbackDataFactory cmcicCallbackDataFactory = new CmcicCallbackDataFactory();

        // when:
        CmcicCallbackData cmcicCallbackData =
                cmcicCallbackDataFactory.fromCallbackData(callbackData);

        // then:
        Map<String, String> expectedCallbackData = cmcicCallbackData.getExpectedCallbackData();
        assertThat(expectedCallbackData.size()).isEqualTo(3);
        assertThat(expectedCallbackData.get("state")).isEqualTo("stateValue");
        assertThat(expectedCallbackData.get("error")).isEqualTo("errorValue");
        assertThat(expectedCallbackData.get("error_description"))
                .isEqualTo("errorDescriptionValue");
        assertThat(expectedCallbackData.get("error_uri")).isNull();

        Map<String, String> unexpectedCallbackData = cmcicCallbackData.getUnexpectedCallbackData();
        assertThat(unexpectedCallbackData.size()).isEqualTo(1);
        assertThat(unexpectedCallbackData.get("state")).isNull();
        assertThat(unexpectedCallbackData.get("error")).isNull();
        assertThat(unexpectedCallbackData.get("error_description")).isNull();
        assertThat(unexpectedCallbackData.get("error_uri")).isEqualTo("unexpected");
    }
}
