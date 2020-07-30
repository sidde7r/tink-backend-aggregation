package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsAccountInformation;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HISAL;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HISPA;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HIUPD;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class FinTsTransactionalAccountMapperTest {
    private static final BigDecimal BOOKED_BALANCE = BigDecimal.valueOf(12.0);
    private static final String CURRENCY = "EUR";
    private static final String BLZ = "50010517";
    private static final String PRODUCT_NAME = "Test Product";
    private static final int CHECKING_ACCOUNT_ID = 1;
    private static final String IBAN = "INGDDEFFXXXDE87500105175425728161";
    private static final String ACCOUNT_NUMBER = "5425728161";
    private static final String FIRST_HOLDER = "First Account Holder";
    private static final String SECOND_HOLDER = "Second Account Holder";

    private FinTsTransactionalAccountMapper mapper;
    private HisalBalance hisalBalance;

    @Before
    public void setUp() {
        hisalBalance = mock(HisalBalance.class);
        given(hisalBalance.calculate(any(HISAL.class)))
                .willReturn(BalanceModule.of(ExactCurrencyAmount.of(BOOKED_BALANCE, CURRENCY)));
        mapper = new FinTsTransactionalAccountMapper(hisalBalance);
    }

    @Test
    public void shouldGetProperlyMappedAccount() {
        // given
        FinTsAccountInformation accountInformation = getAccountInformation();

        // when
        Optional<TransactionalAccount> mappedAccount = mapper.toTinkAccount(accountInformation);

        // then
        assertThat(mappedAccount.isPresent()).isTrue();
        assertAccount(mappedAccount.get(), PRODUCT_NAME, AccountTypes.CHECKING, IBAN);
        // and hisal from account information is used to calculate balance module
        ArgumentCaptor<HISAL> hisalCaptor = ArgumentCaptor.forClass(HISAL.class);
        verify(hisalBalance).calculate(hisalCaptor.capture());
        assertThat(hisalCaptor.getValue().getCurrency()).isEqualTo(CURRENCY);
        assertThat(hisalCaptor.getValue().getBookedBalance()).isEqualTo(BOOKED_BALANCE);
    }

    @Test
    public void shouldGetProperlyMappedAccountWhenNoIbanInBasicInfo() {
        // given
        FinTsAccountInformation accountInformation = getAccountInformation();
        accountInformation.getBasicInfo().setIban(null);

        // when
        Optional<TransactionalAccount> mappedAccount = mapper.toTinkAccount(accountInformation);

        // then
        assertThat(mappedAccount.isPresent()).isTrue();
        assertAccount(mappedAccount.get(), PRODUCT_NAME, AccountTypes.CHECKING, IBAN);
    }

    @Test
    public void
            shouldGetProperlyMappedAccountWithAccountNumberAsUniqueIdWhenNoSepaDetailsPresent() {
        // given
        FinTsAccountInformation accountInformation = getAccountInformation();
        accountInformation.getBasicInfo().setIban(null);
        accountInformation.setSepaDetails(null);

        // when
        Optional<TransactionalAccount> mappedAccount = mapper.toTinkAccount(accountInformation);

        // then
        assertThat(mappedAccount.isPresent()).isTrue();
        assertAccount(mappedAccount.get(), PRODUCT_NAME, AccountTypes.CHECKING, ACCOUNT_NUMBER);
    }

    @Test
    public void shouldReturnEmptyOptionalWhenNonTransactionalAccountTypeProvided() {
        // given
        FinTsAccountInformation accountInformation = getAccountInformation();
        accountInformation.setAccountType(AccountTypes.MORTGAGE);

        // when
        Optional<TransactionalAccount> mappedAccount = mapper.toTinkAccount(accountInformation);

        // then
        assertThat(mappedAccount).isNotPresent();
    }

    @Test
    public void shouldReturnEmptyOptionalWhenAccountWithoutBalanceProvided() {
        // given
        FinTsAccountInformation accountInformation = getAccountInformation();
        accountInformation.setBalance(null);

        // when
        Optional<TransactionalAccount> mappedAccount = mapper.toTinkAccount(accountInformation);

        // then
        assertThat(mappedAccount).isNotPresent();
    }

    private void assertAccount(
            TransactionalAccount account,
            String productName,
            AccountTypes accountType,
            String uniqueId) {
        assertThat(account.getType()).isEqualTo(accountType);
        assertThat(account.getHolderName().toString()).isEqualTo(FIRST_HOLDER);
        assertThat(account.getExactBalance())
                .isEqualTo(ExactCurrencyAmount.of(BOOKED_BALANCE, CURRENCY));
        assertThat(account.getIdModule().getAccountNumber()).isEqualTo(ACCOUNT_NUMBER);
        assertThat(account.getIdModule().getUniqueId()).isEqualTo(uniqueId);
        assertThat(account.getIdModule().getProductName()).isEqualTo(productName);
    }

    private FinTsAccountInformation getAccountInformation() {
        HISAL hisal = new HISAL().setCurrency(CURRENCY).setBookedBalance(BOOKED_BALANCE);
        HIUPD hiupd =
                new HIUPD()
                        .setAccountType(CHECKING_ACCOUNT_ID)
                        .setIban(IBAN)
                        .setAccountNumber(ACCOUNT_NUMBER)
                        .setBlz(BLZ)
                        .setProductName(PRODUCT_NAME)
                        .setFirstAccountHolder(FIRST_HOLDER)
                        .setSecondAccountHolder(SECOND_HOLDER);
        HISPA.Detail hispaDetail = new HISPA.Detail().setIban(IBAN);
        return new FinTsAccountInformation(hiupd, AccountTypes.CHECKING)
                .setBalance(hisal)
                .setSepaDetails(hispaDetail);
    }
}
