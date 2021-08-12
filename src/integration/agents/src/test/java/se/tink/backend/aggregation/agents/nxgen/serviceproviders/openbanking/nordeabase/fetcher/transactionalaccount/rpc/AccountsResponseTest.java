package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.rpc;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.BbanIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AccountsResponseTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/nordeabase/resources";

    private static final String DK_CURRENCY = "DKK";
    private static final String NO_CURRENCY = "NOK";
    private static final String EUR_CURRENCY = "EUR";
    private static final String DANISH_IBAN = "DK9150519593123646";
    private static final String DK_BIC = "NDEADKKK";
    private static final String NORWEGIAN_IBAN = "NO3750022184484";
    private static final String NO_BIC = "NDEANOKK";
    private static final String FINNISH_IBAN = "FI4367927539822622";
    private static final String FI_BIC = "NDEAFIHH";
    private static final String ZERO_AMOUNT = "0.00";

    @Test
    public void shouldMapToTinkAccount() {
        // given
        GetAccountsResponse getAccountsResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "accounts_response.json").toFile(),
                        GetAccountsResponse.class);

        // when
        List<TransactionalAccount> result =
                (List<TransactionalAccount>) getAccountsResponse.toTinkAccounts();

        // then
        assertThat(result.size()).isEqualTo(3);
        for (int i = 0; i < result.size(); i++) {
            assertThat(result.get(i))
                    .usingRecursiveComparison()
                    .ignoringCollectionOrderInFields("identifiers")
                    .isEqualTo(getExpectedAccounts().get(i));
        }
    }

    @Test
    public void shouldMapToTinkAccountWithCorrectCreditLimitWhenUserBalanceIsNegative() {
        // given
        GetAccountsResponse getAccountsResponse =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "accounts_response_with_negative_balance.json")
                                .toFile(),
                        GetAccountsResponse.class);

        // when
        List<TransactionalAccount> result =
                (List<TransactionalAccount>) getAccountsResponse.toTinkAccounts();

        // then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0))
                .usingRecursiveComparison()
                .ignoringCollectionOrderInFields("identifiers")
                .isEqualTo(getExpectedAccountWithNegativeBalance());
    }

    private List<TransactionalAccount> getExpectedAccounts() {
        return Arrays.asList(
                TransactionalAccount.nxBuilder()
                        .withType(TransactionalAccountType.CHECKING)
                        .withPaymentAccountFlag()
                        .withBalance(
                                BalanceModule.builder()
                                        .withBalance(
                                                new ExactCurrencyAmount(
                                                        BigDecimal.valueOf(1.12), DK_CURRENCY))
                                        .setAvailableBalance(
                                                new ExactCurrencyAmount(
                                                        BigDecimal.valueOf(1.12), DK_CURRENCY))
                                        .setCreditLimit(
                                                ExactCurrencyAmount.of(ZERO_AMOUNT, DK_CURRENCY))
                                        .build())
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier("9593123646")
                                        .withAccountNumber(DANISH_IBAN)
                                        .withAccountName("Lønkonto+")
                                        .addIdentifier(new IbanIdentifier(DK_BIC, DANISH_IBAN))
                                        .addIdentifier(new BbanIdentifier("50519593123646"))
                                        .build())
                        .putInTemporaryStorage(
                                NordeaBaseConstants.StorageKeys.ACCOUNT_ID, "DK50519593123646-DKK")
                        .setApiIdentifier("DK50519593123646-DKK")
                        .addHolderName("NAME LAST_NAME")
                        .build()
                        .orElseThrow(IllegalStateException::new),
                TransactionalAccount.nxBuilder()
                        .withType(TransactionalAccountType.SAVINGS)
                        .withPaymentAccountFlag()
                        .withBalance(
                                BalanceModule.builder()
                                        .withBalance(
                                                ExactCurrencyAmount.of(ZERO_AMOUNT, NO_CURRENCY))
                                        .setAvailableBalance(
                                                ExactCurrencyAmount.of(ZERO_AMOUNT, NO_CURRENCY))
                                        .setCreditLimit(
                                                ExactCurrencyAmount.of(ZERO_AMOUNT, NO_CURRENCY))
                                        .build())
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(NORWEGIAN_IBAN)
                                        .withAccountNumber(NORWEGIAN_IBAN)
                                        .withAccountName("SPAREKONTO")
                                        .addIdentifier(new IbanIdentifier(NO_BIC, NORWEGIAN_IBAN))
                                        .addIdentifier(new BbanIdentifier("50022184484"))
                                        .build())
                        .putInTemporaryStorage(
                                NordeaBaseConstants.StorageKeys.ACCOUNT_ID, "NO50022184484-NOK")
                        .setApiIdentifier("NO50022184484-NOK")
                        .addHolderName("NOR_NAME NOR_LAST_NAME")
                        .build()
                        .orElseThrow(IllegalStateException::new),
                TransactionalAccount.nxBuilder()
                        .withType(TransactionalAccountType.CHECKING)
                        .withPaymentAccountFlag()
                        .withBalance(
                                BalanceModule.builder()
                                        .withBalance(
                                                new ExactCurrencyAmount(
                                                        BigDecimal.valueOf(111.12), EUR_CURRENCY))
                                        .setAvailableBalance(
                                                new ExactCurrencyAmount(
                                                        BigDecimal.valueOf(111.11), EUR_CURRENCY))
                                        .build())
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(FINNISH_IBAN)
                                        .withAccountNumber(FINNISH_IBAN)
                                        .withAccountName("KÄYTTÖTILI")
                                        .addIdentifier(new IbanIdentifier(FI_BIC, FINNISH_IBAN))
                                        .build())
                        .putInTemporaryStorage(
                                NordeaBaseConstants.StorageKeys.ACCOUNT_ID,
                                "FI4367927539822622-EUR")
                        .setApiIdentifier("FI4367927539822622-EUR")
                        .addHolderName("FINNISH GUY")
                        .build()
                        .orElseThrow(IllegalStateException::new));
    }

    private TransactionalAccount getExpectedAccountWithNegativeBalance() {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(
                        BalanceModule.builder()
                                .withBalance(
                                        new ExactCurrencyAmount(
                                                BigDecimal.valueOf(-6811.39), DK_CURRENCY))
                                .setAvailableBalance(
                                        new ExactCurrencyAmount(
                                                BigDecimal.valueOf(-6811.39), DK_CURRENCY))
                                .setCreditLimit(ExactCurrencyAmount.of("10000.00", DK_CURRENCY))
                                .build())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier("9593123646")
                                .withAccountNumber(DANISH_IBAN)
                                .withAccountName("Lønkonto+")
                                .addIdentifier(new IbanIdentifier(DK_BIC, DANISH_IBAN))
                                .addIdentifier(new BbanIdentifier("50519593123646"))
                                .build())
                .putInTemporaryStorage(
                        NordeaBaseConstants.StorageKeys.ACCOUNT_ID, "DK50519593123646-DKK")
                .setApiIdentifier("DK50519593123646-DKK")
                .addHolderName("NAME LAST_NAME")
                .build()
                .orElseThrow(IllegalStateException::new);
    }
}
