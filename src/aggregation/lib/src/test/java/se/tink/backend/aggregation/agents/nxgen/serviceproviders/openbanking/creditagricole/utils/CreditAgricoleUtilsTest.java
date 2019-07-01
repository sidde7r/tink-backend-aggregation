package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.utils;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class CreditAgricoleUtilsTest {
    private static final String RANDOM_XML_TAG =
            "<?xml version=\"1.0\"?><tag>12345</tag><name>Tink</name><tag>67890</tag></utilisateurDTO>";
    private static final String EXPECTED_XML_VAL1 = "12345";
    private static final String EXPECTED_XML_VAL2 = "67890";

    @Test
    public void shouldGetStringInTagsFromXMLResponse() {
        List response = CreditAgricoleUtils.getXMLResponse("tag", RANDOM_XML_TAG);

        Assert.assertEquals(response.size(), 2);

        Assert.assertTrue(
                response.contains(EXPECTED_XML_VAL1) && response.contains(EXPECTED_XML_VAL2));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotGetStringInTagsFromXMLResponse() {
        CreditAgricoleUtils.getXMLResponse("noTag", RANDOM_XML_TAG);
    }
}
