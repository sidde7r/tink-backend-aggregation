package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.entity.CardAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.rpc.BalancesResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DnbCardMapperTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/openbanking/dnb/resources";
    private static final String TEST_RESOURCE_ID = "test_resource_id";
    private static final String TEST_NAME = "test_name";
    private static final String TEST_CURRENCY = "NOK";
    private static final String TEST_MASKED_PAN = "*****123456";

    private DnbCardMapper cardMapper;

    @Before
    public void setup() {
        cardMapper = new DnbCardMapper();
    }

    @Test
    public void shouldReturnEmptyOptionalWhenFailedToParse() {
        // when
        Optional<CreditCardAccount> maybeCardAccount = cardMapper.toTinkCardAccount(null);

        // then
        assertThat(maybeCardAccount.isPresent()).isFalse();
    }

    @Test
    public void shouldMapCardAccountProperly() {
        // given
        CardAccountEntity testCardAccount = getTestCardAccount();

        // when
        Optional<CreditCardAccount> maybeCardAccount =
                cardMapper.toTinkCardAccount(testCardAccount);

        // then
        assertThat(maybeCardAccount.isPresent()).isTrue();

        CreditCardAccount cardAccount = maybeCardAccount.get();
        assertThat(cardAccount.getCardModule().getCardNumber()).isEqualTo(TEST_MASKED_PAN);
        assertThat(cardAccount.getCardModule().getBalance())
                .isEqualTo(ExactCurrencyAmount.of(0.99, TEST_CURRENCY));
        assertThat(cardAccount.getCardModule().getAvailableCredit())
                .isEqualTo(ExactCurrencyAmount.of(10000.99, TEST_CURRENCY));
        assertThat(cardAccount.getCardModule().getCardAlias()).isEqualTo(TEST_NAME);

        assertThat(cardAccount.isUniqueIdentifierEqual(TEST_MASKED_PAN)).isTrue();
        assertThat(cardAccount.getAccountNumber()).isEqualTo(TEST_RESOURCE_ID);
        assertThat(cardAccount.getName()).isEqualTo(TEST_NAME);
        assertThat(cardAccount.getIdentifiers()).hasSize(1);
        assertThat(cardAccount.getIdentifiers().get(0))
                .isEqualTo(
                        AccountIdentifier.create(
                                AccountIdentifierType.PAYMENT_CARD_NUMBER, TEST_MASKED_PAN));

        assertThat(cardAccount.getApiIdentifier()).isEqualTo(TEST_RESOURCE_ID);
    }

    private CardAccountEntity getTestCardAccount() {
        return new CardAccountEntity(
                TEST_CURRENCY,
                TEST_MASKED_PAN,
                TEST_NAME,
                TEST_RESOURCE_ID,
                getTestBalances().getBalances());
    }

    private BalancesResponse getTestBalances() {
        return SerializationUtils.deserializeFromString(
                Paths.get(TEST_DATA_PATH, "balances.json").toFile(), BalancesResponse.class);
    }
}
