package se.tink.libraries.account.identifiers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;

import java.net.URI;
import org.junit.Assert;
import org.junit.Test;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.formatters.DisplayAccountIdentifierFormatter;

public class IbanIdentifierTest {

    @Test
    public void testValidIbanAndBic() {

        IbanIdentifier id = new IbanIdentifier("DEUTDEFF500", "AT611904300234573201");

        Assert.assertTrue(id.isValid());
    }

    @Test
    public void testInvalidCountryCode() {

        IbanIdentifier id = new IbanIdentifier("DEUTDEFF500", "QQ8937040044053201300011");

        Assert.assertFalse(id.isValidIban());
    }

    @Test
    public void testInvalidFormat() {

        IbanIdentifier id = new IbanIdentifier("DEUTDEFF500", "QQ893704004405320130001111");

        Assert.assertFalse(id.isValidIban());
    }

    @Test
    public void testInvalidBic() {

        IbanIdentifier id = new IbanIdentifier("DEUTDEFF5001212", "QQ8937040044053201300011");

        Assert.assertFalse(id.isValidBic());
    }

    @Test
    public void testParseIbanAndBic() {

        IbanIdentifier id = new IbanIdentifier("DEUTDEFF500/AT611904300234573201");

        Assert.assertTrue(id.isValid());
    }

    @Test
    public void testValidUri() {

        String number = "DEUTDEFF500/AT611904300234573201";

        URI uri = new IbanIdentifier(number).toURI();

        Assert.assertNotNull(uri);
        Assert.assertEquals("iban://" + number, uri.toString());
    }

    @Test
    public void testInvalidIban() {

        String number = "DEUTDE/34573201";

        AccountIdentifier id = new IbanIdentifier(number);

        Assert.assertNotNull(id);
        Assert.assertFalse(id.isValid());
    }

    @Test
    public void testInvalidUri() {

        AccountIdentifier id = AccountIdentifier.create(URI.create("iban://123123"));

        Assert.assertNotNull(id);
        Assert.assertFalse(id.isValid());
    }

    @Test
    public void testDisplayFormat() {
        IbanIdentifier identifier = new IbanIdentifier("NDEAFIHH", "FI2112345600000785");

        String displayName = identifier.getIdentifier(new DisplayAccountIdentifierFormatter());

        assertThat(displayName).isEqualTo("FI21 1234 5600 0007 85");
    }

    @Test
    public void testIsGiroIdentifierShouldBeFalse() {

        IbanIdentifier identifier = new IbanIdentifier("DEUTDEFF500/AT611904300234573201");
        assertFalse("IbanIdentifier is not a GiroIdentifier", identifier.isGiroIdentifier());
    }
}
