package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.card.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.card.rpc.CardResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.entities.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.rpc.BalanceResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SparebankCardMapperTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sparebank/resources";
    private static final String TEST_RESOURCE_ID =
            "TEST_enc!!QhCfLR1Au7ePQVYqH3s-ASDDSFGKLSDGLJSLDKJ";
    private static final String TEST_NAME = "TEST_SpareBank 1 Mastercard Ung";
    private static final String TEST_CURRENCY = "NOK";
    private static final String TEST_MASKED_PAN = "123456******1234";

    private SparebankCardMapper cardMapper;

    @Before
    public void setup() {
        cardMapper = new SparebankCardMapper();
    }

    @Test
    public void shouldReturnEmptyOptionalWhenFailedToParse() {
        // when
        Optional<CreditCardAccount> maybeCardAccount = cardMapper.toTinkCardAccount(null, null);

        // then
        assertThat(maybeCardAccount.isPresent()).isFalse();
    }

    @Test
    public void shouldMapCardAccountProperly() {
        // given

        // when
        Optional<CreditCardAccount> maybeCreditCardAccount =
                cardMapper.toTinkCardAccount(getTestCardAccount(), getTestBalances());

        // then
        assertThat(maybeCreditCardAccount.isPresent()).isTrue();
        CreditCardAccount creditCardAccount = maybeCreditCardAccount.get();

        assertThat(creditCardAccount.getCardModule().getCardNumber()).isEqualTo(TEST_MASKED_PAN);
        assertThat(creditCardAccount.getCardModule().getBalance())
                .isEqualTo(ExactCurrencyAmount.of(10, "NOK"));
        assertThat(creditCardAccount.getCardModule().getAvailableCredit())
                .isEqualTo(ExactCurrencyAmount.of(9990, "NOK"));
        assertThat(creditCardAccount.getCardModule().getCardAlias()).isEqualTo(TEST_NAME);

        assertThat(creditCardAccount.isUniqueIdentifierEqual(TEST_MASKED_PAN)).isTrue();
        assertThat(creditCardAccount.getAccountNumber()).isEqualTo(TEST_MASKED_PAN);
        assertThat(creditCardAccount.getName()).isEqualTo(TEST_NAME);
        assertThat(creditCardAccount.getIdentifiers()).hasSize(1);
        assertThat(creditCardAccount.getIdentifiers().get(0))
                .isEqualTo(
                        AccountIdentifier.create(
                                AccountIdentifier.Type.PAYMENT_CARD_NUMBER, TEST_MASKED_PAN));

        assertThat(creditCardAccount.getApiIdentifier()).isEqualTo(TEST_RESOURCE_ID);
    }

    private CardEntity getTestCardAccount() {
        return SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "cardAccount.json").toFile(), CardResponse.class)
                .getCardAccounts()
                .get(0);
    }

    private List<BalanceEntity> getTestBalances() {
        return SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "cardBalances.json").toFile(),
                        BalanceResponse.class)
                .getBalances();
    }
}
