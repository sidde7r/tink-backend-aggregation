package se.tink.analytics.jobs.categorization.entities;

import com.clearspring.analytics.util.Lists;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import se.tink.analytics.entites.CategorizationTransaction;

@SuppressWarnings("serial")
public class TransactionGroup implements Serializable {

    private String description;
    private String market;

    // A map that stores: Category Code => UserId => Count of transactions

    private HashMap<String, HashMap<String, Integer>> map;

    public TransactionGroup(String market, String description, String categoryCode, String userId) {

        this.map = Maps.newHashMap();

        HashMap<String, Integer> userMap = Maps.newHashMap();

        userMap.put(userId, 1);

        this.map.put(categoryCode, userMap);

        this.description = description;
        this.market = market;
    }

    /**
     * Merge two groups by merging the maps together
     */
    public TransactionGroup merge(TransactionGroup other) {

        for (Map.Entry<String, HashMap<String, Integer>> entry : other.getMap().entrySet()) {

            String categoryCode = entry.getKey();
            HashMap<String, Integer> newUsers = entry.getValue();

            if (this.map.containsKey(categoryCode)) {

                HashMap<String, Integer> currentUserIdMap = this.map.get(categoryCode);

                for (Map.Entry<String, Integer> newUserMap : newUsers.entrySet()) {

                    Integer occurrences = currentUserIdMap.get(newUserMap.getKey());

                    Integer sum = newUserMap.getValue() + (occurrences == null ? 0 : occurrences);

                    currentUserIdMap.put(newUserMap.getKey(), sum);
                }

            } else {
                this.map.put(categoryCode, newUsers);
            }
        }

        return this;
    }

    public void filterByMinUsers(int minUsers) {

        List<String> toRemove = Lists.newArrayList();

        for (String categoryCode : map.keySet()) {

            // Count the users and remove those categories that are below the threshold
            if (map.get(categoryCode).size() < minUsers) {
                toRemove.add(categoryCode);
            }
        }

        for (String categoryCode : toRemove) {
            map.remove(categoryCode);
        }
    }

    public void filterByMinOccurrences(int minOccurrences) {

        List<String> toRemove = Lists.newArrayList();

        for (String categoryCode : map.keySet()) {

            // Count the total sum of the occurrences and remove those categories that are below the threshold

            int sum = 0;

            for (int value : map.get(categoryCode).values()) {
                sum += value;
            }

            if (sum < minOccurrences) {
                toRemove.add(categoryCode);
            }
        }

        for (String categoryCode : toRemove) {
            map.remove(categoryCode);
        }
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public HashMap<String, HashMap<String, Integer>> getMap() {
        return map;
    }

    public Set<String> getCategoryCodes() {
        return map.keySet();
    }

    public String getDescription() {
        return description;
    }

    /**
     * Expands the group to a list of transactions
     */
    public List<CategorizationTransaction> toTransactionList() {
        List<CategorizationTransaction> result = Lists.newArrayList();

        for (String categoryCode : map.keySet()) {
            for (String userId : map.get(categoryCode).keySet()) {

                int count = map.get(categoryCode).get(userId);

                for (int i = 0; i < count; i++) {
                    result.add(new CategorizationTransaction(description, categoryCode, market, userId));
                }
            }
        }

        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("description", description)
                .add("market", market)
                .add("map", map)
                .toString();
    }
}
