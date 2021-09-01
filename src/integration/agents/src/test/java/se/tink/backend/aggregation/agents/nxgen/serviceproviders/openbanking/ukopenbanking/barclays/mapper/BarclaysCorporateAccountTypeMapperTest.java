package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.barclays.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.rpc.ProductV31Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.ProductFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.AccountTypeMapper;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays.mapper.BarclaysCorporateAccountTypeMapper;

@RunWith(MockitoJUnitRunner.class)
public class BarclaysCorporateAccountTypeMapperTest {

    private static final String ACCOUNT_ID = "AccountID";
    @Mock private AccountEntity accountEntity;
    @Mock private ProductFetcher productFetcher;
    private AccountTypeMapper accountTypeMapper;

    @Before
    public void setUp() throws Exception {
        this.accountTypeMapper = new BarclaysCorporateAccountTypeMapper(productFetcher);
    }

    @Test
    public void shouldRecognizeCheckingAccount() {
        // given
        when(accountEntity.getAccountId()).thenReturn(ACCOUNT_ID);
        when(accountEntity.getRawAccountSubType()).thenReturn(null);
        ProductV31Response product = mock(ProductV31Response.class);
        when(productFetcher.fetchProduct(ACCOUNT_ID)).thenReturn(Optional.of(product));
        when(product.getAccountProductType(ACCOUNT_ID)).thenReturn("BusinessCurrentAccount");

        // when
        AccountTypes accountType = accountTypeMapper.getAccountType(accountEntity);

        // then
        assertThat(accountType).isEqualTo(AccountTypes.CHECKING);
    }

    @Test
    public void shouldRecognizeSavingAccount() {
        // given
        when(accountEntity.getAccountId()).thenReturn(ACCOUNT_ID);
        when(accountEntity.getRawAccountSubType()).thenReturn("Savings");

        // when
        AccountTypes accountType = accountTypeMapper.getAccountType(accountEntity);

        // then
        assertThat(accountType).isEqualTo(AccountTypes.SAVINGS);
    }

    @Test
    public void shouldRecognizeCreditCardAccount() {
        // given
        when(accountEntity.getAccountId()).thenReturn(ACCOUNT_ID);
        ProductV31Response product = mock(ProductV31Response.class);
        when(productFetcher.fetchProduct(ACCOUNT_ID)).thenReturn(Optional.of(product));
        when(product.getAccountProductType(ACCOUNT_ID)).thenReturn("CommercialCreditCard");

        // when
        AccountTypes accountType = accountTypeMapper.getAccountType(accountEntity);

        // then
        assertThat(accountType).isEqualTo(AccountTypes.CREDIT_CARD);
    }

    @Test
    public void shouldThrowExceptionWhenAppearedUnexpectedProductType() {
        // given
        when(accountEntity.getAccountId()).thenReturn(ACCOUNT_ID);
        when(accountEntity.getRawAccountSubType()).thenReturn(null);
        ProductV31Response product = mock(ProductV31Response.class);
        when(productFetcher.fetchProduct(ACCOUNT_ID)).thenReturn(Optional.of(product));
        when(product.getAccountProductType(ACCOUNT_ID)).thenReturn("Other");

        // when
        ThrowingCallable throwingCallable = () -> accountTypeMapper.getAccountType(accountEntity);

        // then
        assertThatThrownBy(throwingCallable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unexpected product type has appeared");
    }
}
