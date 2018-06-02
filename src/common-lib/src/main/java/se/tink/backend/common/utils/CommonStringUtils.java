package se.tink.backend.common.utils;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.Set;
import org.apache.lucene.queryparser.classic.QueryParser;
import se.tink.libraries.i18n.Catalog;

public class CommonStringUtils {
    private static final Joiner AND_JOINER = Joiner.on(", ");
    private static final int MAX_MULTIPLE_AND_OBJECTS = 5;
    
    public static String escapeElasticSearchSearchString(String string) {
        return Strings.isNullOrEmpty(string) ? string : QueryParser.escape(string);
    }

    public static String formatAndObjects(Iterable<String> descriptions, Catalog catalog) {
        Iterator<String> descriptionIterator = descriptions.iterator();
        Set<String> included = Sets.newHashSetWithExpectedSize(MAX_MULTIPLE_AND_OBJECTS);
        while (descriptionIterator.hasNext() && included.size() < MAX_MULTIPLE_AND_OBJECTS) {
            included.add(descriptionIterator.next());
        }

        if (included.size() == 1) {
            return included.iterator().next();
        }

        Iterable<String> includedButLast = Iterables.limit(included, included.size() - 1);

        return AND_JOINER.join(includedButLast) + " " + (catalog.getString("and") + " " + Iterables.getLast(included));
    }
    
    public static String formatCredentialsStatusPayloadSuffix(long numberOfAccounts, long numberOfTransactions,
            Catalog catalog) {
        StringBuilder builder = new StringBuilder();

        builder.append(Catalog.format(catalog.getPluralString("{0} account", "{0} accounts", numberOfAccounts),
                numberOfAccounts));

        if (numberOfTransactions > 0) {
            builder.append(" ");
            builder.append(catalog.getString("and"));
            builder.append(" ");

            builder.append(Catalog.format(
                    catalog.getPluralString("{0} transaction", "{0} transactions", numberOfTransactions),
                    numberOfTransactions));
        }

        return builder.toString();
    }
}
