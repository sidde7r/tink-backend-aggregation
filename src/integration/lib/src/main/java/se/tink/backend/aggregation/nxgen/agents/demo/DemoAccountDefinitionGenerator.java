package se.tink.backend.aggregation.nxgen.agents.demo;

import static se.tink.backend.aggregation.nxgen.agents.demo.DemoConstants.MarketCodes;
import static se.tink.backend.aggregation.nxgen.agents.demo.DemoConstants.MarketRegex;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.general.TransferDestinationPatternBuilder;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntityImpl;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoSavingsAccount;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoTransactionAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.BbanIdentifier;
import se.tink.libraries.account.identifiers.BelgianIdentifier;
import se.tink.libraries.account.identifiers.DanishIdentifier;
import se.tink.libraries.account.identifiers.FinnishIdentifier;
import se.tink.libraries.account.identifiers.GermanIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.NDAPersonalNumberIdentifier;
import se.tink.libraries.account.identifiers.NorwegianIdentifier;
import se.tink.libraries.account.identifiers.PaymPhoneNumberIdentifier;
import se.tink.libraries.account.identifiers.PaymentCardNumberIdentifier;
import se.tink.libraries.account.identifiers.PlusGiroIdentifier;
import se.tink.libraries.account.identifiers.PortugalBancoBpiIdentifier;
import se.tink.libraries.account.identifiers.SepaEurIdentifier;
import se.tink.libraries.account.identifiers.SortCodeIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.SwedishSHBInternalIdentifier;
import se.tink.libraries.account.identifiers.TinkIdentifier;

/** Deterministic account generator based on user-name and provider */
public class DemoAccountDefinitionGenerator {
    private static final Map<Type, Class<? extends AccountIdentifier>> identifiersForType;

    static {
        Map<Type, Class<? extends AccountIdentifier>> map = new HashMap<>();
        map.put(Type.IBAN, IbanIdentifier.class);
        map.put(Type.BBAN, BbanIdentifier.class);
        map.put(Type.BE, BelgianIdentifier.class);
        map.put(Type.SE, SwedishIdentifier.class);
        map.put(Type.SORT_CODE, SortCodeIdentifier.class);
        map.put(Type.SE_BG, BankGiroIdentifier.class);
        map.put(Type.SE_PG, PlusGiroIdentifier.class);
        map.put(Type.PT_BPI, PortugalBancoBpiIdentifier.class);
        map.put(Type.SE_SHB_INTERNAL, SwedishSHBInternalIdentifier.class);
        map.put(Type.SEPA_EUR, SepaEurIdentifier.class);
        map.put(Type.DE, GermanIdentifier.class);
        map.put(Type.DK, DanishIdentifier.class);
        map.put(Type.NO, NorwegianIdentifier.class);
        map.put(Type.FI, FinnishIdentifier.class);
        map.put(Type.SE_NDA_SSN, NDAPersonalNumberIdentifier.class);
        map.put(Type.TINK, TinkIdentifier.class);
        map.put(Type.PAYM_PHONE_NUMBER, PaymPhoneNumberIdentifier.class);
        map.put(Type.PAYMENT_CARD_NUMBER, PaymentCardNumberIdentifier.class);
        identifiersForType = Collections.unmodifiableMap(map);
    }

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
                MarketCodes.UK_SORT_CODES.get(
                        generateNumber(userDeterministicKey, 2) % MarketCodes.UK_SORT_CODES.size());
        return sortCode
                + ("" + generateNumber(deterministicKey, 5) + generateNumber(deterministicKey, 3));
    }

    private static String generateIban(
            String key,
            CountryCode countryCode,
            String nationalCheckDigit,
            String branchCode,
            String bankCode,
            int accountNumberLength) {
        String number = getAccountNumber(key, accountNumberLength);

        // This builder does not handle IBAN for Norway correctly
        // https://github.com/arturmkrtchyan/iban4j/issues/71
        return new Iban.Builder()
                .countryCode(countryCode)
                .nationalCheckDigit(nationalCheckDigit)
                .branchCode(branchCode)
                .accountNumber(number)
                .bankCode(bankCode)
                .build()
                .toString();
    }

    private static String generateAccountNumbersIT(String deterministicKey) {
        return generateIban(deterministicKey, CountryCode.IT, "X", "11101", "05428", 12);
    }

    private static String generateAccountNumbersFR(String deterministicKey) {
        return generateIban(deterministicKey, CountryCode.FR, "0", "01005", "20041", 12);
    }

    private static String getAccountNumber(String userDeterministicKey, int expectedLength) {
        if (userDeterministicKey.length() < expectedLength) {
            return Strings.padStart(userDeterministicKey, expectedLength, '0');
        } else {
            return userDeterministicKey.substring(expectedLength);
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
                return prepareAccountId(providerName, userDeterministicKey, deterministicKey);
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
                if (providerName.matches(MarketRegex.UK_PROVIDERS_REGEX)) {
                    type = AccountIdentifier.Type.SORT_CODE;
                }
                if (shouldSetIbanAsIdentifier(providerName)) {
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
                return prepareAccountId(providerName, userDeterministicKey, deterministicKey);
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
                if (providerName.matches(MarketRegex.UK_PROVIDERS_REGEX)) {
                    type = AccountIdentifier.Type.SORT_CODE;
                }
                if (shouldSetIbanAsIdentifier(providerName)) {
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
                return prepareAccountId(providerName, userDeterministicKey, deterministicKey);
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

    private static DemoTransactionAccount getDemoTransactionalAccountWithZeroBalance(
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
                return prepareAccountId(providerName, userDeterministicKey, deterministicKey);
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
                if (providerName.matches(MarketRegex.UK_PROVIDERS_REGEX)) {
                    type = AccountIdentifier.Type.SORT_CODE;
                }
                if (shouldSetIbanAsIdentifier(providerName)) {
                    type = AccountIdentifier.Type.IBAN;
                }
                AccountIdentifier identifier =
                        AccountIdentifier.create(type, getAccountId(), "testAccount");
                return Lists.newArrayList(identifier);
            }
        };
    }

    public static Optional<? extends GeneralAccountEntity> accountToGeneralAccountEntity(
            Account account) {
        return GeneralAccountEntityImpl.createFromCoreAccount(account);
    }

    public static Map<Account, List<TransferDestinationPattern>> generateTransferDestinations(
            List<Account> accounts) {
        List<Account> sourceAccounts =
                accounts.stream()
                        .filter(
                                a ->
                                        a.getType() == AccountTypes.CHECKING
                                                || a.getType() == AccountTypes.SAVINGS)
                        .collect(Collectors.toList());
        List<AccountIdentifier> sourceIdentifiers =
                accounts.stream()
                        .filter(
                                a ->
                                        a.getType() == AccountTypes.CHECKING
                                                || a.getType() == AccountTypes.SAVINGS)
                        .map(Account::getIdentifiers)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
        Map<Account, List<TransferDestinationPattern>> result = new HashMap<>();
        sourceIdentifiers.forEach(
                id -> result.putAll(generateTransferDestinations(sourceAccounts, id.getType())));
        return result;
    }

    private static Map<Account, List<TransferDestinationPattern>> generateTransferDestinations(
            List<Account> accounts, Type destinationAccountType) {
        List<GeneralAccountEntity> sourceAccounts =
                accounts.stream()
                        .map(DemoAccountDefinitionGenerator::accountToGeneralAccountEntity)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
        List<GeneralAccountEntity> destinationAccounts =
                accounts.stream()
                        .map(DemoAccountDefinitionGenerator::accountToGeneralAccountEntity)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
        TransferDestinationPatternBuilder builder =
                new TransferDestinationPatternBuilder()
                        .matchDestinationAccountsOn(
                                destinationAccountType, getClassForType(destinationAccountType))
                        .setSourceAccounts(sourceAccounts)
                        .setDestinationAccounts(destinationAccounts)
                        .setTinkAccounts(accounts)
                        .addMultiMatchPattern(
                                destinationAccountType, TransferDestinationPattern.ALL);
        if (destinationAccountType.equals(Type.SE)) {
            builder.addMultiMatchPattern(Type.SE_PG, TransferDestinationPattern.ALL)
                    .addMultiMatchPattern(Type.SE_BG, TransferDestinationPattern.ALL);
        }
        return builder.build();
    }

    private static Class<? extends AccountIdentifier> getClassForType(Type type) {
        Class<? extends AccountIdentifier> clazz = identifiersForType.get(type);
        if (clazz == null) {
            throw new IllegalArgumentException("Unknown identifier.");
        }
        return clazz;
    }

    private static String prepareAccountId(
            String providerName, String userDeterministicKey, String deterministicKey) {
        if (providerName.matches(MarketRegex.UK_PROVIDERS_REGEX)) {
            return generateAccountNumbersUK(userDeterministicKey, deterministicKey);
        } else if (providerName.matches(MarketRegex.IT_PROVIDERS_REGEX)) {
            return generateAccountNumbersIT(deterministicKey);
        } else if (providerName.matches(MarketRegex.FR_PROVIDERS_REGEX)) {
            return generateAccountNumbersFR(deterministicKey);
        } else {
            return generateAccountNumbers(deterministicKey);
        }
    }

    private static boolean shouldSetIbanAsIdentifier(String providerName) {
        return providerName.matches(MarketRegex.IT_PROVIDERS_REGEX)
                || providerName.matches(MarketRegex.FR_PROVIDERS_REGEX);
    }
}
