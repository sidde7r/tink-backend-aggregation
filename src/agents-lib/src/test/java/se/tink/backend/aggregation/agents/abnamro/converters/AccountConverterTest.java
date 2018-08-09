package se.tink.backend.aggregation.agents.abnamro.converters;

import com.google.common.collect.Lists;
import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.libraries.abnamro.client.model.AmountEntity;
import se.tink.libraries.abnamro.client.model.PfmContractEntity;
import static org.assertj.core.api.Assertions.assertThat;

public class AccountConverterTest {

    @Test
    public void testMappings() {
        PfmContractEntity pfmContract = createPfmContract();

        List<Account> accounts = new AccountConverter().convert(Lists.newArrayList(pfmContract));

        assertThat(!accounts.isEmpty());
        assertThat(accounts.size()).isEqualTo(1);

        Account account = accounts.get(0);

        assertThat(account.getName()).isEqualTo(pfmContract.getName());
        assertThat(account.getBalance()).isEqualTo(pfmContract.getBalance().getAmount());
        assertThat(account.getBankId()).isEqualTo(pfmContract.getContractNumber());
        assertThat(account.getAccountNumber()).isNotNull();
        assertThat(account.getType()).isNotNull();
    }

    @Test
    public void testMappings_whenProductGroupIsNull() {
        PfmContractEntity pfmContract = createPfmContract();
        pfmContract.setProductGroup(null);

        Account account = new AccountConverter().convert(Lists.newArrayList(pfmContract)).get(0);

        assertThat(pfmContract.getProductGroup()).isNull();
        assertThat(account.getType()).isEqualByComparingTo(AccountTypes.CHECKING);
    }

    private static PfmContractEntity createPfmContract() {
        AmountEntity amount = new AmountEntity();
        amount.setAmount(1000D);
        amount.setCurrencyCode("EUR");

        PfmContractEntity entity = new PfmContractEntity();
        entity.setAccountNumber("NLABN4900000000");
        entity.setBalance(amount);
        entity.setName("Foo Bar Account");
        entity.setContractNumber("123456789");
        entity.setProductGroup("PAYMENT_ACCOUNTS");

        return entity;
    }

}
