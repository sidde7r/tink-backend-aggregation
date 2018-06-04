package se.tink.backend.common.merchants;

import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Assert;
import org.junit.Test;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.core.Merchant;

/**
 * Verifies duplicate merchant logic
 */
public class MerchantDuplicateFinderTest {

    @Test
    public void differentNamesShouldNotBeDuplicates() throws Exception {

        // Merchant 1
        Merchant m1 = getDefaultMerchant();
        m1.setName("Bosses Glassbar");

        // Merchant 2
        Merchant m2 = getDefaultMerchant();
        m2.setName("Stefans Glassbar");

        ArrayList<Merchant> list = new ArrayList<>(Arrays.asList(m1, m2));

        MerchantDuplicateFinder duplicateFinder = new MerchantDuplicateFinder();

        List<MerchantDuplicateResult> duplicates = duplicateFinder.findDuplicates(list);

        Assert.assertEquals(0, duplicates.size());
    }

    @Test
    public void sameNameAndNullAddressShouldNotBeDuplicates() throws Exception {

        Merchant m1 = getDefaultMerchant();
        m1.setName("Bosses Glassbar");
        m1.setFormattedAddress(null);

        Merchant m2 = getDefaultMerchant();
        m2.setName("Bosses Glassbar");
        m2.setFormattedAddress(null);

        ArrayList<Merchant> list = new ArrayList<>(Arrays.asList(m1, m2));

        MerchantDuplicateFinder duplicateFinder = new MerchantDuplicateFinder();

        List<MerchantDuplicateResult> duplicates = duplicateFinder.findDuplicates(list);

        Assert.assertEquals(0, duplicates.size());
    }

    @Test
    public void sameNameAndEmptyAddressShouldNotBeDuplicates() throws Exception {

        Merchant m1 = getDefaultMerchant();
        m1.setName("Bosses Glassbar");
        m1.setFormattedAddress("");

        Merchant m2 = getDefaultMerchant();
        m2.setName("Bosses Glassbar");
        m2.setFormattedAddress("");

        ArrayList<Merchant> list = new ArrayList<>(Arrays.asList(m1, m2));

        MerchantDuplicateFinder duplicateFinder = new MerchantDuplicateFinder();

        List<MerchantDuplicateResult> duplicates = duplicateFinder.findDuplicates(list);

        Assert.assertEquals(0, duplicates.size());
    }

    @Test
    public void sameNameAndAddressShouldBeDuplicates() throws Exception {

        // High Quality Merchant
        Merchant high = getDefaultMerchant();
        high.setName("Bosses Glassbar");
        high.setFormattedAddress("Pippi Långstrumps gata 30");
        high.setPhoneNumber("013-11111111");

        // Low Quality Merchant
        Merchant low = getDefaultMerchant();
        low.setName("Bosses Glassbar");
        low.setFormattedAddress("Pippi Långstrumps gata 30");

        ArrayList<Merchant> list = new ArrayList<>(Arrays.asList(high, low));

        MerchantDuplicateFinder duplicateFinder = new MerchantDuplicateFinder();

        List<MerchantDuplicateResult> duplicates = duplicateFinder.findDuplicates(list);

        Assert.assertEquals(1, duplicates.size());

        Assert.assertEquals(low.getId(), duplicates.get(0).getDuplicate().getId());
        Assert.assertEquals(high.getId(), duplicates.get(0).getReplacedBy().getId());
    }

    @Test
    public void sameNameAndAddressShouldBeSortedOnDateDuplicates() throws Exception {

        // High Quality Merchant
        Merchant m1 = getDefaultMerchant();
        m1.setName("Bosses Glassbar");
        m1.setFormattedAddress("Pippi Långstrumps gata 30");
        m1.setCreated(DateUtils.addDays(new Date(), -10));

        // Low Quality Merchant
        Merchant m2 = getDefaultMerchant();
        m2.setName("Bosses Glassbar");
        m2.setFormattedAddress("Pippi Långstrumps gata 30");
        m2.setCreated(new Date());

        ArrayList<Merchant> list = new ArrayList<>(Arrays.asList(m1, m2));

        MerchantDuplicateFinder duplicateFinder = new MerchantDuplicateFinder();

        List<MerchantDuplicateResult> duplicates = duplicateFinder.findDuplicates(list);

        Assert.assertEquals(1, duplicates.size());

        Assert.assertEquals(m2.getId(), duplicates.get(0).getDuplicate().getId());
        Assert.assertEquals(m1.getId(), duplicates.get(0).getReplacedBy().getId());
    }

    @Test
    public void sameNameAndAddressForThreeMerchantsShouldBeDuplicates() throws Exception {

        // Highest Quality Merchant
        final Merchant high = getDefaultMerchant();
        high.setName("Bosses Glassbar");
        high.setFormattedAddress("Pippi Långstrumps gata 30");
        high.setPhoneNumber("013-11111111");
        high.setSniCode("SNI");

        // Low Quality Merchant
        final Merchant low = getDefaultMerchant();
        low.setName("Bosses Glassbar");
        low.setFormattedAddress("Pippi Långstrumps gata 30");
        low.setPhoneNumber("013-11111111");

        // Lowest Quality Merchant
        final Merchant lowest = getDefaultMerchant();
        lowest.setName("Bosses Glassbar");
        lowest.setFormattedAddress("Pippi Långstrumps gata 30");

        ArrayList<Merchant> list = new ArrayList<>(Arrays.asList(high, low, lowest));

        MerchantDuplicateFinder duplicateFinder = new MerchantDuplicateFinder();

        List<MerchantDuplicateResult> duplicates = duplicateFinder.findDuplicates(list);

        Assert.assertEquals(3, duplicates.size());

        // Verify that we have the different combinations

        Assert.assertTrue(Iterables.any(duplicates,
                input -> input.getDuplicate().getId().equals(low.getId()) && input.getReplacedBy().getId()
                        .equals(high.getId())));

        Assert.assertTrue(Iterables.any(duplicates,
                input -> input.getDuplicate().getId().equals(lowest.getId()) && input.getReplacedBy().getId()
                        .equals(high.getId())));

        Assert.assertTrue(Iterables.any(duplicates,
                input -> input.getDuplicate().getId().equals(lowest.getId()) && input.getReplacedBy().getId()
                        .equals(low.getId())));

    }

    private Merchant getDefaultMerchant() {
        Merchant m = new Merchant();
        m.setId(UUIDUtils.toTinkUUID(UUID.randomUUID()));
        m.setCreated(new Date());

        return m;
    }

}
