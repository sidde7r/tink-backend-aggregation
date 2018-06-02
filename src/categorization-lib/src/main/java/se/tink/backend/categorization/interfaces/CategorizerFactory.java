package se.tink.backend.categorization.interfaces;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import se.tink.backend.categorization.lookup.CitiesByMarket;
import se.tink.backend.categorization.rules.LabelIndexCache;
import se.tink.backend.common.config.CategorizationConfiguration;
import se.tink.backend.core.Provider;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;

/**
 * Constructs a {@link Categorizer}.
 */
public interface CategorizerFactory extends Closeable {
    Categorizer build(User user, Provider provider, Collection<Transaction> inStoreTransactions,
            LabelIndexCache labelIndexCache, CitiesByMarket citiesByMarket,
            CategorizationConfiguration categorizationConfiguration);

    @Override
    default void close() throws IOException {
    }
}
