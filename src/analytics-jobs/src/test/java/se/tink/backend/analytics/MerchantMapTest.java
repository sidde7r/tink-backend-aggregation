package se.tink.backend.analytics;

import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import org.junit.Test;
import se.tink.analytics.merchantmap.entities.MerchantMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MerchantMapTest {

    /**
     * Verifies that it is possible to merge two maps with different merchants
     */
    @Test
    public void shouldMergeTwoItemsWithDifferentMerchants() {
        UUID merchant1 = UUID.randomUUID();
        UUID merchant2 = UUID.randomUUID();

        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();

        MerchantMap t1 = new MerchantMap(merchant1, user1);
        MerchantMap t2 = new MerchantMap(merchant2, user2);

        t1.merge(t2);

        Map<UUID, HashSet<UUID>> merged = t1.getMerchantUsers();

        // Two merchant rows with one user in each
        assertEquals(2, merged.size());
        assertEquals(1, merged.get(merchant1).size());
        assertEquals(1, merged.get(merchant2).size());
        assertEquals(user1, merged.get(merchant1).iterator().next());
        assertEquals(user2, merged.get(merchant2).iterator().next());
    }

    /**
     * Verifies that it is possible to merge two maps with the same users
     */
    @Test
    public void shouldMergeTwoItemsWithSameMerchant() {
        UUID merchant1 = UUID.randomUUID();

        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();

        MerchantMap t1 = new MerchantMap(merchant1, user1);
        MerchantMap t2 = new MerchantMap(merchant1, user2);

        t1.merge(t2);

        Map<UUID, HashSet<UUID>> merged = t1.getMerchantUsers();

        // One merchant row with two users
        assertEquals(1, merged.size());
        assertEquals(2, merged.get(merchant1).size());

        assertTrue(merged.get(merchant1).contains(user1));
        assertTrue(merged.get(merchant1).contains(user2));
    }

    /**
     * Verifies that it is possible to merge a map with the same merchant and user
     */
    @Test
    public void shouldMergeTwoItemsWithSameMerchantAndUser() {
        UUID merchant1 = UUID.randomUUID();
        UUID user1 = UUID.randomUUID();

        MerchantMap t1 = new MerchantMap(merchant1, user1);
        MerchantMap t2 = new MerchantMap(merchant1, user1);

        t1.merge(t2);

        Map<UUID, HashSet<UUID>> merged = t1.getMerchantUsers();

        // One merchant with one user
        assertEquals(1, merged.size());
        assertEquals(1, merged.get(merchant1).size());

        assertTrue(merged.get(merchant1).contains(user1));
    }

    /**
     * Verifies that a merchant should be resolved when a merchant has more than 80 % of the total
     * (distinct) users
     */
    @Test
    public void shouldResolveMerchantWithMoreThan80PercentUsers() {
        UUID merchant1 = UUID.randomUUID();
        UUID merchant2 = UUID.randomUUID();

        MerchantMap t1 = new MerchantMap();

        Map<UUID, HashSet<UUID>> items = t1.getMerchantUsers();

        items.put(merchant1, new HashSet<UUID>());
        items.put(merchant2, new HashSet<UUID>());

        for (int i = 0; i < 81; i++) {
            items.get(merchant1).add(UUID.randomUUID());
        }

        for (int i = 0; i < 20; i++) {
            items.get(merchant2).add(UUID.randomUUID());
        }

        assertTrue(t1.canResolveMerchant());
        assertEquals(merchant1, t1.getResolvedMerchant());

        System.out.println(t1.toString());
    }

    /**
     * Verifies that a merchant shouldn't be resolvable if it has below 80% of the total users
     */
    @Test
    public void shouldNotResolveMerchantWithLessThan80PercentUsers() {
        UUID merchant1 = UUID.randomUUID();
        UUID merchant2 = UUID.randomUUID();

        MerchantMap t1 = new MerchantMap();

        Map<UUID, HashSet<UUID>> items = t1.getMerchantUsers();

        items.put(merchant1, new HashSet<UUID>());
        items.put(merchant2, new HashSet<UUID>());

        for (int i = 0; i < 79; i++) {
            items.get(merchant1).add(UUID.randomUUID());
        }

        for (int i = 0; i < 20; i++) {
            items.get(merchant2).add(UUID.randomUUID());
        }

        assertFalse(t1.canResolveMerchant());
        assertNull(t1.getResolvedMerchant());
    }

    /**
     * Verifies that a merchant shouldn't be resolvable if it has less than 3 distinct users
     */
    @Test
    public void shouldNotResolveMerchantWithLess3Users() {
        UUID merchant1 = UUID.randomUUID();

        MerchantMap t1 = new MerchantMap();

        Map<UUID, HashSet<UUID>> items = t1.getMerchantUsers();

        items.put(merchant1, new HashSet<UUID>());

        items.get(merchant1).add(UUID.randomUUID());
        items.get(merchant1).add(UUID.randomUUID());

        assertFalse(t1.canResolveMerchant());
        assertNull(t1.getResolvedMerchant());
    }
}
