package se.tink.backend.aggregation.agents.banks.seb;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Account;

public class AccountUtils {
    private static final Pattern SUB_ACCOUNTS =
            Pattern.compile(",?sub_accounts=\\[(?<subAccounts>.*?)\\]");
    private static final Joiner CSV_JOINER = Joiner.on(",");
    private static final Splitter CSV_SPLITTER = Splitter.on(",").trimResults().omitEmptyStrings();

    public static String getBankIdBySubAccount(List<Account> accounts, String subAccount) {
        if (accounts != null) {
            List<Account> accountsWithSubAccounts =
                    accounts.stream()
                            .filter(account -> getSubAccounts(account).contains(subAccount))
                            .collect(Collectors.toList());

            if (accountsWithSubAccounts.isEmpty()) {
                return null;
            }

            if (accountsWithSubAccounts.size() > 1) {
                Optional<Account> optionalAccount =
                        accountsWithSubAccounts.stream()
                                .filter(account -> !account.isExcluded())
                                .filter(account -> !account.isClosed())
                                .findFirst();

                if (!optionalAccount.isPresent()) {
                    optionalAccount =
                            accountsWithSubAccounts.stream()
                                    .filter(account -> !account.isExcluded())
                                    .filter(Account::isClosed)
                                    .findFirst();
                }

                if (!optionalAccount.isPresent()) {
                    optionalAccount =
                            accountsWithSubAccounts.stream()
                                    .filter(Account::isExcluded)
                                    .filter(account -> !account.isClosed())
                                    .findFirst();
                }

                return optionalAccount.isPresent()
                        ? optionalAccount.get().getBankId()
                        : accountsWithSubAccounts.get(0).getBankId();
            }

            return accountsWithSubAccounts.get(0).getBankId();
        }

        return null;
    }

    public static Set<String> getSubAccounts(Account account) {

        Set<String> subAccounts = Sets.newHashSet();
        String payload = account.getPayload();

        if (!Strings.isNullOrEmpty(payload)) {
            Matcher matcher = SUB_ACCOUNTS.matcher(payload);

            if (matcher.find()) {
                String csv = matcher.group("subAccounts");
                if (!Strings.isNullOrEmpty(csv)) {
                    subAccounts = Sets.newHashSet(CSV_SPLITTER.split(csv));
                }
            }
        }

        return subAccounts;
    }

    public static void setSubAccounts(Account account, Set<String> subAccounts) {
        String payload = account.getPayload();

        if (Strings.isNullOrEmpty(payload)) {
            payload = "";
        }

        Matcher matcher = SUB_ACCOUNTS.matcher(payload);
        payload = matcher.replaceAll("");

        if (payload.length() > 0) {
            payload += ",";
        }

        payload += String.format("sub_accounts=[%s]", CSV_JOINER.join(subAccounts));

        account.setPayload(payload);
    }
}
