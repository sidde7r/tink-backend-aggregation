package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.creditcard;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Collection;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.client.FetcherClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.creditcard.rpc.CreditCardDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.creditcard.rpc.CreditCardsResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CreditCardFetcherTest {

    private static final String CREDIT_CARD_DATA_JSON =
            "{\"cards\":[{\"card_id\":\"2218836201\",\"card_category\":\"credit\",\"card_status\":\"active\",\"card_expiration\":{\"date\":\"2023-02-28\",\"formatted\":\"02/23\"},\"cardholder_name\":\"ASDF\",\"cardholder_type\":\"parallel\",\"principal_cardholder_name\":null,\"product_code\":\"NO0051\",\"card_loyalty_group\":null,\"country_code\":\"NO\",\"currency\":\"NOK\",\"nickname\":null,\"pan_id\":null,\"atm_account_number\":null,\"credit\":{\"masked_credit_card_number\":\"5269 **** **** 3239\",\"credit_limit\":null,\"credit_booked_balance\":null,\"credit_available_balance\":9375.33,\"prev_min_instalment_amount\":null,\"instalment_min_percent\":null,\"instalment_free_months\":[],\"interest\":null,\"invoice\":null},\"debit\":null,\"parallel_cards\":null,\"usage_limits\":null,\"permissions\":{\"transactions\":true,\"transfer_from_credit\":true,\"view_pin\":true,\"allowed_to_enroll_mobile_payments\":true},\"notifications\":null,\"metadata\":null},{\"card_id\":\"KHOP197\",\"card_category\":\"debit\",\"card_status\":\"active\",\"card_expiration\":null,\"cardholder_name\":\"ASDF ASDF ASDF\",\"cardholder_type\":null,\"principal_cardholder_name\":null,\"product_code\":\"NOECB\",\"card_loyalty_group\":null,\"country_code\":\"NO\",\"currency\":\"NOK\",\"nickname\":null,\"pan_id\":null,\"atm_account_number\":null,\"credit\":null,\"debit\":{\"masked_debit_card_number\":\"4002 **** **** 4549\",\"debit_account_balance\":null,\"debit_account_number\":null,\"debit_account_name\":null,\"debit_account_key\":null},\"parallel_cards\":null,\"usage_limits\":null,\"permissions\":{\"transactions\":false,\"transfer_from_credit\":false,\"view_pin\":true,\"allowed_to_enroll_mobile_payments\":true},\"notifications\":null,\"metadata\":null}]}";
    private static final String CREDIT_CARD_DETAILS_DATA_JSON =
            "{\"card_id\":\"2218836201\",\"card_category\":\"credit\",\"card_status\":\"active\",\"card_expiration\":{\"date\":\"2023-02-28\",\"formatted\":\"02/23\"},\"cardholder_name\":\"FIRST SECOND SURNAME\",\"cardholder_type\":\"parallel\",\"principal_cardholder_name\":null,\"product_code\":\"NO0051\",\"card_loyalty_group\":null,\"country_code\":\"NO\",\"currency\":\"NOK\",\"nickname\":null,\"pan_id\":null,\"atm_account_number\":null,\"credit\":{\"masked_credit_card_number\":\"5269 **** **** 3239\",\"credit_limit\":10000,\"credit_booked_balance\":127.05,\"credit_available_balance\":1765.06,\"prev_min_instalment_amount\":null,\"instalment_min_percent\":null,\"instalment_free_months\":[],\"interest\":null,\"invoice\":null},\"debit\":null,\"parallel_cards\":null,\"usage_limits\":{\"internet\":true,\"internet_non3d\":null,\"contactless\":null,\"daily_limits\":null,\"weekly_limits\":null,\"monthly_credit_spending_limit\":null,\"permanent_region\":[\"norway\",\"nordic\",\"baltic\",\"europe\",\"americas\",\"asia\",\"africa\",\"oceania\"],\"temporary_region\":null},\"permissions\":{\"transactions\":true,\"transfer_from_credit\":true,\"view_pin\":true,\"allowed_to_enroll_mobile_payments\":true},\"notifications\":null,\"metadata\":{\"geographical_zones\":[\"norway\",\"europe\",\"nordic\",\"baltic\",\"americas\",\"asia\",\"oceania\",\"africa\"],\"limits\":{\"atm_payment\":0,\"atm_payment_step\":0,\"atm_withdrawal\":7500,\"atm_withdrawal_step\":500,\"pos_payment\":100000,\"pos_payment_step\":500,\"online_payment\":100000,\"online_payment_step\":500}}}\n";

    private static final String MASKED_PAN = "5269********3239";

    @Test
    public void shouldReturnProperlyMappedCreditCards() {
        // given
        FetcherClient fetcherClient = mock(FetcherClient.class);
        given(fetcherClient.fetchCreditCards())
                .willReturn(
                        SerializationUtils.deserializeFromString(
                                CREDIT_CARD_DATA_JSON, CreditCardsResponse.class));
        given(fetcherClient.fetchCreditCardDetails("2218836201"))
                .willReturn(
                        SerializationUtils.deserializeFromString(
                                CREDIT_CARD_DETAILS_DATA_JSON, CreditCardDetailsResponse.class));
        CreditCardFetcher creditCardFetcher = new CreditCardFetcher(fetcherClient);
        // when
        Collection<CreditCardAccount> creditCardAccounts = creditCardFetcher.fetchAccounts();

        // then

        assertThat(creditCardAccounts).hasSize(1);
        CreditCardAccount card = creditCardAccounts.iterator().next();
        assertThat(card.isUniqueIdentifierEqual(MASKED_PAN));
        assertThat(card.getApiIdentifier()).isEqualTo("2218836201");
        assertThat(card.getIdentifiers()).hasSize(1);
        assertThat(card.getIdentifiers().get(0).getType())
                .isEqualTo(AccountIdentifier.Type.PAYMENT_CARD_NUMBER);
        assertThat(card.getIdentifiers().get(0).getIdentifier()).isEqualTo(MASKED_PAN);

        assertThat(card.getName()).isEqualTo(MASKED_PAN);
        assertThat(card.getAccountNumber()).isEqualTo(MASKED_PAN);

        assertThat(card.getExactBalance()).isEqualTo(ExactCurrencyAmount.of(127.05, "NOK"));
        assertThat(card.getExactAvailableCredit())
                .isEqualTo(ExactCurrencyAmount.of(1765.06, "NOK"));
    }
}
