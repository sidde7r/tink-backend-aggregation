package se.tink.libraries.jersey.utils.masker;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

public class WhitelistHeaderMaskerTest {

    @Test
    public void ensureNotWhitelistedItemsAreMaskedAndPreservesSize() {

        // given
        WhitelistHeaderMasker masker = new WhitelistHeaderMasker(ImmutableSet.of("Header_A"));
        String notWhitelistedKey = "Header_B";
        List<String> headerValues = ImmutableList.of("Value_A", "Value_B");

        // when
        List<String> maskedValues = masker.mask(notWhitelistedKey, headerValues);

        // then
        Assert.assertEquals(
                "Header list length should be preserved.",
                headerValues.size(),
                maskedValues.size());
        Assert.assertTrue(
                "Should contain only masked entries.", containsOnly(maskedValues, "***Masked***"));
    }

    @Test
    public void ensureWhitelistedItemsAreNotMaskedAnd() {

        // given
        WhitelistHeaderMasker masker = new WhitelistHeaderMasker(ImmutableSet.of("Header_A"));
        String notWhitelistedKey = "Header_A";
        List<String> headerValues = ImmutableList.of("Value_A", "Value_B");

        // when
        List<String> maskedValues = masker.mask(notWhitelistedKey, headerValues);

        // then
        Assert.assertEquals(
                "Header list length should be preserved.",
                headerValues.size(),
                maskedValues.size());
        Assert.assertTrue(
                "Should contain whitelisted values", maskedValues.containsAll(headerValues));
    }

    private boolean containsOnly(List<String> list, String element) {
        Set<String> set = ImmutableSet.copyOf(list);
        return set.size() == 1 && set.contains(element);
    }
}
