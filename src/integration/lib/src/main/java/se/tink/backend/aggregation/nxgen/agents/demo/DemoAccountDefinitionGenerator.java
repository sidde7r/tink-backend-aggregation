package se.tink.backend.aggregation.nxgen.agents.demo;

import static se.tink.backend.aggregation.nxgen.agents.demo.DemoConstants.Uksortcodes;

import com.google.common.collect.Lists;
import java.util.List;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoSavingsAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoTransactionAccount;
import se.tink.libraries.account.AccountIdentifier;

/** Deterministic account generator based on user-name and provider */
public class DemoAccountDefinitionGenerator {

    public static String ukProviderRegex = "^uk-.*";

    private static String createDeterministicKey(String combination) {
        return Integer.toString(combination.hashCode());
    }

    private static int generateNumber(String deterministicKey, int digits) {
        if (digits <= 0 || digits > 7) {
            return 0;
        }

        char[] deterministicKeyList = deterministicKey.toCharArray();
        int uniqueAccountInfo = digits;
        int index = 0;

        while (String.valueOf(uniqueAccountInfo).length() <= digits) {
            uniqueAccountInfo *= deterministicKeyList[index++];
            index = index % deterministicKey.length();
        }

        return Integer.parseInt(("" + uniqueAccountInfo).substring(0, digits));
    }

    private static double generateDouble(String deterministicKey, int digits) {
        return (double) (generateNumber(deterministicKey, digits)) / 100;
    }

    private static String generateAccoutNumbers(String deterministicKey) {
        return ("" + generateNumber(deterministicKey, 4))
                + "-"
                + ("" + generateNumber(deterministicKey, 5) + generateNumber(deterministicKey, 7));
    }

    private static String generateAccoutNumbersUK(
            String userDeterministicKey, String deterministicKey) {
        Integer sortCode =
                Uksortcodes.get(generateNumber(userDeterministicKey, 2) % Uksortcodes.size());
        return sortCode
                + ("" + generateNumber(deterministicKey, 4) + generateNumber(deterministicKey, 4));
    }

    public static DemoSavingsAccount getDemoSavingsAccounts(String username, String providerName) {

        String deterministicKey = createDeterministicKey("Savings" + username + providerName);
        String userDeterministicKey = createDeterministicKey(username + providerName);
        return new DemoSavingsAccount() {
            @Override
            public String getAccountId() {
                if (providerName.matches(ukProviderRegex)) {
                    return generateAccoutNumbersUK(userDeterministicKey, deterministicKey);
                } else return generateAccoutNumbers(deterministicKey);
            }

            @Override
            public String getAccountName() {
                return "Savings Account " + username;
            }

            @Override
            public double getAccountBalance() {
                return generateDouble(deterministicKey, 7);
            }

            @Override
            public List<AccountIdentifier> getIdentifiers() {
                AccountIdentifier.Type type = AccountIdentifier.Type.SE;
                if (providerName.matches(ukProviderRegex)) {
                    type = AccountIdentifier.Type.SORT_CODE;
                }
                AccountIdentifier identifier =
                        AccountIdentifier.create(type, getAccountId(), "testAccount");
                return Lists.newArrayList(identifier);
            }
        };
    }

    public static DemoTransactionAccount getDemoTransactionalAccount(
            String username, String providerName) {
        String deterministicKey = createDeterministicKey("Transaction" + username + providerName);
        String userDeterministicKey = createDeterministicKey(username + providerName);

        return new DemoTransactionAccount() {
            @Override
            public String getAccountId() {
                if (providerName.matches(ukProviderRegex)) {
                    return generateAccoutNumbersUK(userDeterministicKey, deterministicKey);
                } else return generateAccoutNumbers(deterministicKey);
            }

            @Override
            public String getAccountName() {
                return "Checking Account " + username;
            }

            @Override
            public double getBalance() {
                return generateDouble(deterministicKey, 5);
            }

            @Override
            public List<AccountIdentifier> getIdentifiers() {
                AccountIdentifier.Type type = AccountIdentifier.Type.SE;
                if (providerName.matches(ukProviderRegex)) {
                    type = AccountIdentifier.Type.SORT_CODE;
                }
                AccountIdentifier identifier =
                        AccountIdentifier.create(type, getAccountId(), "testAccount");
                return Lists.newArrayList(identifier);
            }
        };
    }

    public static DemoTransactionAccount getDemoTransactionalAccountWithZeroBalance(
            String username, String providerName) {
        String deterministicKey =
                createDeterministicKey("Transaction with zero balance" + username + providerName);
        String userDeterministicKey = createDeterministicKey(username + providerName);

        return new DemoTransactionAccount() {
            @Override
            public String getAccountId() {
                if (providerName.matches(ukProviderRegex)) {
                    return generateAccoutNumbersUK(userDeterministicKey, deterministicKey);
                } else return generateAccoutNumbers(deterministicKey);
            }

            @Override
            public String getAccountName() {
                return "Checking Account " + username + " zero balance";
            }

            @Override
            public double getBalance() {
                return 0.00;
            }

            @Override
            public List<AccountIdentifier> getIdentifiers() {
                AccountIdentifier.Type type = AccountIdentifier.Type.SE;
                if (providerName.matches(ukProviderRegex)) {
                    type = AccountIdentifier.Type.SORT_CODE;
                }
                AccountIdentifier identifier =
                        AccountIdentifier.create(type, getAccountId(), "testAccount");
                return Lists.newArrayList(identifier);
            }
        };
    }
}
