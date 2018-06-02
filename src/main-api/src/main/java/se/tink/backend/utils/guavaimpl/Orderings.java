package se.tink.backend.utils.guavaimpl;

import com.google.common.collect.ImmutableMap;
import java.util.Comparator;
import se.tink.backend.core.AccountBalance;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationFieldOption;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.core.Field;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.product.ProductArticle;
import se.tink.backend.core.product.ProductPropertyKey;

public class Orderings {
    /**
     * Order according to an explicit order. Similar to
     * {@link com.google.common.collect.Ordering#explicit(Object, Object[])}, but is more forgiving when unrecognized
     * objects are sorted.
     *
     * @param a   the explicit order
     * @param <T> the type of the objects to order. Must correctly implement {@link Object#hashCode()} and
     *            {@link Object#equals(Object)}.
     * @return a comparator that orders according the explicit order, but considers unrecognized objects as less than
     * recognized objects.
     */
    private static <T> Comparator<T> explicitButUnknownFirst(T... a) {
        final ImmutableMap.Builder<T, Integer> builder = ImmutableMap.builder();
        int counter = 0;
        for (T t : a) {
            builder.put(t, counter++);
        }
        ImmutableMap<T, Integer> orderMap = builder.build();
        return Comparator.<T, Integer>comparing(t -> orderMap.getOrDefault(t, -1));
    }

    /**
     * Standard transaction ordering based on date and when the transaction was inserted into the database.
     */
    public static final Comparator<Transaction> TRANSACTION_DATE_ORDERING = Comparator
            .comparing(Transaction::getDate)
            .thenComparing(Transaction::getTimestamp)
            .thenComparing(Transaction::getId);

    /**
     * Sorting on original date. Fallback to `TRANSACTION_DATE_ORDERING` if dates are equal.
     */
    public static final Comparator<Transaction> TRANSACTION_ORIGINAL_DATE_ORDERING = Comparator
            .comparing(Transaction::getOriginalDate)
            .thenComparing(TRANSACTION_DATE_ORDERING);

    public static final Comparator<FraudDetails> FRAUD_DETAILS_DATE = Comparator.nullsLast(Comparator.comparing
            (FraudDetails::getDate, Comparator.nullsLast(Comparator.naturalOrder())));

    public static final Comparator<ApplicationFieldOption> APPLICATION_FIELD_OPTION_BY_LABEL = Comparator
            .comparing(ApplicationFieldOption::getLabel);

    public static final Comparator<Application> APPLICATION_BY_CREATED = Comparator.comparing(Application::getCreated);

    public static final Comparator<AccountBalance> ACCOUNT_BALANCE_HISTORY_ORDERING = Comparator
            .comparing(AccountBalance::getDate).thenComparing(AccountBalance::getInserted);

    public static final Comparator<Credentials> CREDENTIALS_BY_TYPE_AND_ACTIVITY = Comparator
            .comparing(Credentials::getType, explicitButUnknownFirst(CredentialsTypes.MOBILE_BANKID))
            .thenComparing(Credentials::getUpdated, Comparator.nullsFirst(Comparator.naturalOrder()))
            .thenComparing(Credentials::getStatusUpdated, Comparator.nullsFirst(Comparator.naturalOrder()));

    /**
     * CreditSafe credentials are considered larger than other credentials.
     */
    public static final Comparator<Credentials> CREDENTIALS_BY_CREDITSAFE = Comparator
            .comparing(Credentials::getProviderName, explicitButUnknownFirst("creditsafe"));

    public static final Comparator<ProductArticle> PRODUCTS_BY_INTEREST = Comparator
            .comparing(article -> {
                double interest = 0d;

                Object interestRaw = article.getProperty(ProductPropertyKey.INTEREST_RATE);

                if (interestRaw != null) {
                    interest = ((Number) interestRaw).doubleValue();

                    if (article.hasProperty(ProductPropertyKey.INTEREST_RATE_DISCOUNT)) {
                        interest -= ((Number) article.getProperty(ProductPropertyKey.INTEREST_RATE_DISCOUNT))
                                .doubleValue();
                    }
                }

                return interest;
            });

    /**
     * Credentials with (username field == `String username`) are considered larger than other Credentials.
     */
    public static Comparator<Credentials> credentialsByUsername(final String username) {
        return Comparator.comparing(c -> c.getField(Field.Key.USERNAME), explicitButUnknownFirst(username));
    }

}
