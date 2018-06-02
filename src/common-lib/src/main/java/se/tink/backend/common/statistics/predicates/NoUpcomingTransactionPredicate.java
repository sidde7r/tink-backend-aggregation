package se.tink.backend.common.statistics.predicates;

import com.google.common.base.Predicate;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Transaction;
import se.tink.libraries.date.DateUtils;

public class NoUpcomingTransactionPredicate implements Predicate<Transaction> {
    private final List<Credentials> userCredentials;
    private final Date lastUpdatedCredential;

    private final static Comparator<Credentials> dateComparator = (x, y) -> x.getUpdated().compareTo(y.getUpdated());

    public NoUpcomingTransactionPredicate(List<Credentials> credentials) {
        userCredentials = credentials;
        lastUpdatedCredential = lastUpdated();
    }

    private Date lastUpdated() {
        Date today = DateUtils.inclusiveEndTime(DateUtils.getToday());
        List<Credentials> filtered = userCredentials.stream()
                .filter(c -> c.getUpdated() != null)
                .collect(Collectors.toList());
        if (filtered.isEmpty()) {
            return today;
        }
        Date credentialUpdated = filtered.stream().max(dateComparator).get().getUpdated();
        return credentialUpdated.before(today) ? today : credentialUpdated;
    }

    @Override
    public boolean apply(Transaction t) {
       return t.getDate().before(lastUpdatedCredential);
    }
}
