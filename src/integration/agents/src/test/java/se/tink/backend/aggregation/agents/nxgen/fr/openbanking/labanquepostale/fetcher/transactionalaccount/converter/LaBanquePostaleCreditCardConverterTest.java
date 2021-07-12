package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class LaBanquePostaleCreditCardConverterTest {

    private static final String BALANCES_RESPONSE =
            "{\"_links\":{\"self\":{\"href\":\"https://api.labanquepostale.com/v1/accounts\"}},\"accounts\":[{\"_links\":{\"balances\":{\"href\":\"https://api.labanquepostale.com/v1/accounts/ule-Q01ezzJWDTC24_6_WlFCuq2DMODy2VTiaBzWNYsMza2HPnWJN8bgvHJ9QYSQ/balances\"},\"transactions\":{\"href\":\"https://api.labanquepostale.com/v1/accounts/ule-Q01ezzJWDTC24_6_WlFCuq2DMODy2VTiaBzWNYsMza2HPnWJN8bgvHJ9QYSQ/transactions\"}},\"resourceId\":\"ule-Q01ezzJWDTC24_6_WlFCuq2DMODy2VTiaBzWNYsMza2HPnWJN8bgvHJ9QYSQ\",\"accountId\":{\"other\":{\"identification\":\"4970********3655\",\"schemeName\":\"CPAN\",\"issuer\":\"\"},\"currency\":\"EUR\"},\"name\":\"VALENTIN/FLORENCE.MLLE-2021-06-30T00:00:00\",\"details\":\"Carte à débit différé\",\"product\":\"CARTE VISA A DEBIT DIFFERE\",\"linkedAccount\":\"6099467L020\",\"usage\":\"PRIV\",\"cashAccountType\":\"CARD\",\"balances\":[{\"balanceAmount\":{\"amount\":\"-190.79\",\"currency\":\"EUR\"},\"balanceType\":\"OTHR\",\"name\":\"L’encours de votre carte\"}]}]}";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void shouldMapCreditCard() throws JsonProcessingException {
        // given
        AccountResponse accountResponse =
                objectMapper.readValue(BALANCES_RESPONSE, AccountResponse.class);
        AccountEntity expectedAccount = accountResponse.getAccounts().get(0);
        ExactCurrencyAmount expectedAccountBalance =
                expectedAccount.getBalances().get(0).getBalanceAmount().toAmount();

        // when
        CreditCardAccount result =
                LaBanquePostaleCreditCardConverter.toTinkCreditCard(
                        accountResponse.getAccounts().get(0));

        // then
        Assert.assertEquals(
                expectedAccount.getAccountId().getOther().getIdentification(),
                result.getCardModule().getCardNumber());
        Assert.assertEquals(expectedAccount.getName(), result.getCardModule().getCardAlias());
        Assert.assertEquals(expectedAccount.getLinkedAccount(), result.getAccountNumber());
        Assert.assertEquals(expectedAccountBalance, result.getExactBalance());
    }
}
