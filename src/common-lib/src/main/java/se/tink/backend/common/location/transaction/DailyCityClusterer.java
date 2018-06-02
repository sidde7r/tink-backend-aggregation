package se.tink.backend.common.location.transaction;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import se.tink.backend.core.CategoryTypes;
import se.tink.libraries.date.Period;
import se.tink.backend.core.Transaction;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.backend.utils.guavaimpl.predicates.StringEqualsPredicate;
import se.tink.backend.utils.guavaimpl.predicates.TransactionPredicate;

public class DailyCityClusterer {

    protected Map<String, String> cityByMerchantId;

    public DailyCityClusterer(Map<String, String> cityByMerchantId) {
        this.cityByMerchantId = cityByMerchantId;
    }
    
    public List<DailyCityExistence> transactionsPerDayPerCity(List<Transaction> allUserTransactions, Date target, int daysRadius) {
        Date startDate = DateUtils.setInclusiveStartTime(org.apache.commons.lang.time.DateUtils.addDays(target, -daysRadius));
        Date endDate = DateUtils.inclusiveEndTime(org.apache.commons.lang.time.DateUtils.addDays(target, daysRadius));
        return transactionsPerDayPerCity(allUserTransactions, startDate, endDate);
    }

    public List<DailyCityExistence> transactionsPerDayPerCity(List<Transaction> allUserTransactions, Date startDate, Date endDate) {

        List<Transaction> transactions = filterTransactions(allUserTransactions, startDate, endDate);

        ImmutableListMultimap<String, Transaction> index = Multimaps.index(transactions, new Function<Transaction, String>() {
            @Nullable
            @Override
            public String apply(Transaction t) {
                return ThreadSafeDateFormat.FORMATTER_DAILY.format(t.getDate());
            }
        });

        Multimap<String, Transaction> indexFiltered = Multimaps.filterValues(index,
                t -> cityByMerchantId.containsKey(t.getMerchantId()));

        Multimap <String, String> ci = Multimaps.transformValues(indexFiltered, new Function<Transaction, String>() {
            @Nullable
            @Override
            public String apply(@Nullable Transaction t) {
                return cityByMerchantId.get(t.getMerchantId());
            }
        });

        List<DailyCityExistence> existences = Lists.newArrayList();
        for(String key : ci.keySet()) {
            existences.addAll(createPerDailyCityExistence(key, ci.get(key)));
        }

        return existences;
    }

    private List<Transaction> filterTransactions(List<Transaction> transactions, Date s, Date e) {
        Predicate<Transaction> p = new TransactionPredicate.Builder()
                .withinPeriod(new Period(s, e))
                .setCategoryType(CategoryTypes.EXPENSES)
                .requireHasMerchant().build();

        Iterable<Transaction> filtered = Iterables.filter(transactions, p);
        return Lists.newArrayList(filtered);
    }

    private List<DailyCityExistence> createPerDailyCityExistence(String date, Collection<String> cities) {
        List<String> orderedUnique = Lists.newArrayList(Sets.newHashSet(cities));

        List<DailyCityExistence> existences = Lists.newArrayList();
        for(int i = 0; i < orderedUnique.size(); i++) {
            String city = orderedUnique.get(i);
            int count = countWith(cities, new StringEqualsPredicate(city));
            existences.add(new DailyCityExistence(city, count, date));
        }

        return existences;
    }

    private int countWith(Iterable<String> iterable, Predicate<String> predicate) {
        return Iterables.size(Iterables.filter(iterable, predicate));
    }
}
