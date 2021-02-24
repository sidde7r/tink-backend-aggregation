package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountInterestDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.InterestDetailEntity;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails.Type;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AccountEntityMapperTest {

    private static final String ACCOUNT_NO_EXT = "123234345";
    private static final String ACCOUNT_NO_INT = "567678789";

    private AccountEntity accountEntity;
    private AccountEntityMapper accountEntityMapper;

    @Before
    public void setUp() {
        accountEntityMapper = new AccountEntityMapper("SE");
        accountEntity =
                SerializationUtils.deserializeFromString(
                        "{\n"
                                + "    \"currency\": \"PLN\",\n"
                                + "    \"accountName\": \"sample account name\",\n"
                                + "    \"accountProduct\": \"sample account product\",\n"
                                + "    \"accountNoExt\": \""
                                + ACCOUNT_NO_EXT
                                + "\",\n"
                                + "    \"accountNoInt\": \""
                                + ACCOUNT_NO_INT
                                + "\",\n"
                                + "    \"balance\": -1234.45\n"
                                + "}",
                        AccountEntity.class);
    }

    @Test
    public void toLoanAccountWhenAccountProductIsMortgageInAgentsConfiguration() {
        // given
        Map<String, Type> accountProductToTypeMapping = new HashMap<>();
        accountProductToTypeMapping.put("sample account product", LoanDetails.Type.MORTGAGE);
        // and
        DanskeBankConfiguration configuration =
                danskeBankConfiguration(accountProductToTypeMapping);

        // and
        InterestDetailEntity interestDetailEntity = new InterestDetailEntity();
        interestDetailEntity.setRateInPercent("1.100");

        AccountInterestDetailsEntity accountInterestDetailsEntity =
                new AccountInterestDetailsEntity();
        accountInterestDetailsEntity.setInterestDetails(
                Collections.singletonList(interestDetailEntity));

        AccountDetailsResponse accountDetailsResponse = new AccountDetailsResponse();
        accountDetailsResponse.setAccountOwners(Collections.singletonList("12345 - ACCOUNT OWNER"));
        accountDetailsResponse.setAccountType("Home Loan");
        accountDetailsResponse.setAccountInterestDetails(accountInterestDetailsEntity);

        // when
        LoanAccount result =
                accountEntityMapper.toLoanAccount(
                        configuration, accountEntity, accountDetailsResponse);

        // then
        assertThat(result.getDetails().getType()).isEqualTo(LoanDetails.Type.MORTGAGE);
        // and
        assertThat(result.getName()).isEqualTo("sample account name");
        assertThat(result.getAccountNumber()).isEqualTo(ACCOUNT_NO_EXT);
        assertThat(result.getApiIdentifier()).isEqualTo(ACCOUNT_NO_INT);
        assertThat(result.getExactBalance()).isEqualTo(ExactCurrencyAmount.of(-1234.45, "PLN"));
        assertThat(result.getDetails().getLoanNumber()).isEqualTo(ACCOUNT_NO_EXT);
        assertThat(result.getIdModule().getProductName()).isEqualTo("Home Loan");
        assertThat(result.getInterestRate()).isEqualTo(0.0110);
        assertThat(result.getHolderName().toString()).isEqualTo("ACCOUNT OWNER");
        assertThat(result.getParties().size()).isEqualTo(1);
    }

    @Test
    public void toLoanAccountWhenAccountProductIsNotMortgageInAgentsConfiguration() {
        // given
        DanskeBankConfiguration configuration = danskeBankConfiguration();

        // when
        LoanAccount result =
                accountEntityMapper.toLoanAccount(
                        configuration, accountEntity, new AccountDetailsResponse());

        // then
        assertThat(result.getDetails().getType()).isEqualTo(LoanDetails.Type.DERIVE_FROM_NAME);
        // and
        assertThat(result.getName()).isEqualTo("sample account name");
        assertThat(result.getAccountNumber()).isEqualTo(ACCOUNT_NO_EXT);
        assertThat(result.getApiIdentifier()).isEqualTo(ACCOUNT_NO_INT);
        assertThat(result.getExactBalance()).isEqualTo(ExactCurrencyAmount.of(-1234.45, "PLN"));
        assertThat(result.getDetails().getLoanNumber()).isEqualTo(ACCOUNT_NO_EXT);
        assertThat(result.getIdModule().getProductName()).isNull();
        assertThat(result.getInterestRate()).isEqualTo(0.0);
        assertThat(result.getHolderName()).isNull();
        assertThat(result.getParties().size()).isEqualTo(0);
    }

    private DanskeBankConfiguration danskeBankConfiguration() {
        return danskeBankConfiguration(new HashMap<>());
    }

    private DanskeBankConfiguration danskeBankConfiguration(Map<String, Type> loanAccountTypes) {
        DanskeBankConfiguration configuration = mock(DanskeBankConfiguration.class);
        given(configuration.getLoanAccountTypes()).willReturn(loanAccountTypes);
        given(configuration.canExecuteExternalTransfer(anyString()))
                .willReturn(AccountCapabilities.Answer.UNKNOWN);
        given(configuration.canReceiveExternalTransfer(anyString()))
                .willReturn(AccountCapabilities.Answer.UNKNOWN);
        given(configuration.canPlaceFunds(anyString()))
                .willReturn(AccountCapabilities.Answer.UNKNOWN);
        given(configuration.canWithdrawCash(anyString()))
                .willReturn(AccountCapabilities.Answer.UNKNOWN);
        return configuration;
    }
}
