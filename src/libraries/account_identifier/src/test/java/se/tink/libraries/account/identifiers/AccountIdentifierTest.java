package se.tink.libraries.account.identifiers;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import org.junit.Test;
import se.tink.libraries.account.AccountIdentifier;
import static org.assertj.core.api.Assertions.assertThat;

public class AccountIdentifierTest {
    @Test
    public void testWithNameFromUrl() throws URISyntaxException, UnsupportedEncodingException {
        String encodedName = URLEncoder.encode("ÅlandName", "UTF8");

        String accountUrl = "se://1200112233?name=" + encodedName;
        AccountIdentifier identifier = AccountIdentifier.create(new URI(accountUrl));

        assertThat(identifier.getName().isPresent()).isTrue();
        assertThat(identifier.getName().get()).isEqualTo("ÅlandName");
    }

    @Test
    public void testUrlWithHostAndPath() throws URISyntaxException {
        String accountUrl = "iban://DEUTDEFF500/AT611904300234573201";
        AccountIdentifier identifier = AccountIdentifier.create(new URI(accountUrl));

        assertThat(identifier.getIdentifier()).isEqualTo("DEUTDEFF500/AT611904300234573201");
        assertThat(identifier.getType()).isEqualTo(AccountIdentifier.Type.IBAN);
        assertThat(identifier.getName().isPresent()).isFalse();
    }

    @Test
    public void testUrlWithHostPathAndName() throws UnsupportedEncodingException, URISyntaxException {
        String encodedName = URLEncoder.encode("Åland Name", "UTF8");
        String accountUrl = "iban://DEUTDEFF500/AT611904300234573201?name=" + encodedName;
        AccountIdentifier identifier = AccountIdentifier.create(new URI(accountUrl));

        assertThat(identifier.getIdentifier()).isEqualTo("DEUTDEFF500/AT611904300234573201");
        assertThat(identifier.getType()).isEqualTo(AccountIdentifier.Type.IBAN);
        assertThat(identifier.getName().isPresent()).isTrue();
        assertThat(identifier.getName().get()).isEqualTo("Åland Name");
    }

    @Test
    public void testWitouthNameFromCreate() {
        AccountIdentifier identifier = AccountIdentifier.create(AccountIdentifier.Type.SE, "1200112233");

        assertThat(identifier.getName().isPresent()).isFalse();
    }

    @Test
    public void testWithEmptyNameFromCreate() {
        AccountIdentifier identifier = AccountIdentifier.create(AccountIdentifier.Type.SE, "1200112233", "");

        assertThat(identifier.getName().isPresent()).isFalse();
    }

    @Test
    public void testWithEmptyNameInUrl() throws URISyntaxException {
        String encodedName = "";

        String accountUrl = "se://1200112233?name=" + encodedName;
        AccountIdentifier identifier = AccountIdentifier.create(new URI(accountUrl));

        assertThat(identifier.getName().isPresent()).isFalse();
    }

    @Test
    public void testToUrlWithHostAndPath() {
        AccountIdentifier identifier =
                AccountIdentifier.create(AccountIdentifier.Type.IBAN, "DEUTDEFF500/AT611904300234573201");

        URI toUri = identifier.toURI();
        assertThat(toUri).isNotNull();
        assertThat(toUri.getQuery()).isNull();
        assertThat(toUri.getHost()).isEqualTo("DEUTDEFF500");
        assertThat(toUri.getPath()).isEqualTo("/AT611904300234573201");
    }

    @Test
    public void testToUrlWithHost() {
        AccountIdentifier identifier =
                AccountIdentifier.create(AccountIdentifier.Type.SE, "1200112233");

        URI toUri = identifier.toURI();
        assertThat(toUri).isNotNull();
        assertThat(toUri.getQuery()).isNull();
        assertThat(toUri.getHost()).isEqualTo("1200112233");
        assertThat(toUri.getPath()).isEmpty();
    }

    @Test
    public void testToUrlWithHostPathAndName() {
        AccountIdentifier identifier =
                AccountIdentifier.create(AccountIdentifier.Type.IBAN, "DEUTDEFF500/AT611904300234573201", "Åland Name");

        URI toUri = identifier.toURI();
        assertThat(toUri).isNotNull();
        assertThat(toUri.getHost()).isEqualTo("DEUTDEFF500");
        assertThat(toUri.getPath()).isEqualTo("/AT611904300234573201");
        assertThat(toUri.getQuery()).isEqualTo("name=Åland+Name");
    }
}
