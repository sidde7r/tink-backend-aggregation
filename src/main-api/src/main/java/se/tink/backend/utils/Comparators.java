package se.tink.backend.utils;

import com.google.common.base.Strings;
import java.util.Comparator;
import java.util.Map;
import se.tink.backend.core.Account;
import se.tink.backend.core.Balance;
import se.tink.backend.core.Credentials;

public class Comparators {
    public static Comparator<Balance> BALANCE_BY_AMOUNT = (b1, b2) -> Double.compare(b1.getAmount(), b2.getAmount());
    
    public static Comparator<Account> accountByFullName(final Map<String, Credentials> credentialsById) {
        return (a1, a2) -> {
            Credentials c1 = credentialsById.get(a1.getCredentialsId());
            Credentials c2 = credentialsById.get(a2.getCredentialsId());

            // FIXME: Use the display name of the provider instead.
            String c1str = (c1 != null) ? c1.getProviderName() : "_";
            String c2str = (c2 != null) ? c2.getProviderName() : "_";

            String a1str = c1str + " " + a1.getName();
            String a2str = c2str + " " + a2.getName();

            if (!Strings.isNullOrEmpty(a1.getAccountNumber())) {
                a1str += " " + a1.getAccountNumber();
            }

            if (!Strings.isNullOrEmpty(a2.getAccountNumber())) {
                a2str += " " + a2.getAccountNumber();
            }

            return a1str.compareToIgnoreCase(a2str);
        };
    }
}
