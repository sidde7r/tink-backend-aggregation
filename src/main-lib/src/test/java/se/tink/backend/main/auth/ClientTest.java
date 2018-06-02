package se.tink.backend.main.auth;

import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.core.Client;
import se.tink.backend.core.ClientMessage;
import static org.assertj.core.api.Assertions.assertThat;

public class ClientTest {

    Client client;

    @Before
    public void setUp() {
        client = new Client();
        ClientMessage cm1 = new ClientMessage();
        ClientMessage cm2 = new ClientMessage();
        cm1.setLocale("nl_NL");
        cm1.setMessage("Some message in dutch");
        cm2.setLocale("en_US");
        cm2.setMessage("Some message in english");
        List<ClientMessage> messages = Lists.newArrayList(cm1, cm2);
        client.setMessages(messages);
    }

    @Test
    public void wrongLocaleShouldReturnEmptyMessage() {
        assertThat(client.getMessage("wrong_Locale").isPresent()).isFalse();
    }

    @Test
    public void correctLocaleShouldReturnMessage() {
        assertThat(client.getMessage("nl_NL")).isNotNull();
        assertThat(client.getMessage("en_US")).isNotNull();
    }

    @Test
    public void nullLocaleShouldReturnEmpty() {
        assertThat(client.getMessage(null).isPresent()).isFalse();
    }

    @Test
    public void correctLocaleShouldReturnCorrectMessage() {
        assertThat(client.getMessage("nl_NL").get()).isEqualTo("Some message in dutch");
    }
}