package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.converter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.card.LaBanquePostaleCreditCardConverter;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class LaBanquePostaleCreditCardConverterTest {

    private static final String BALANCES_RESPONSE =
            "{\"_links\":{\"self\":{\"href\":\"https://api.labanquepostale.com/v1/accounts\"}},\"accounts\":[{\"_links\":{\"balances\":{\"href\":\"https://api.labanquepostale.com/v1/accounts/ule-Q01ezzJWDTC24_6_WlFCuq2DMODy2VTiaBzWNYsMza2HPnWJN8bgvHJ9QYSQ/balances\"},\"transactions\":{\"href\":\"https://api.labanquepostale.com/v1/accounts/ule-Q01ezzJWDTC24_6_WlFCuq2DMODy2VTiaBzWNYsMza2HPnWJN8bgvHJ9QYSQ/transactions\"}},\"resourceId\":\"ule-Q01ezzJWDTC24_6_WlFCuq2DMODy2VTiaBzWNYsMza2HPnWJN8bgvHJ9QYSQ\",\"accountId\":{\"other\":{\"identification\":\"4970********3655\",\"schemeName\":\"CPAN\",\"issuer\":\"\"},\"currency\":\"EUR\"},\"name\":\"VALENTIN/FLORENCE.MLLE-2021-06-30T00:00:00\",\"details\":\"Carte à débit différé\",\"product\":\"CARTE VISA A DEBIT DIFFERE\",\"linkedAccount\":\"6099467L020\",\"usage\":\"PRIV\",\"cashAccountType\":\"CARD\",\"balances\":[{\"balanceAmount\":{\"amount\":\"-190.79\",\"currency\":\"EUR\"},\"balanceType\":\"OTHR\",\"name\":\"L’encours de votre carte\"}]}]}";

    private LaBanquePostaleCreditCardConverter converter;

    @Before
    public void setUp() {
        converter = new LaBanquePostaleCreditCardConverter();
    }

    @Test
    public void shouldMapCreditCard() {
        // given
        AccountResponse accountResponse =
                SerializationUtils.deserializeFromString(BALANCES_RESPONSE, AccountResponse.class);

        AccountEntity expectedAccount = accountResponse.getAccounts().get(0);
        ExactCurrencyAmount expectedAccountBalance =
                expectedAccount.getBalances().get(0).getBalanceAmount().toAmount();

        // when
        CreditCardAccount result = converter.toTinkCreditCard(accountResponse.getAccounts().get(0));

        // then
        assertThat(result.getCardModule().getCardNumber())
                .isEqualTo(expectedAccount.getAccountId().getOther().getIdentification());
        assertThat(result.getCardModule().getCardAlias()).isEqualTo(expectedAccount.getName());
        assertThat(result.getAccountNumber()).isEqualTo(expectedAccount.getLinkedAccount());
        assertThat(result.getExactBalance()).isEqualTo(expectedAccountBalance);
    }
}
