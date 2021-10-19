package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.rpc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.entities.AccountIdentificationEntity;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AccountResponseTest {

    private static final String ACCOUNT_RESPONSE_BODY =
            "{\"_links\":{\"self\":{\"href\":\"https://api.labanquepostale.com/v1/accounts\"}},\"accounts\":[{\"_links\":{\"balances\":{\"href\":\"https://api.labanquepostale.com/v1/accounts/6099467L020/balances\"},\"transactions\":{\"href\":\"https://api.labanquepostale.com/v1/accounts/6099467L020/transactions\"}},\"resourceId\":\"6099467L020\",\"accountId\":{\"iban\":\"FR3120041000016099467L02051\",\"currency\":\"EUR\"},\"name\":\"MR BARDIAU OU MLE VALENTIN\",\"usage\":\"PRIV\",\"cashAccountType\":\"CACC\"},{\"_links\":{\"balances\":{\"href\":\"https://api.labanquepostale.com/v1/accounts/ule-Q01ezzJWDTC24_6_WlFCuq2DMODy2VTiaBzWNYsMza2HPnWJN8bgvHJ9QYSQ/balances\"},\"transactions\":{\"href\":\"https://api.labanquepostale.com/v1/accounts/ule-Q01ezzJWDTC24_6_WlFCuq2DMODy2VTiaBzWNYsMza2HPnWJN8bgvHJ9QYSQ/transactions\"}},\"resourceId\":\"ule-Q01ezzJWDTC24_6_WlFCuq2DMODy2VTiaBzWNYsMza2HPnWJN8bgvHJ9QYSQ\",\"accountId\":{\"other\":{\"identification\":\"4970********3655\",\"schemeName\":\"CPAN\",\"issuer\":\"\"},\"currency\":\"EUR\"},\"name\":\"VALENTIN/FLORENCE.MLLE-2021-06-30T00:00:00\",\"details\":\"Carte à débit différé\",\"product\":\"CARTE VISA A DEBIT DIFFERE\",\"linkedAccount\":\"6099467L020\",\"usage\":\"PRIV\",\"cashAccountType\":\"CARD\"}]}";

    @Test
    public void shouldMapCheckingAccountAndCreditCardAccountId() {
        // when
        AccountResponse response =
                SerializationUtils.deserializeFromString(
                        ACCOUNT_RESPONSE_BODY, AccountResponse.class);

        // then
        assertThat(response.getAccounts()).hasSize(2);
        AccountIdentificationEntity checkingAccountId =
                response.getAccounts().get(0).getAccountId();
        assertThat(checkingAccountId.getIban()).isEqualTo("FR3120041000016099467L02051");
        assertThat(checkingAccountId.getCurrency()).isEqualTo("EUR");
        assertThat(checkingAccountId.getOther()).isNull();

        AccountIdentificationEntity creditCardAccountId =
                response.getAccounts().get(1).getAccountId();
        assertThat(creditCardAccountId.getOther().getIdentification())
                .isEqualTo("4970********3655");
        assertThat(creditCardAccountId.getOther().getSchemeName()).isEqualTo("CPAN");
        assertThat(creditCardAccountId.getCurrency()).isEqualTo("EUR");
        assertThat(creditCardAccountId.getIban()).isNull();
    }
}
