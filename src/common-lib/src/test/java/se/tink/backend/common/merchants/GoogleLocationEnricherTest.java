package se.tink.backend.common.merchants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.core.Coordinate;
import se.tink.backend.core.Merchant;
import se.tink.backend.core.MerchantSources;
import se.tink.libraries.uuid.UUIDUtils;

/**
 * Verifies that we get can update coordinates on old merchants
 */
public class GoogleLocationEnricherTest {
    private static final String DEFAULT_LOCALE = "sv_SE";
    private static final String DEFAULT_COUNTRY = "SE";
    
    @Test
    public void shouldNotUpdateMerchantsWithOutAddress() throws Exception {

        Merchant m1 = generateTestMerchant();

        ArrayList<Merchant> list = new ArrayList<>(Arrays.asList(m1));

        List<Merchant> s = new MerchantGoogleLocationEnricher().enrichMerchants(list, DEFAULT_LOCALE, DEFAULT_COUNTRY);

        Assert.assertEquals(0, s.size());
    }

    @Test
    public void shouldNotUpdateMerchantsWithCoordinates() throws Exception {

        Merchant m1 = generateTestMerchant();
        m1.setCoordinates(new Coordinate());

        ArrayList<Merchant> list = new ArrayList<>(Arrays.asList(m1));

        List<Merchant> s = new MerchantGoogleLocationEnricher().enrichMerchants(list, DEFAULT_LOCALE, DEFAULT_COUNTRY);

        Assert.assertEquals(0, s.size());
    }

    @Test
    @Ignore // Integration-test that should be fixed with mocks/stubbing
    public void shouldUpdateMerchantWithCoordinatesIfFound() throws Exception {

        Merchant m1 = generateTestMerchant();
        m1.setFormattedAddress("Kalmarvägen 11, 27456 Abbekås");

        ArrayList<Merchant> list = new ArrayList<>(Arrays.asList(m1));

        List<Merchant> s = new MerchantGoogleLocationEnricher().enrichMerchants(list, DEFAULT_LOCALE, DEFAULT_COUNTRY);

        Assert.assertEquals(1, s.size());
    }

    @Test
    @Ignore // Integration-test that should be fixed with mocks/stubbing
    public void shouldNotUpdateMerchantWithCoordinatesIfNotFound() throws Exception {

        Merchant m1 = generateTestMerchant();
        m1.setFormattedAddress("dsfdsf sdfsdfsdf sdf23424 dsf4");

        ArrayList<Merchant> list = new ArrayList<>(Arrays.asList(m1));

        List<Merchant> s = new MerchantGoogleLocationEnricher().enrichMerchants(list, DEFAULT_LOCALE, DEFAULT_COUNTRY);

        Assert.assertEquals(0, s.size());
    }

    private Merchant generateTestMerchant() {
        Merchant merchant = new Merchant();
        merchant.setId(UUIDUtils.toTinkUUID(UUID.randomUUID()));
        merchant.setName("Testing");
        merchant.setCreated(new Date());
        merchant.setSource(MerchantSources.MANUALLY);

        return merchant;
    }

}


