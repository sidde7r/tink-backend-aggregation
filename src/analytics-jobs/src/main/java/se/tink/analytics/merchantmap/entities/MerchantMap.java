package se.tink.analytics.merchantmap.entities;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MerchantMap implements Serializable {

    private static final long serialVersionUID = 5767647919994145503L;

    // Map that stores merchant and a set of users that belongs to that merchant
    private final HashMap<UUID, HashSet<UUID>> merchantUsers;

    // Map that stores extended information about merchants used
    private Map<UUID, Merchant> extendedMerchantInformation;

    public MerchantMap(UUID merchantId, UUID userId) {
        HashSet<UUID> users = new HashSet<>();
        users.add(userId);

        // Default initial size of 16 is probably a bit to high, lower to 4 improve memory utilization
        merchantUsers = new HashMap<>(4);
        merchantUsers.put(merchantId, users);
    }

    public MerchantMap() {
        merchantUsers = new HashMap<>();
    }

    public MerchantMap merge(MerchantMap other) {

        for (Map.Entry<UUID, HashSet<UUID>> entry : other.getMerchantUsers().entrySet()) {
            UUID key = entry.getKey();
            HashSet<UUID> value = entry.getValue();

            if (merchantUsers.containsKey(key)) {
                merchantUsers.get(key).addAll(value);
            } else {
                merchantUsers.put(key, value);
            }
        }

        return this;
    }

    public boolean canResolveMerchant() {
        return getResolvedMerchant() != null;
    }

    /**
     * Logic for finding a merchant is
     * 1) At least 80% of all transactions (distinct by user) must belong to a merchant
     * 2) There must be at least 3 distinct users on the resolved merchant
     *
     * @return UUID of the resolved merchant
     */
    public UUID getResolvedMerchant() {
        UUID bestMerchant = null;
        Integer bestMerchantUserCount = 0;
        Integer totalUsers = 0;

        // Find the merchant with most distinct users
        for (Map.Entry<UUID, HashSet<UUID>> s : merchantUsers.entrySet()) {
            Integer numberOfUsers = s.getValue().size();

            if (numberOfUsers >= bestMerchantUserCount) {
                bestMerchant = s.getKey();
                bestMerchantUserCount = numberOfUsers;
            }

            totalUsers += numberOfUsers;
        }

        // Can not resolve a merchant if we don't have any merchants or if the best merchant
        // has less than 3 users
        if (totalUsers == 0 || bestMerchantUserCount < 3) {
            return null;
        }

        // The best merchant must have a value higher than the threshold of 80 %
        if (bestMerchantUserCount / (double) totalUsers > 0.80) {
            return bestMerchant;
        }

        // We could not make a decision about which merchant that we should choose
        return null;
    }

    public Map<UUID, HashSet<UUID>> getMerchantUsers() {
        return merchantUsers;
    }

    public Set<UUID> getMerchantIds() {
        return merchantUsers.keySet();
    }

    public void setExtendedMerchantInformation(Map<UUID, Merchant> extendedMerchantInformation) {
        this.extendedMerchantInformation = extendedMerchantInformation;
    }

    public String getExtendedInformation() {
        if (extendedMerchantInformation == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        for (Map.Entry<UUID, HashSet<UUID>> entry : merchantUsers.entrySet()) {
            sb.append("{ Key: ");
            sb.append(entry.getKey().toString());
            sb.append(", Users: ");
            sb.append(entry.getValue().size());

            Merchant merchant = extendedMerchantInformation.get(entry.getKey());

            if (merchant != null) {

                sb.append(", Name: ");
                sb.append(merchant.getName() != null ? merchant.getName() : "null");

                sb.append(", Source: ");
                sb.append(merchant.getSource() != null ? merchant.getSource() : "null");

                sb.append(", Root Node: ");
                sb.append(merchant.getParentId() != null ? "no" : "yes");
            }

            sb.append("} ");
        }

        return sb.toString();
    }
}
