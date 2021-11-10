package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.creditcards;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.creditcard.BecCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.creditcard.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.creditcard.rpc.CardDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.creditcard.rpc.FetchCardResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BecCardFetcherTest {

    private BecApiClient apiClient;
    private BecCreditCardFetcher becCreditCardFetcher;

    @Before
    public void setup() {
        apiClient = mock(BecApiClient.class);
        becCreditCardFetcher = new BecCreditCardFetcher(apiClient);
    }

    @Test
    public void shouldFetchCreditCards() {
        when(apiClient.fetchAccounts()).thenReturn(getFetchAccountsResponse());
        when(apiClient.fetchCards()).thenReturn(getFetchCreditCardResponse());
        when(apiClient.fetchCardDetails(anyString())).thenReturn(getCreditCardDetailsResponse());
        when(apiClient.fetchAccountDetails(anyString())).thenReturn(getAccountDetailsResponse());

        Collection<CreditCardAccount> creditCardAccounts = becCreditCardFetcher.fetchAccounts();

        assertThat(creditCardAccounts).hasSize(1);
        CreditCardAccount creditCardAccount = creditCardAccounts.iterator().next();
        assertThat(creditCardAccount.getType()).isEqualTo(AccountTypes.CREDIT_CARD);
        assertThat(creditCardAccount.getUniqueIdentifier()).isEqualTo("11112222333");
        assertThat(creditCardAccount.getAccountNumber()).isEqualTo("11112222333");
        assertThat(creditCardAccount.getName()).isEqualTo("Heidi mastercard");
        assertThat(creditCardAccount.getIdentifiers().stream())
                .anyMatch(
                        creditCardIdentifier ->
                                creditCardIdentifier.getType() == AccountIdentifierType.IBAN
                                        && "DK5000400440116243"
                                                .equals(creditCardIdentifier.getIdentifier()));
        assertThat(creditCardAccount.getIdentifiers().stream())
                .anyMatch(
                        creditCardIdentifier ->
                                creditCardIdentifier.getType() == AccountIdentifierType.BBAN
                                        && "00400440116243"
                                                .equals(creditCardIdentifier.getIdentifier()));
        assertThat(creditCardAccount.getExactBalance().getDoubleValue()).isEqualTo(1958.57);
        assertThat(creditCardAccount.getExactAvailableCredit().getDoubleValue()).isEqualTo(30000.0);
        assertThat(creditCardAccount.getExactCreditLimit()).isNull();
        assertThat(creditCardAccount.getParties()).hasSize(1);
        assertThat(creditCardAccount.getParties().get(0).getName()).isEqualTo("John Smith");
        assertThat(creditCardAccount.getParties().get(0).getRole()).isEqualTo(Party.Role.HOLDER);
    }

    private FetchAccountResponse getFetchAccountsResponse() {
        return SerializationUtils.deserializeFromString(
                "[\n"
                        + "  {\n"
                        + "    \"accountId\": \"11112222333\",\n"
                        + "    \"accountName\": \"Heidi mastercard\",\n"
                        + "    \"balance\": 1958.57,\n"
                        + "    \"balanceTxt\": \"1.958,57\",\n"
                        + "    \"dateLastRecord\": \"2021-11-09\",\n"
                        + "    \"maximum\": 0,\n"
                        + "    \"maximumTxt\": \"\",\n"
                        + "    \"currency\": \"DKK\",\n"
                        + "    \"accountAuthCode\": 4,\n"
                        + "    \"isStdFraKonto\": false,\n"
                        + "    \"hasExpenditureOverview\": true,\n"
                        + "    \"userAccountRole\": \"1\",\n"
                        + "    \"primaryOwner\": \"John Smith\",\n"
                        + "    \"isNemKonto\": true,\n"
                        + "    \"availableAmount\": 1958.57,\n"
                        + "    \"availableAmountTxt\": \"1.958,57\"\n"
                        + "  }"
                        + "]",
                FetchAccountResponse.class);
    }

    private List<CardEntity> getFetchCreditCardResponse() {
        return SerializationUtils.deserializeFromString(
                        "{\n"
                                + "  \"cardArray\": [\n"
                                + "    {\n"
                                + "      \"cardName\": \"Visa/Dankort\",\n"
                                + "      \"cardNumber\": \"4571 XXXX XXXX 3109\",\n"
                                + "      \"cardType\": \"K\",\n"
                                + "      \"status\": \"1\",\n"
                                + "      \"statusText\": \"Aktivt\",\n"
                                + "      \"cardId\": \"_aaaaaaaaaaaaabbbbbbbbbbbcccccc\",\n"
                                + "      \"hasGeoSec\": false,\n"
                                + "      \"imageUrl\": \"https://eticket32.prod.bec.dk/grafik/android/xhdpi/small/k_visa.png\",\n"
                                + "      \"isInWallet\": false,\n"
                                + "      \"urlDetails\": \"/mobilbank/kort/detalje?cardId=_aaaaaaaaaaaaabbbbbbbbbbbcccccc&iconType=4&exp=_zzzzzzzzzzzzzb__\",\n"
                                + "      \"canReorder\": false,\n"
                                + "      \"isAddToWalletAvailable\": false\n"
                                + "    }\n"
                                + "  ]\n"
                                + "}",
                        FetchCardResponse.class)
                .getCardArray();
    }

    private CardDetailsResponse getCreditCardDetailsResponse() {

        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"accountNumber\": \"11112222333\",\n"
                        + "    \"cardName\": \"Visa/Dankort\",\n"
                        + "    \"cardNumber\": \"4571 XXXX XXXX 3109\",\n"
                        + "    \"cardType\": \"K\",\n"
                        + "    \"status\": \"1\",\n"
                        + "    \"statusText\": \"Aktivt\",\n"
                        + "    \"cardHolderName\":\"John Smith\",\n"
                        + "    \"cardId\": \"_aaaaaaaaaaaaabbbbbbbbbbbcccccc\",\n"
                        + "    \"hasGeoSec\": false,\n"
                        + "    \"imageUrl\": \"https://eticket32.prod.bec.dk/grafik/android/xhdpi/small/k_visa.png\",\n"
                        + "    \"isInWallet\": false,\n"
                        + "    \"urlDetails\": \"/mobilbank/kort/detalje?cardId=_N2D1zq*fqEyRko-rjmXAHm3MsThPTFCrj5HG3s5lJlXJ&iconType=4&exp=_F0ZjliOHOGDZmtQHVb__\",\n"
                        + "    \"canReorder\": false,\n"
                        + "    \"isAddToWalletAvailable\": false\n"
                        + "}",
                CardDetailsResponse.class);
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
                        + "  \"accountType\": \"mastercard\",\n"
                        + "  \"accountName\": \"mastercard\",\n"
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
