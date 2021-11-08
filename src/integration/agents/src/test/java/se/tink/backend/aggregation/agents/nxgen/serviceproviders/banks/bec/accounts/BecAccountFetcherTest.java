package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.BecAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BecAccountFetcherTest {

    private BecApiClient apiClient;
    private BecAccountFetcher becAccountFetcher;

    @Before
    public void setup() {
        apiClient = mock(BecApiClient.class);
        becAccountFetcher = new BecAccountFetcher(apiClient);
    }

    @Test
    public void shouldFetchAccounts() {
        when(apiClient.fetchAccounts()).thenReturn(getFetchAccountResponse());
        when(apiClient.fetchAccountDetails(anyString())).thenReturn(getAccountDetailsResponse());

        Collection<TransactionalAccount> transactionalAccounts = becAccountFetcher.fetchAccounts();

        assertThat(transactionalAccounts).hasSize(1);
        TransactionalAccount transactionalAccount = transactionalAccounts.iterator().next();
        assertThat(transactionalAccount.getType()).isEqualTo(AccountTypes.CHECKING);
        assertThat(transactionalAccount.getUniqueIdentifier()).isEqualTo("11112222333");
        assertThat(transactionalAccount.getAccountNumber()).isEqualTo("11112222333");
        assertThat(transactionalAccount.getName()).isEqualTo("LÃ¸nkonto");
        assertThat(transactionalAccount.getIdentifiers().stream())
                .anyMatch(
                        accountIdentifier ->
                                accountIdentifier.getType() == AccountIdentifierType.IBAN
                                        && "DK5000400440116243"
                                                .equals(accountIdentifier.getIdentifier()));
        assertThat(transactionalAccount.getIdentifiers().stream())
                .anyMatch(
                        accountIdentifier ->
                                accountIdentifier.getType() == AccountIdentifierType.BBAN
                                        && "00400440116243"
                                                .equals(accountIdentifier.getIdentifier()));
        assertThat(transactionalAccount.getExactBalance().getDoubleValue()).isEqualTo(20000.01);
        assertThat(transactionalAccount.getExactAvailableBalance().getDoubleValue())
                .isEqualTo(9036.97);
        ;
        assertThat(transactionalAccount.getExactAvailableCredit()).isNull();
        assertThat(transactionalAccount.getExactCreditLimit()).isNull();
        assertThat(transactionalAccount.getParties()).hasSize(1);
        assertThat(transactionalAccount.getParties().get(0).getName()).isEqualTo("John Smith");
        assertThat(transactionalAccount.getParties().get(0).getRole()).isEqualTo(Party.Role.HOLDER);
    }

    private FetchAccountResponse getFetchAccountResponse() {
        return SerializationUtils.deserializeFromString(
                "[\n"
                        + "  {\n"
                        + "    \"accountId\": \"11112222333\",\n"
                        + "    \"accountName\": \"LÃ¸nkonto\",\n"
                        + "    \"balance\": 20000.01,\n"
                        + "    \"balanceTxt\": \"20000.01\",\n"
                        + "    \"dateLastRecord\": \"2021-11-09\",\n"
                        + "    \"maximum\": 30000.00,\n"
                        + "    \"maximumTxt\": \"30.000,00\",\n"
                        + "    \"currency\": \"DKK\",\n"
                        + "    \"accountAuthCode\": 4,\n"
                        + "    \"isStdFraKonto\": true,\n"
                        + "    \"hasExpenditureOverview\": true,\n"
                        + "    \"userAccountRole\": \"1\",\n"
                        + "    \"primaryOwner\": \"John Smith\",\n"
                        + "    \"isNemKonto\": true,\n"
                        + "    \"availableAmount\": 9036.97,\n"
                        + "    \"availableAmountTxt\": \"9.036,97\"\n"
                        + "  }\n"
                        + "]",
                FetchAccountResponse.class);
    }

    private AccountDetailsResponse getAccountDetailsResponse() {

        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"grantedOverdraft\": 0,\n"
                        + "  \"grantedOverdraftTxt\": \"\",\n"
                        + "  \"grantedOverdraftDueDate\": \"\",\n"
                        + "  \"nemKonto\": true,\n"
                        + "  \"stdAccount\": true,\n"
                        + "  \"iban\": \"DK5000400440116243\",\n"
                        + "  \"accountType\": \"Tjek-konto\",\n"
                        + "  \"accountName\": \"LÃ¸nkonto\",\n"
                        + "  \"accountId\": \"11112222333\",\n"
                        + "  \"accountHolder\": \"John Smith\",\n"
                        + "  \"customerId\": \"111111\",\n"
                        + "  \"maxAmount\": 30000.00,\n"
                        + "  \"maxAmountTxt\": \"30.000,00\",\n"
                        + "  \"swift\": \"ALBADKKK\",\n"
                        + "  \"currency\": \"DKK\",\n"
                        + "  \"notYetDeducted\": 0,\n"
                        + "  \"notYetDeductedTxt\": \"0,00\"\n"
                        + "}",
                AccountDetailsResponse.class);
    }
}
