package se.tink.backend.aggregation.nxgen.agents.demo;

import static se.tink.backend.aggregation.nxgen.agents.demo.DemoConstants.MARKET_CODES;
import static se.tink.backend.aggregation.nxgen.agents.demo.DemoConstants.MARKET_REGEX;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoSavingsAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoTransactionAccount;
import se.tink.libraries.account.AccountIdentifier;

/** Deterministic account generator based on user-name and provider */
public class DemoAccountDefinitionGenerator {

    private static String createDeterministicKey(String combination) {
        return Integer.toString(Math.abs(combination.hashCode()));
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
        String number = getAccountNumber(userDeterministicKey);
        String code = getCode(deterministicKey);
        Iban iban =
                new Iban.Builder()
                        .countryCode(CountryCode.IT)
                        .nationalCheckDigit("X")
                        .branchCode(code)
                        .accountNumber(number)
                        .bankCode(code)
                        .build();
        // IT 18 X 8930 8930 00000202985435
        // IT 21 X 70723 70723 00000 202985435
        return iban.toString();
    }

    private static String getCode(String deterministicKey) {
        if (deterministicKey.length() < 4) {
            return Strings.padStart(deterministicKey, 4, '0');
        } else {
            return deterministicKey.substring(
                    deterministicKey.length() - 4, deterministicKey.length());
        }
    }

    private static String getAccountNumber(String userDeterministicKey) {
        if (userDeterministicKey.length() < 14) {
            return Strings.padStart(userDeterministicKey, 14, '0');
        } else {
            return userDeterministicKey.substring(14);
        }
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
            public Optional<Double> getAvailableBalance() {
                return Optional.of(
                        BigDecimal.valueOf(getBalance() * 0.9)
                                .setScale(2, BigDecimal.ROUND_HALF_UP)
                                .doubleValue());
            }

            @Override
            public Optional<Double> getCreditLimit() {
                int val = generateNumber(deterministicKey, 1);
                if (val > 5) {
                    return Optional.empty();
                } else {
                    return Optional.of(val * 1000.0);
                }
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

    public static DemoTransactionAccount getDemoTransactionalAccountWithoutIdentifiers(
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
            public Optional<Double> getAvailableBalance() {
                return Optional.of(
                        BigDecimal.valueOf(getBalance() * 0.9)
                                .setScale(2, BigDecimal.ROUND_HALF_UP)
                                .doubleValue());
            }

            @Override
            public Optional<Double> getCreditLimit() {
                int val = generateNumber(deterministicKey, 1);
                if (val > 5) {
                    return Optional.empty();
                } else {
                    return Optional.of(val * 1000.0);
                }
            }

            @Override
            public List<AccountIdentifier> getIdentifiers() {
                return Collections.emptyList();
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
            public Optional<Double> getAvailableBalance() {
                return Optional.of(0.0);
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
