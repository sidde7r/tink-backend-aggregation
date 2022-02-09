package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountEntity;

public class AccountTypeMapperTest {

    private static final String UNKNOWN_TYPE = "Unknown";
    private AccountEntity accountEntity;

    @Before
    public void setUp() {
        accountEntity = mock(AccountEntity.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfUnknownAccountType() {
        when(accountEntity.getRawAccountType()).thenReturn(UNKNOWN_TYPE);

        AccountTypeMapper.getAccountType(accountEntity);
    }

    @Test
    public void shouldRecognizeAccountTypeLoan() {
        when(accountEntity.getRawAccountSubType()).thenReturn("Loan");

        assertThat(AccountTypeMapper.getAccountType(accountEntity)).isEqualTo(AccountTypes.LOAN);
    }

    @Test
    public void shouldRecognizeAccountTypeMortgage() {
        when(accountEntity.getRawAccountSubType()).thenReturn("Mortgage");

        assertThat(AccountTypeMapper.getAccountType(accountEntity))
                .isEqualTo(AccountTypes.MORTGAGE);
    }

    @Test
    public void shouldRecognizeAccountTypeCurrent() {
        when(accountEntity.getRawAccountSubType()).thenReturn("CurrentAccount");

        assertThat(AccountTypeMapper.getAccountType(accountEntity))
                .isEqualTo(AccountTypes.CHECKING);
    }

    @Test
    public void shouldRecognizeAccountTypeEMoney() {
        when(accountEntity.getRawAccountSubType()).thenReturn("EMoney");

        assertThat(AccountTypeMapper.getAccountType(accountEntity))
                .isEqualTo(AccountTypes.CHECKING);
    }

    @Test
    public void shouldRecognizeAccountTypePrePaid() {
        when(accountEntity.getRawAccountSubType()).thenReturn("PrePaidCard");

        assertThat(AccountTypeMapper.getAccountType(accountEntity))
                .isEqualTo(AccountTypes.CREDIT_CARD);
    }

    @Test
    public void shouldRecognizeAccountTypeChargeCard() {
        when(accountEntity.getRawAccountSubType()).thenReturn("ChargeCard");

        assertThat(AccountTypeMapper.getAccountType(accountEntity))
                .isEqualTo(AccountTypes.CREDIT_CARD);
    }

    @Test
    public void shouldRecognizeAccountTypeCreditCard() {
        when(accountEntity.getRawAccountSubType()).thenReturn("CreditCard");

        assertThat(AccountTypeMapper.getAccountType(accountEntity))
                .isEqualTo(AccountTypes.CREDIT_CARD);
    }

    @Test
    public void shouldRecognizeAccountTypeSavings() {
        when(accountEntity.getRawAccountSubType()).thenReturn("Savings");

        assertThat(AccountTypeMapper.getAccountType(accountEntity)).isEqualTo(AccountTypes.SAVINGS);
    }
}
