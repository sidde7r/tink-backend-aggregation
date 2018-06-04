package se.tink.backend.common.merchants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.core.Merchant;
import se.tink.libraries.uuid.UUIDUtils;

public class MerchantWhiteSpaceTrimmerTest {

    @Test
    public void testTrimFields() throws Exception {

        Merchant m = new Merchant();
        m.setId(UUIDUtils.toTinkUUID(UUID.randomUUID()));
        m.setFormattedAddress(" formatted address ");
        m.setSniCode(" sni code ");
        m.setPhoneNumber(" phone ");
        m.setWebsite(" web site ");
        m.setAddress(" address ");
        m.setCity(" city ");
        m.setCountry(" country ");
        m.setName(" name ");
        m.setOrganizationId(" org id ");

        ArrayList<Merchant> list = new ArrayList<>(Arrays.asList(m));

        MerchantWhiteSpaceTrimmer trimmer = new MerchantWhiteSpaceTrimmer();

        List<Merchant> merchants = trimmer.TrimWhiteSpace(list);

        Merchant result = merchants.get(0);

        Assert.assertEquals(1, merchants.size());
        Assert.assertEquals("formatted address", result.getFormattedAddress());
        Assert.assertEquals("sni code", result.getSniCode());
        Assert.assertEquals("phone", result.getPhoneNumber());
        Assert.assertEquals("web site", result.getWebsite());
        Assert.assertEquals("address", result.getAddress());
        Assert.assertEquals("city", result.getCity());
        Assert.assertEquals("country", result.getCountry());
        Assert.assertEquals("name", result.getName());
        Assert.assertEquals("org id", result.getOrganizationId());
    }
}
