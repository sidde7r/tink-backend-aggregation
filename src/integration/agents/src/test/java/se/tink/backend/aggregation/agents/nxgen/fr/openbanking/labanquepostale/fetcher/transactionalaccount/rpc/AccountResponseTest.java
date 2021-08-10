package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.entities.AccountEntity;

public class AccountResponseTest {

    private static final String ACCOUNT_RESPONSE_BODY =
            "{\"_links\":{\"self\":{\"href\":\"https://api.labanquepostale.com/v1/accounts\"}},\"accounts\":[{\"_links\":{\"balances\":{\"href\":\"https://api.labanquepostale.com/v1/accounts/6099467L020/balances\"},\"transactions\":{\"href\":\"https://api.labanquepostale.com/v1/accounts/6099467L020/transactions\"}},\"resourceId\":\"6099467L020\",\"accountId\":{\"iban\":\"FR3120041000016099467L02051\",\"currency\":\"EUR\"},\"name\":\"MR BARDIAU OU MLE VALENTIN\",\"usage\":\"PRIV\",\"cashAccountType\":\"CACC\"},{\"_links\":{\"balances\":{\"href\":\"https://api.labanquepostale.com/v1/accounts/ule-Q01ezzJWDTC24_6_WlFCuq2DMODy2VTiaBzWNYsMza2HPnWJN8bgvHJ9QYSQ/balances\"},\"transactions\":{\"href\":\"https://api.labanquepostale.com/v1/accounts/ule-Q01ezzJWDTC24_6_WlFCuq2DMODy2VTiaBzWNYsMza2HPnWJN8bgvHJ9QYSQ/transactions\"}},\"resourceId\":\"ule-Q01ezzJWDTC24_6_WlFCuq2DMODy2VTiaBzWNYsMza2HPnWJN8bgvHJ9QYSQ\",\"accountId\":{\"other\":{\"identification\":\"4970********3655\",\"schemeName\":\"CPAN\",\"issuer\":\"\"},\"currency\":\"EUR\"},\"name\":\"VALENTIN/FLORENCE.MLLE-2021-06-30T00:00:00\",\"details\":\"Carte à débit différé\",\"product\":\"CARTE VISA A DEBIT DIFFERE\",\"linkedAccount\":\"6099467L020\",\"usage\":\"PRIV\",\"cashAccountType\":\"CARD\"}]}";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void shouldMapCheckingAccountAndCreditCardAccountId() throws JsonProcessingException {
        // when
        AccountResponse response =
                objectMapper.readValue(ACCOUNT_RESPONSE_BODY, AccountResponse.class);

        // then
        Assertions.assertThat(response.getAccounts()).hasSize(2);
        AccountEntity checkingAccountEntity = response.getAccounts().get(0);
        Assertions.assertThat(checkingAccountEntity.getAccountId().getIban())
                .isEqualTo("FR3120041000016099467L02051");
        Assertions.assertThat(checkingAccountEntity.getAccountId().getCurrency()).isEqualTo("EUR");
        Assertions.assertThat(checkingAccountEntity.getAccountId().getOther()).isNull();

        AccountEntity cardAccountEntity = response.getAccounts().get(1);
        Assertions.assertThat(cardAccountEntity.getAccountId().getOther().getIdentification())
                .isEqualTo("4970********3655");
        Assertions.assertThat(cardAccountEntity.getAccountId().getOther().getSchemeName())
                .isEqualTo("CPAN");
        Assertions.assertThat(cardAccountEntity.getAccountId().getCurrency()).isEqualTo("EUR");
        Assertions.assertThat(cardAccountEntity.getAccountId().getIban()).isNull();
    }
}
