package se.tink.backend.aggregation.nxgen.controllers.transfer.validators;

import org.junit.Assert;
import org.junit.Test;

public class StructuredMessageValidatorTest {

    @Test
    public void correctElectronicMessage() {
        Assert.assertTrue(StructuredMessageValidator.isValidOgmVcs("010806817183"));
    }

    @Test
    public void correctVisualMessage() {
        Assert.assertTrue(
                StructuredMessageValidator.isValidOgmVcs("+++010/8068/17183+++"));
    }

    @Test
    public void incorrectElectronicMessage() {
        Assert.assertFalse(StructuredMessageValidator.isValidOgmVcs("8011810010"));
    }

    @Test
    public void correctElectronicMessageBiggerThanInt() {
        Assert.assertTrue(StructuredMessageValidator.isValidOgmVcs("801181001047"));
    }

}
