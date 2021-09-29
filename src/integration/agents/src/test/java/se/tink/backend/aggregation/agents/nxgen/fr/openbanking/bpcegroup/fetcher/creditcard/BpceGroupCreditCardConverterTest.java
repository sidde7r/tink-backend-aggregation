package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.creditcard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.refresh.CreditCardAccountRefreshException;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.creditcard.converter.BpceGroupCreditCardConverter;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transactionalaccount.entity.accounts.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transactionalaccount.entity.accounts.BalanceEntity;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BpceGroupCreditCardConverterTest {

    @Test
    public void shouldMapCreditCardInfoForAccountWithBalance() {

        // given
        AccountEntity accountsResponse = getAccountWithBalance();

        // when
        CreditCardAccount creditCardAccount =
                BpceGroupCreditCardConverter.toCreditCardAccount(accountsResponse);

        // then
        assertThat(creditCardAccount.getParties()).hasSize(1);
        assertThat(creditCardAccount.getParties().get(0).getName()).isEqualTo("Mlle Name Surname");
        assertThat(creditCardAccount.getName()).isEqualTo("Visa Classic");
        assertThat(creditCardAccount.getExactBalance().getDoubleValue()).isEqualTo(610.91);
    }

    @Test
    public void shouldThrowExceptionWhenCardNumberIsMissing() {
        // given
        AccountEntity accountEntity = getAccountWithMissingCreditCardPattern();

        // when
        Throwable throwable =
                catchThrowable(
                        () -> BpceGroupCreditCardConverter.toCreditCardAccount(accountEntity));

        // then
        assertThat(throwable)
                .isInstanceOf(CreditCardAccountRefreshException.class)
                .hasMessage("Cannot determine card number");
    }

    private static AccountEntity getAccountWithBalance() {
        AccountEntity accountEntity = mock(AccountEntity.class);
        when(accountEntity.getBalances()).thenReturn(getBalances());
        when(accountEntity.getName()).thenReturn("MLLE NAME SURNAME XX1234");
        when(accountEntity.getProduct()).thenReturn("Visa Classic");
        when(accountEntity.getResourceId()).thenReturn("068-GFCidentification");
        when(accountEntity.getLinkedAccount()).thenReturn("068-CPT01234567890");
        when(accountEntity.getHolderName()).thenReturn("MLLE NAME SURNAME");
        return accountEntity;
    }

    private static AccountEntity getAccountWithMissingCreditCardPattern() {
        AccountEntity accountEntity = mock(AccountEntity.class);
        when(accountEntity.getBalances()).thenReturn(getBalances());
        when(accountEntity.getName()).thenReturn("MLLE NAME SURNAME");
        return accountEntity;
    }

    private static List<BalanceEntity> getBalances() {
        BalanceEntity balanceEntity1 =
                SerializationUtils.deserializeFromString(
                        "{\n"
                                + "  \"name\": \"Encours\",\n"
                                + "  \"balanceAmount\": {\n"
                                + "    \"currency\": \"EUR\",\n"
                                + "    \"amount\": \"610.91\"\n"
                                + "  },\n"
                                + "  \"balanceType\": \"OTHR\",\n"
                                + "  \"referenceDate\": \"2021-10-05\"\n"
                                + "}",
                        BalanceEntity.class);

        BalanceEntity balanceEntity2 =
                SerializationUtils.deserializeFromString(
                        "{\n"
                                + "  \"name\": \"Dernier encours prélevé\",\n"
                                + "  \"balanceAmount\": {\n"
                                + "    \"currency\": \"EUR\",\n"
                                + "    \"amount\": \"1266.17\"\n"
                                + "  },\n"
                                + "  \"balanceType\": \"OTHR\",\n"
                                + "  \"referenceDate\": \"2021-09-06\"\n"
                                + "}",
                        BalanceEntity.class);

        return Lists.newArrayList(balanceEntity1, balanceEntity2);
    }
}
