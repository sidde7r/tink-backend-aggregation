package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.mapper;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountEntity;
import se.tink.libraries.account.identifiers.DanishIdentifier;
import se.tink.libraries.account.identifiers.FinnishIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.NorwegianIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

@Ignore
public class AccountEntityMarketMapperTestParams {

    private static final String DK = "DK";
    private static final String NO = "NO";
    private static final String SE = "SE";
    private static final String FI = "FI";

    public static Object[] getUniqueIdTestParams() {
        return new Object[] {
            arr(accountEntity("0123123123", "0123456789"), "0123456789", DK),
            arr(accountEntity("0123456789", "0123456789"), "0123456789", NO),
            arr(accountEntity("0123123123", "0123456789"), "0123456789", SE),
            arr(accountEntity("0123123123", "0123456789"), "0123123123", FI),
            arr(accountEntity("12345", "0123456"), "0000123456", DK),
            arr(accountEntity("12345", "0123456"), "0123456", NO),
            arr(accountEntity("12345", "0123456"), "0123456", SE),
            arr(accountEntity("12345", "0123456"), "12345", FI),
            arr(accountEntity("abc", "01*"), "000000001*", DK),
            arr(accountEntity("abc", "01*"), "01*", NO),
            arr(accountEntity("abc", "01*"), "01*", SE),
            arr(accountEntity("abc", "01*"), "abc", FI)
        };
    }

    private static final String FIRST_OWNER = "NAME LASTNAME";
    private static final String FIRST_OWNER_WITH_SSN = "12345678901 - NAME LASTNAME";
    private static final String SECOND_OWNER = "SECOND NAME LASTNAME";
    private static final String SECOND_OWNER_WITH_SSN = "1234512 - SECOND NAME LASTNAME";

    public static Object[] getAccountOwnersTestParams() {
        return new Object[] {
            arr(null, emptyList(), DK),
            arr(null, emptyList(), NO),
            arr(null, emptyList(), SE),
            arr(null, emptyList(), FI),
            arr(asList("", " "), emptyList(), DK),
            arr(asList("", " "), emptyList(), NO),
            arr(asList("", " "), emptyList(), SE),
            arr(asList("", " "), emptyList(), FI),
            arr(asList(" ", FIRST_OWNER), singletonList(FIRST_OWNER), DK),
            arr(asList(" ", FIRST_OWNER), emptyList(), NO),
            arr(asList(" ", FIRST_OWNER), emptyList(), SE),
            arr(asList(" ", FIRST_OWNER), emptyList(), FI),
            arr(asList(null, FIRST_OWNER), singletonList(FIRST_OWNER), DK),
            arr(asList(null, FIRST_OWNER), emptyList(), NO),
            arr(asList(null, FIRST_OWNER), emptyList(), SE),
            arr(asList(null, FIRST_OWNER), emptyList(), FI),
            arr(asList(" ", FIRST_OWNER_WITH_SSN), singletonList(FIRST_OWNER_WITH_SSN), DK),
            arr(asList(" ", FIRST_OWNER_WITH_SSN), singletonList(FIRST_OWNER), NO),
            arr(asList(" ", FIRST_OWNER_WITH_SSN), singletonList(FIRST_OWNER), SE),
            arr(asList(" ", FIRST_OWNER_WITH_SSN), emptyList(), FI),
            arr(asList("abc", FIRST_OWNER_WITH_SSN), asList("abc", FIRST_OWNER_WITH_SSN), DK),
            arr(asList("abc", FIRST_OWNER_WITH_SSN), emptyList(), NO),
            arr(asList("abc", FIRST_OWNER_WITH_SSN), emptyList(), SE),
            arr(asList("abc", FIRST_OWNER_WITH_SSN), emptyList(), FI),
            arr(asList(null, FIRST_OWNER_WITH_SSN), singletonList(FIRST_OWNER_WITH_SSN), DK),
            arr(asList(null, FIRST_OWNER_WITH_SSN), singletonList(FIRST_OWNER), NO),
            arr(asList(null, FIRST_OWNER_WITH_SSN), singletonList(FIRST_OWNER), SE),
            arr(asList(null, FIRST_OWNER_WITH_SSN), emptyList(), FI),
            arr(asList(FIRST_OWNER, SECOND_OWNER), asList(FIRST_OWNER, SECOND_OWNER), DK),
            arr(asList(FIRST_OWNER, SECOND_OWNER), emptyList(), NO),
            arr(asList(FIRST_OWNER, SECOND_OWNER), emptyList(), SE),
            arr(asList(FIRST_OWNER, SECOND_OWNER), emptyList(), FI),
            arr(
                    asList(FIRST_OWNER, SECOND_OWNER_WITH_SSN),
                    asList(FIRST_OWNER, SECOND_OWNER_WITH_SSN),
                    DK),
            arr(asList(FIRST_OWNER, SECOND_OWNER_WITH_SSN), emptyList(), NO),
            arr(asList(FIRST_OWNER, SECOND_OWNER_WITH_SSN), emptyList(), SE),
            arr(asList(FIRST_OWNER, SECOND_OWNER_WITH_SSN), emptyList(), FI),
            arr(
                    asList(FIRST_OWNER_WITH_SSN, SECOND_OWNER_WITH_SSN),
                    asList(FIRST_OWNER_WITH_SSN, SECOND_OWNER_WITH_SSN),
                    DK),
            arr(
                    asList(FIRST_OWNER_WITH_SSN, SECOND_OWNER_WITH_SSN),
                    asList(FIRST_OWNER, SECOND_OWNER),
                    NO),
            arr(
                    asList(FIRST_OWNER_WITH_SSN, SECOND_OWNER_WITH_SSN),
                    asList(FIRST_OWNER, SECOND_OWNER),
                    SE),
            arr(asList(FIRST_OWNER_WITH_SSN, SECOND_OWNER_WITH_SSN), emptyList(), FI)
        };
    }

    public static Object[] getMarketIdentifierTestParams() {
        return new Object[] {
            arr(accountEntity(null), accountDetails(null), emptyList(), DK),
            arr(accountEntity(null), accountDetails(null), emptyList(), NO),
            arr(accountEntity(null), accountDetails(null), emptyList(), SE),
            arr(accountEntity(null), accountDetails(null), emptyList(), FI),
            arr(accountEntity(""), accountDetails("  "), emptyList(), DK),
            arr(accountEntity(""), accountDetails("  "), emptyList(), NO),
            arr(accountEntity(""), accountDetails("  "), emptyList(), SE),
            arr(accountEntity(""), accountDetails("  "), emptyList(), FI),
            arr(
                    accountEntity("#$%  "),
                    accountDetails(" **iban** "),
                    singletonList(new IbanIdentifier("iban")),
                    DK),
            arr(
                    accountEntity("#$%  "),
                    accountDetails("   **iban123**"),
                    singletonList(new IbanIdentifier("iban123")),
                    NO),
            arr(
                    accountEntity("#$%  "),
                    accountDetails("**iban 0 0**  "),
                    singletonList(new IbanIdentifier("iban00")),
                    SE),
            arr(
                    accountEntity("#$%  "),
                    accountDetails("* *iban* *"),
                    singletonList(new IbanIdentifier("iban")),
                    FI),
            arr(
                    accountEntity("12345#$%  "),
                    accountDetails("123 "),
                    asList(new DanishIdentifier("12345"), new IbanIdentifier("123")),
                    DK),
            arr(
                    accountEntity("#$%  12345"),
                    accountDetails("123"),
                    asList(new NorwegianIdentifier("12345"), new IbanIdentifier("123")),
                    NO),
            arr(
                    accountEntity("#1$2 3%  45"),
                    accountDetails("123"),
                    asList(new SwedishIdentifier("12345"), new IbanIdentifier("123")),
                    SE),
            arr(
                    accountEntity("#$%  12345 "),
                    accountDetails("123"),
                    asList(new FinnishIdentifier("12345"), new IbanIdentifier("123")),
                    FI)
        };
    }

    private static Object[] arr(Object... args) {
        return args;
    }

    private static AccountEntity accountEntity(String accountNoExt) {
        AccountEntity accountEntity = mock(AccountEntity.class);
        when(accountEntity.getAccountNoExt()).thenReturn(accountNoExt);
        return accountEntity;
    }

    private static AccountEntity accountEntity(String accountNoInt, String accountNoExt) {
        AccountEntity accountEntity = mock(AccountEntity.class);
        when(accountEntity.getAccountNoInt()).thenReturn(accountNoInt);
        when(accountEntity.getAccountNoExt()).thenReturn(accountNoExt);
        return accountEntity;
    }

    private static AccountDetailsResponse accountDetails(String iban) {
        AccountDetailsResponse accountDetailsResponse = new AccountDetailsResponse();
        accountDetailsResponse.setIban(iban);
        return accountDetailsResponse;
    }
}
