package se.tink.backend.aggregation.agents.utils.authentication.vasco.digipass;

import org.junit.Assert;
import org.junit.Test;

public class DigipassTest {
    @Test
    public void serializationTest() {
        Digipass a = new Digipass();
        a.initializeRegistrationData("foobar");
        String as = a.serialize();

        Digipass b = new Digipass();
        b.deserialize(as);
        String bs = b.serialize();

        Assert.assertEquals("Serialization/Deserialization failed.", as, bs);
    }
}
