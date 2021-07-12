package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class LaBanquePostaleCreditCardConverterTest {

    private static final String BALANCES_RESPONSE =
            "{\"_links\":{\"self\":{\"href\":\"https://api.labanquepostale.com/v1/accounts\"}},\"accounts\":[{\"_links\":{\"balances\":{\"href\":\"https://api.labanquepostale.com/v1/accounts/ule-Q01ezzJWDTC24_6_WlFCuq2DMODy2VTiaBzWNYsMza2HPnWJN8bgvHJ9QYSQ/balances\"},\"transactions\":{\"href\":\"https://api.labanquepostale.com/v1/accounts/ule-Q01ezzJWDTC24_6_WlFCuq2DMODy2VTiaBzWNYsMza2HPnWJN8bgvHJ9QYSQ/transactions\"}},\"resourceId\":\"ule-Q01ezzJWDTC24_6_WlFCuq2DMODy2VTiaBzWNYsMza2HPnWJN8bgvHJ9QYSQ\",\"accountId\":{\"other\":{\"identification\":\"4970********3655\",\"schemeName\":\"CPAN\",\"issuer\":\"\"},\"currency\":\"EUR\"},\"name\":\"VALENTIN/FLORENCE.MLLE-2021-06-30T00:00:00\",\"details\":\"Carte à débit différé\",\"product\":\"CARTE VISA A DEBIT DIFFERE\",\"linkedAccount\":\"6099467L020\",\"usage\":\"PRIV\",\"cashAccountType\":\"CARD\",\"balances\":[{\"balanceAmount\":{\"amount\":\"-190.79\",\"currency\":\"EUR\"},\"balanceType\":\"OTHR\",\"name\":\"L’encours de votre carte\"}]}]}";

    private LaBanquePostaleCreditCardConverter objectUnderTest;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void shouldMapCreditCard() throws JsonProcessingException {
        // given
        AccountResponse accountResponse =
                objectMapper.readValue(BALANCES_RESPONSE, AccountResponse.class);

        // when
        CreditCardAccount result =
                LaBanquePostaleCreditCardConverter.toTinkCreditCard(
                        accountResponse.getAccounts().get(0));

        // then
        Assert.assertEquals("4970********3655", result.getCardModule().getCardNumber());
        Assert.assertEquals(
                "VALENTIN/FLORENCE.MLLE-2021-06-30T00:00:00",
                result.getCardModule().getCardAlias());
        Assert.assertEquals("6099467L020", result.getAccountNumber());
        Assert.assertEquals(new BigDecimal("-190.79"), result.getExactBalance().getExactValue());
    }
}
