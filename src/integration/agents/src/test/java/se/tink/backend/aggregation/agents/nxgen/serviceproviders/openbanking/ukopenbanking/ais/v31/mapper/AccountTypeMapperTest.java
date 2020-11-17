package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountOwnershipType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;

public class AccountTypeMapperTest {

    private AccountTypeMapper accountTypeMapper;
    private AccountEntity accountEntity;
    private UkOpenBankingAisConfig aisConfig;
    private static final String PERSONAL_TYPE = "Personal";
    private static final String BUSINESS_TYPE = "Business";
    private static final String UNKNOWN_TYPE = "Unknown";

    @Before
    public void setUp() {
        aisConfig = mock(UkOpenBankingAisConfig.class);
        accountEntity = mock(AccountEntity.class);
        accountTypeMapper = new AccountTypeMapper(aisConfig);
    }

    @Test
    public void shouldRecognizePersonalAccountOwnership() {
        when(accountEntity.getRawAccountType()).thenReturn(PERSONAL_TYPE);

        assertThat(accountTypeMapper.getAccountOwnershipType(accountEntity))
                .isEqualTo(AccountOwnershipType.PERSONAL);
    }

    @Test
    public void shouldRecognizeBusinessAccountOwnership() {
        when(accountEntity.getRawAccountType()).thenReturn(BUSINESS_TYPE);

        assertThat(accountTypeMapper.getAccountOwnershipType(accountEntity))
                .isEqualTo(AccountOwnershipType.BUSINESS);
    }

    @Test
    public void shouldNotSupportBusinessIfAisConfigIsNotPassed() {
        when(accountEntity.getRawAccountType()).thenReturn(BUSINESS_TYPE);

        assertThat(accountTypeMapper.supportsAccountOwnershipType(accountEntity)).isFalse();
    }

    @Test
    public void shouldNotSupportBusinessIfAisConfigSupportsDifferentOwnership() {
        accountTypeMapper = new AccountTypeMapper(aisConfig);
        when(aisConfig.getAllowedAccountOwnershipType()).thenReturn(AccountOwnershipType.PERSONAL);
        when(accountEntity.getRawAccountType()).thenReturn(BUSINESS_TYPE);

        assertThat(accountTypeMapper.supportsAccountOwnershipType(accountEntity)).isFalse();
    }

    @Test
    public void shouldSupportBusinessIfAisConfigSupportsBusiness() {
        accountTypeMapper = new AccountTypeMapper(aisConfig);
        when(aisConfig.getAllowedAccountOwnershipType()).thenReturn(AccountOwnershipType.BUSINESS);
        when(accountEntity.getRawAccountType()).thenReturn(BUSINESS_TYPE);

        assertThat(accountTypeMapper.supportsAccountOwnershipType(accountEntity)).isTrue();
    }

    @Test
    public void shouldSupportPersonalIfAisConfigSupportsPersonal() {
        when(aisConfig.getAllowedAccountOwnershipType()).thenReturn(AccountOwnershipType.PERSONAL);
        accountTypeMapper = new AccountTypeMapper(aisConfig);
        when(accountEntity.getRawAccountType()).thenReturn(PERSONAL_TYPE);

        assertThat(accountTypeMapper.supportsAccountOwnershipType(accountEntity)).isTrue();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfUnknownAccountType() {
        when(accountEntity.getRawAccountType()).thenReturn(UNKNOWN_TYPE);

        accountTypeMapper.getAccountType(accountEntity);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfUnknownAccountOwnershipType() {
        when(accountEntity.getRawAccountSubType()).thenReturn(UNKNOWN_TYPE);

        accountTypeMapper.getAccountOwnershipType(accountEntity);
    }

    @Test
    public void shouldRecognizeAccountType() {
        when(accountEntity.getRawAccountSubType()).thenReturn("Loan");

        assertThat(accountTypeMapper.getAccountType(accountEntity)).isEqualTo(AccountTypes.LOAN);
    }
}
