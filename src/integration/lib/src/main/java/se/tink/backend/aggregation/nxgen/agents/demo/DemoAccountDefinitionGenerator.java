package se.tink.backend.aggregation.nxgen.agents.demo;

import static se.tink.backend.aggregation.nxgen.agents.demo.DemoConstants.MARKET_CODES;
import static se.tink.backend.aggregation.nxgen.agents.demo.DemoConstants.MARKET_REGEX;

import com.google.common.collect.Lists;
import java.util.List;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoSavingsAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoTransactionAccount;
import se.tink.libraries.account.AccountIdentifier;

/** Deterministic account generator based on user-name and provider */
public class DemoAccountDefinitionGenerator {

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

    private static String generateAccountNumbers(String deterministicKey) {
        return ("" + generateNumber(deterministicKey, 4))
                + "-"
                + ("" + generateNumber(deterministicKey, 5) + generateNumber(deterministicKey, 7));
    }

    private static String generateAccountNumbersUK(
            String userDeterministicKey, String deterministicKey) {
        Integer sortCode =
                DemoConstants.MARKET_CODES.UK_SORT_CODES.get(
                        generateNumber(userDeterministicKey, 2)
                                % MARKET_CODES.UK_SORT_CODES.size());
        return sortCode
                + ("" + generateNumber(deterministicKey, 5) + generateNumber(deterministicKey, 3));
    }

    private static String generateAccountNumbersIT(
            String userDeterministicKey, String deterministicKey) {
        Integer bankIdentifier =
                MARKET_CODES.IT_BANK_IDENTIFIERS.get(
                        generateNumber(userDeterministicKey, 2)
                                % MARKET_CODES.IT_BANK_IDENTIFIERS.size());
        Integer branchIdentifier =
                MARKET_CODES.IT_BANK_ACCOUNT_IDENTIFIER.get(
                        generateNumber(userDeterministicKey, 2)
                                % MARKET_CODES.IT_BANK_ACCOUNT_IDENTIFIER.size());

        return DemoConstants.IT_ACCOUNT_NUMBER_PREFIX
                + String.format("%05d", bankIdentifier)
                + String.format("%05d", branchIdentifier)
                + ("" + generateNumber(deterministicKey, 5) + generateNumber(deterministicKey, 7));
    }

    public static DemoSavingsAccount getDemoSavingsAccounts(String username, String providerName) {
        return getDemoSavingsAccounts(username, providerName, 0);
    }

    public static DemoSavingsAccount getDemoSavingsAccounts(
            String username, String providerName, int key) {
        String deterministicKey =
                createDeterministicKey("Savings" + (key != 0 ? key : "") + username + providerName);
        String userDeterministicKey = createDeterministicKey(username + providerName);
        return new DemoSavingsAccount() {
            @Override
            public String getAccountId() {
                if (providerName.matches(MARKET_REGEX.UK_PROVIDERS_REGEX)) {
                    return generateAccountNumbersUK(userDeterministicKey, deterministicKey);
                } else if (providerName.matches(MARKET_REGEX.IT_PROVIDERS_REGEX)) {
                    return generateAccountNumbersIT(userDeterministicKey, deterministicKey);
                } else {
                    return generateAccountNumbers(deterministicKey);
                }
            }

            @Override
            public String getAccountName() {
                return "Savings Account " + username + (key != 0 ? " " + key : "");
            }

            @Override
            public double getAccountBalance() {
                return generateDouble(deterministicKey, 7);
            }

            @Override
            public List<AccountIdentifier> getIdentifiers() {
                AccountIdentifier.Type type = AccountIdentifier.Type.SE;
                if (providerName.matches(MARKET_REGEX.UK_PROVIDERS_REGEX)) {
                    type = AccountIdentifier.Type.SORT_CODE;
                }
                if (providerName.matches(MARKET_REGEX.IT_PROVIDERS_REGEX)) {
                    type = AccountIdentifier.Type.IBAN;
                }
                AccountIdentifier identifier =
                        AccountIdentifier.create(type, getAccountId(), "testAccount");
                return Lists.newArrayList(identifier);
            }
        };
    }

    public static DemoTransactionAccount getDemoTransactionalAccount(
            String username, String providerName) {
        return getDemoTransactionalAccount(username, providerName, 0);
    }

    public static DemoTransactionAccount getDemoTransactionalAccount(
            String username, String providerName, int key) {
        String deterministicKey =
                createDeterministicKey(
                        "Transaction" + (key != 0 ? key : "") + username + providerName);
        String userDeterministicKey = createDeterministicKey(username + providerName);
        return new DemoTransactionAccount() {
            @Override
            public String getAccountId() {
                if (providerName.matches(MARKET_REGEX.UK_PROVIDERS_REGEX)) {
                    return generateAccountNumbersUK(userDeterministicKey, deterministicKey);
                } else if (providerName.matches(MARKET_REGEX.IT_PROVIDERS_REGEX)) {
                    return generateAccountNumbersIT(userDeterministicKey, deterministicKey);
                } else {
                    return generateAccountNumbers(deterministicKey);
                }
            }

            @Override
            public String getAccountName() {
                return "Checking Account " + username + (key != 0 ? " " + key : "");
            }

            @Override
            public double getBalance() {
                return generateDouble(deterministicKey, 5);
            }

            @Override
            public List<AccountIdentifier> getIdentifiers() {
                AccountIdentifier.Type type = AccountIdentifier.Type.SE;
                if (providerName.matches(MARKET_REGEX.UK_PROVIDERS_REGEX)) {
                    type = AccountIdentifier.Type.SORT_CODE;
                }
                if (providerName.matches(MARKET_REGEX.IT_PROVIDERS_REGEX)) {
                    type = AccountIdentifier.Type.IBAN;
                }
                AccountIdentifier identifier =
                        AccountIdentifier.create(type, getAccountId(), "testAccount");
                return Lists.newArrayList(identifier);
            }
        };
    }

    public static DemoTransactionAccount getDemoTransactionalAccountWithZeroBalance(
            String username, String providerName) {
        return getDemoTransactionalAccountWithZeroBalance(username, providerName, 0);
    }

    public static DemoTransactionAccount getDemoTransactionalAccountWithZeroBalance(
            String username, String providerName, int key) {
        String deterministicKey =
                createDeterministicKey(
                        "Transaction with zero balance"
                                + (key != 0 ? key : "")
                                + username
                                + providerName);
        String userDeterministicKey = createDeterministicKey(username + providerName);
        return new DemoTransactionAccount() {
            @Override
            public String getAccountId() {
                if (providerName.matches(MARKET_REGEX.UK_PROVIDERS_REGEX)) {
                    return generateAccountNumbersUK(userDeterministicKey, deterministicKey);
                } else if (providerName.matches(MARKET_REGEX.IT_PROVIDERS_REGEX)) {
                    return generateAccountNumbersIT(userDeterministicKey, deterministicKey);
                } else {
                    return generateAccountNumbers(deterministicKey);
                }
            }

            @Override
            public String getAccountName() {
                return "Checking Account "
                        + username
                        + " zero balance"
                        + (key != 0 ? " " + key : "");
            }

            @Override
            public double getBalance() {
                return 0.00;
            }

            @Override
            public List<AccountIdentifier> getIdentifiers() {
                AccountIdentifier.Type type = AccountIdentifier.Type.SE;
                if (providerName.matches(MARKET_REGEX.UK_PROVIDERS_REGEX)) {
                    type = AccountIdentifier.Type.SORT_CODE;
                }
                if (providerName.matches(MARKET_REGEX.IT_PROVIDERS_REGEX)) {
                    type = AccountIdentifier.Type.IBAN;
                }
                AccountIdentifier identifier =
                        AccountIdentifier.create(type, getAccountId(), "testAccount");
                return Lists.newArrayList(identifier);
            }
        };
    }
}
