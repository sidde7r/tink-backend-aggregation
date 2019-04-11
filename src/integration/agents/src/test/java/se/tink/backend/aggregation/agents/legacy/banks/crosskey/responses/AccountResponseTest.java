package se.tink.backend.aggregation.agents.banks.crosskey.responses;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import org.junit.Test;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.banks.crosskey.PaginationTypes;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

public class AccountResponseTest {
    private static final String ACCOUNT_RESPONSE_JSON =
            "{\"accountNumber\":\"SE123456\",\"accountId\":\"abc123\",\"accountNickname\":\"2311 10 101 10\",\"currency\":\"SEK\",\"availableAmount\":-2000000.00,\"balance\":-2000000.00,\"creditLimit\":null,\"interestRate\":1.6500,\"trueInterestRate\":null,\"interestMargin\":0,\"capitalization\":\"\",\"minInterestRate\":0,\"maxInterestRate\":0,\"referenceInterestName\":\"\",\"referenceInterestValue\":0,\"interestLadder\":[],\"accountType\":\"506\",\"accountTypeName\":\"LÅN FAST RÄNTA - BULLET\",\"accountGroup\":\"loan\",\"accountSubGroup\":null,\"bban\":\"23111010110\",\"bbanFormatted\":\"2311 10 101 10\",\"bic\":\"AABASESS\",\"payableAccount\":false,\"transferable\":false,\"defaultAccount\":false,\"showAccount\":true,\"reservations\":false,\"reservationAmount\":0,\"accountOwnerName\":\"FIRST LAST\",\"accountCoOwnerName\":\"PARTNER PERHAPS\",\"moreOwnersThanTwo\":false,\"dueDate\":null,\"grossInterestAmount\":null,\"netInterestAmount\":null,\"interestTaxAmount\":null,\"receiverAccount\":null,\"owner\":true,\"softLocked\":false,\"pledged\":false,\"allowedAsDefaultAccount\":true,\"usageType\":null,\"usageText\":null}";

    @Test
    public void verifyToStringOutput_containsNonSensitiveInfoOnly() throws IOException {
        AccountResponse accountResponse =
                new ObjectMapper().readValue(ACCOUNT_RESPONSE_JSON, AccountResponse.class);
        String toStringValue = accountResponse.toString();

        assertThat(toStringValue)
                .doesNotContain("SE123456")
                .doesNotContain("abc123")
                .doesNotContain("2311 10 101 10")
                .doesNotContain("23111010110")
                .doesNotContain("FIRST")
                .doesNotContain("LAST")
                .doesNotContain("PARTNER")
                .doesNotContain("PERHAPS")
                .doesNotContain("-2000000.00")
                .contains("=SEK")
                .contains("=1.65")
                .contains("=506")
                .contains("=LÅN FAST RÄNTA - BULLET")
                .contains("=loan");
    }

    @Test
    public void testToTinkAccount() throws Exception {

        AccountResponse response = createAccountResponse("2310 23 493 78");
        Account account = runToTinkAccount(response);

        defaultValidationFor(account);

        assertThat(account.getName()).isEqualTo("Sparkonto");
    }

    @Test
    public void testToTinkAccount2() throws Exception {
        AccountResponse response = createAccountResponse("SE5023000000023100379401");
        Account account = runToTinkAccount(response);

        defaultValidationFor(account);

        assertThat(account.getName()).isEqualTo("Sparkonto");
    }

    @Test
    public void testToTinkAccount3() throws Exception {
        AccountResponse response = createAccountResponse("Mitt konto");
        Account account = runToTinkAccount(response);

        defaultValidationFor(account);

        assertThat(account.getName()).isEqualTo("Mitt konto");
    }

    public Account runToTinkAccount(AccountResponse response) {
        CrossKeyConfig config = mock(CrossKeyConfig.class);
        createStaticReturnValuesFor(config);

        return response.toTinkAccount(config);
    }

    public void defaultValidationFor(Account account) {
        assertThat(account.getAccountNumber()).isEqualTo("2310 23 493 78");
        assertThat(account.getBalance()).isEqualTo(10.00);
        assertThat(account.getAvailableCredit()).isEqualTo(10.00);
        assertThat(account.getIdentifiers().size()).isEqualTo(2);
        assertThat(account.getType()).isEqualTo(AccountTypes.CHECKING);
        assertThat(account.getBankId()).isEqualTo("45301f93438a8b5bc7e3de0162j160b206653fed");
    }

    public List<AccountIdentifier> createIdentifiers() {
        List<AccountIdentifier> identifiers = Lists.newArrayList();

        identifiers.add(new SwedishIdentifier("92352240367"));
        identifiers.add(new IbanIdentifier("AABASESS", "SE3423000000023100379398"));

        return identifiers;
    }

    public AccountResponse createAccountResponse(String nickName) {
        AccountResponse accountResponse = new AccountResponse();
        accountResponse.setBalance(10.00);
        accountResponse.setAvailableAmount(10.00);
        accountResponse.setBban("23102349378");
        accountResponse.setBbanFormatted("2310 23 493 78");
        accountResponse.setBic("AABASESS");
        accountResponse.setAccountNumber("SE5023000000023100379401");
        accountResponse.setAccountId("45301f93438a8b5bc7e3de0162j160b206653fed");
        accountResponse.setAccountNickname(nickName);
        accountResponse.setAccountTypeName("Sparkonto");
        accountResponse.setAccountGroup("check");
        accountResponse.setAccountType(414);
        accountResponse.setUsageType("");

        return accountResponse;
    }

    public void createStaticReturnValuesFor(CrossKeyConfig config) {
        when(config.getAccountType(any(String.class), any(String.class)))
                .thenReturn(AccountTypes.CHECKING);
        when(config.getIdentifiers(any(String.class), any(String.class), any(String.class)))
                .thenReturn(createIdentifiers());
        when(config.getPaginationType()).thenReturn(PaginationTypes.DATE);
    }
}
