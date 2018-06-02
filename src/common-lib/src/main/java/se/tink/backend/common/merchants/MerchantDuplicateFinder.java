package se.tink.backend.common.merchants;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.List;
import se.tink.backend.core.Merchant;

public class MerchantDuplicateFinder {

    /**
     * Finds and returns a list of duplicate merchants in the input list
     * <p>
     * Note that multiple mappings isn't resolved and can occur, for example
     * A => B
     * B => C
     * A => C
     *
     * @param merchants
     * @return a list of merchants that could be removed and the merchant that they should be replaced with
     */
    public List<MerchantDuplicateResult> findDuplicates(List<Merchant> merchants) {

        List<MerchantDuplicateResult> result = new ArrayList<>();

        for (Merchant m1 : merchants) {
            for (Merchant m2 : merchants) {
                if (isDuplicate(m1, m2)) {

                    final Merchant lowestQualityMerchant = getLowestQualityMerchant(m1, m2);
                    final Merchant highestQualityMerchant = lowestQualityMerchant == m1 ? m2 : m1;

                    Boolean alreadyAdded = Iterables.any(result, input -> input.getDuplicate() == lowestQualityMerchant
                            && input.getReplacedBy() == highestQualityMerchant);

                    if (!alreadyAdded) {
                        MerchantDuplicateResult duplicate = new MerchantDuplicateResult();
                        duplicate.setDuplicate(lowestQualityMerchant);
                        duplicate.setReplacedBy(highestQualityMerchant);

                        result.add(duplicate);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Check if a merchant is considered to be a duplicate of another
     * Logic is that
     * - Different object
     * - Same name
     * - Same address (address not null or empty)
     * - Only check "public visible" merchants
     *
     * @param m1
     * @param m2
     * @return
     */
    private boolean isDuplicate(Merchant m1, Merchant m2) {
        return isDifferentMerchants(m1, m2) && hasAddress(m1) && hasAddress(m2) && hasSameName(m1, m2)
                && hasSameAddress(m1, m2) && isPublicVisibleMerchant(m1) && isPublicVisibleMerchant(m2);
    }

    private Boolean isDifferentMerchants(Merchant m1, Merchant m2) {
        return m1 != m2 || !m1.getId().equalsIgnoreCase(m2.getId());
    }

    private boolean hasAddress(Merchant m1) {
        return !Strings.isNullOrEmpty(m1.getFormattedAddress());
    }

    private boolean hasSameName(Merchant m1, Merchant m2) {
        return m1.getName() != null && m2.getName() != null && m1.getName().equalsIgnoreCase(m2.getName());
    }

    private boolean hasSameAddress(Merchant m1, Merchant m2) {
        return m1.getFormattedAddress() != null && m2.getFormattedAddress() != null && m1.getFormattedAddress()
                .equalsIgnoreCase(m2.getFormattedAddress());
    }

    /**
     * A merchant can be "private" and only visible for specific users.
     *
     * @param m1
     * @return
     */
    private boolean isPublicVisibleMerchant(Merchant m1) {
        List<String> visibleToUsers = m1.getVisibleToUsers();

        return visibleToUsers == null || visibleToUsers.size() == 0;
    }

    /**
     * Calculate the "quality" on a merchant based on the number of fields that are null or empty.
     *
     * @param m1 First Merchant
     * @param m2 Second Merchant
     * @return
     */
    private Merchant getLowestQualityMerchant(Merchant m1, Merchant m2) {

        int m1Count = getNumberOfNullOrEmptyFields(m1);
        int m2Count = getNumberOfNullOrEmptyFields(m2);

        // 1. Takes the merchant with least amount of null fields
        if (m1Count > m2Count) {
            return m1;
        }
        if (m1Count < m2Count) {
            return m2;
        }

        // 2. Takes the oldest merchant
        if (m1.getCreated() != null && m2.getCreated() != null) {
            if (m1.getCreated().getTime() < m2.getCreated().getTime()) {
                return m2;
            }
            if (m1.getCreated().getTime() > m2.getCreated().getTime()) {
                return m1;
            }
        }

        // 3. Takes the lowest id (to make it deterministic)
        if (m1.getId().hashCode() < m2.getId().hashCode()) {
            return m1;
        }

        return m2;

    }

    /**
     * Counts the number of null/empty fields on a merchant
     *
     * @param merchant
     * @return
     */
    private int getNumberOfNullOrEmptyFields(Merchant merchant) {
        int count = 0;

        if (Strings.isNullOrEmpty(merchant.getSniCode())) {
            count++;
        }

        if (Strings.isNullOrEmpty(merchant.getAddress())) {
            count++;
        }

        if (Strings.isNullOrEmpty(merchant.getCategoryId())) {
            count++;
        }

        if (Strings.isNullOrEmpty(merchant.getCity())) {
            count++;
        }

        if (Strings.isNullOrEmpty(merchant.getCountry())) {
            count++;
        }

        if (Strings.isNullOrEmpty(merchant.getOrganizationId())) {
            count++;
        }

        if (Strings.isNullOrEmpty(merchant.getParentId())) {
            count++;
        }

        if (Strings.isNullOrEmpty(merchant.getPostalCode())) {
            count++;
        }

        if (Strings.isNullOrEmpty(merchant.getWebsite())) {
            count++;
        }

        if (Strings.isNullOrEmpty(merchant.getPhoneNumber())) {
            count++;
        }

        if (Strings.isNullOrEmpty(merchant.getReference())) {
            count++;
        }

        if (Strings.isNullOrEmpty(merchant.getPhotoReference())) {
            count++;
        }

        if (merchant.getCoordinates() == null) {
            count++;
        }

        return count;
    }
}
