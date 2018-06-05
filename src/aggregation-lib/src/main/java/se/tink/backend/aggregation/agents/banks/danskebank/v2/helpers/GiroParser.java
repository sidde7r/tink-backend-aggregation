package se.tink.backend.aggregation.agents.banks.danskebank.v2.helpers;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.giro.validation.LuhnCheck;
import se.tink.libraries.account.AccountIdentifierPredicate;

public class GiroParser {
    // 1-2, 12-3, 123-4 ... 1234567-8
    private static final String PG_WITH_DASH_PATTERN = "^\\d{1,8}-\\d$";
    // 123-4567 or 1234-5678
    private static final String BG_WITH_DASH_PATTERN = "^\\d{3,4}-\\d{4}$";

    // 1[-?]2, 12[-?]3, 123[-?]4 ... 1234567[-?]8 or 123[-?]4567 or 1234[-?]5678
    private static final String BG_OR_PG_WITH_OPTIONAL_DASH_PATTERN = "^(\\d{1,8}-?\\d)|(\\d{3,4}-?\\d{4})$";

    public static Map<AccountIdentifier.Type, AccountIdentifier> createPossibleIdentifiersFor(String accountNumber) {
        String cleanedAccountNumber = getCleanedAccountNumber(accountNumber);

        if (!isValidAccountNumber(cleanedAccountNumber)) {
            return ImmutableMap.of();
        }

        ConcurrentMap<AccountIdentifier.Type, AccountIdentifier> validIdentifiers = Maps.newConcurrentMap();

        validIdentifiers.putAll(FluentIterable
                .from(getPossibleTypes(cleanedAccountNumber))
                .transform(toIdentifier(cleanedAccountNumber))
                .filter(AccountIdentifierPredicate.IS_VALID)
                .uniqueIndex(TO_TYPE));

        return validIdentifiers;
    }

    private static boolean isValidAccountNumber(String cleanedAccountNumber) {
        if (!cleanedAccountNumber.matches(BG_OR_PG_WITH_OPTIONAL_DASH_PATTERN)) {
            return false;
        }

        // Check that it's numeric (except for possible dash) and that we have valid luhn digit on the account number
        String cleanedAccountNumberWithoutDashes = cleanedAccountNumber.replace("-", "");

        return LuhnCheck.isLastCharCorrectLuhnMod10Check(cleanedAccountNumberWithoutDashes);
    }

    private static String getCleanedAccountNumber(String accountNumber) {
        return accountNumber.trim().replace(" ", "");
    }

    private static List<AccountIdentifier.Type> getPossibleTypes(String cleanedAccountNumber) {
        if (cleanedAccountNumber.contains("-")) {
            if (cleanedAccountNumber.matches(PG_WITH_DASH_PATTERN)) {
                return Lists.newArrayList(AccountIdentifier.Type.SE_PG);
            } else if (cleanedAccountNumber.matches(BG_WITH_DASH_PATTERN)) {
                return Lists.newArrayList(AccountIdentifier.Type.SE_BG);
            } else {
                return Lists.newArrayList();
            }
        } else {
            return Lists.newArrayList(AccountIdentifier.Type.SE_BG, AccountIdentifier.Type.SE_PG);
        }
    }

    private static Function<AccountIdentifier.Type, AccountIdentifier> toIdentifier(final String accountNumber) {
        return type -> AccountIdentifier.create(type, accountNumber);
    }

    private static Function<AccountIdentifier, AccountIdentifier.Type> TO_TYPE =
            AccountIdentifier::getType;
}
