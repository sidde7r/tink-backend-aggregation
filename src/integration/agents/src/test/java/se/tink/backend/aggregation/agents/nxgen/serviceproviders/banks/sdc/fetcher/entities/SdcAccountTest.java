package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.converter.DefaultAccountNumberToIbanConverter;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SdcAccountTest {

    private static final String SDC_CHECKING_ACCOUNT =
            "{\n"
                    + "    \"id\": \"1234.5678\", \n"
                    + "    \"amount\": {\n"
                    + "        \"value\": 234,\n"
                    + "        \"scale\": 10,\n"
                    + "        \"currency\": \"NOK\"\n"
                    + "    },\n"
                    + "    \"availableAmount\": {\n"
                    + "        \"value\": 456,\n"
                    + "        \"scale\": 10,\n"
                    + "        \"currency\": \"DKK\"\n"
                    + "    },\n"
                    + "    \"localizedAccountId\": \"sample localized account id\",\n"
                    + "    \"name\" : \"sample name\",\n"
                    + "    \"productElementType\" : \"CISP\""
                    + "}";

    @Test
    public void toTinkAccountWithCheckingAccount() {
        // given
        SdcAccount sdcAccount =
                SerializationUtils.deserializeFromString(SDC_CHECKING_ACCOUNT, SdcAccount.class);

        // when
        TransactionalAccount result =
                sdcAccount.toTinkAccount(DefaultAccountNumberToIbanConverter.DK_CONVERTER);

        // then
        assertThat(result.getType()).isEqualTo(AccountTypes.CHECKING);
        assertThat(result.getAccountNumber()).isEqualTo("DK9412340000005678");
        assertThat(result.getName()).isEqualTo("sample name");
        assertThat(result.isUniqueIdentifierEqual("DK9412340000005678")).isTrue();
        assertThat(result.getExactBalance())
                .isEqualTo(ExactCurrencyAmount.of(BigDecimal.valueOf(234, 10), "NOK"));
        assertThat(result.getIdentifiers())
                .anyMatch(
                        id ->
                                id.getIdentifier().contains("DK9412340000005678")
                                        && id.getType().toString().equals("iban"));
        assertThat(result.getIdentifiers())
                .anyMatch(
                        id ->
                                id.getIdentifier().contains("12345678")
                                        && id.getType().toString().equals("bban"));
        assertThat(result.getAccountFlags()).contains(AccountFlag.PSD2_PAYMENT_ACCOUNT);
    }

    @Test
    public void toTinkCreditCardAccount() {
        // given
        SdcAccount sdcAccount =
                SerializationUtils.deserializeFromString(SDC_CHECKING_ACCOUNT, SdcAccount.class);

        // when
        CreditCardAccount creditCardAccount = sdcAccount.toTinkCreditCardAccount();

        // then
        assertThat(creditCardAccount.getName()).isEqualTo("sample name");
        assertThat(creditCardAccount.getAccountNumber()).isEqualTo("sample localized account id");
        assertThat(creditCardAccount.getApiIdentifier()).isEqualTo("12345678");

        assertThat(creditCardAccount.getExactBalance())
                .isEqualTo(ExactCurrencyAmount.of(BigDecimal.valueOf(234, 10), "NOK"));

        assertThat(creditCardAccount.getExactAvailableCredit())
                .isEqualTo(ExactCurrencyAmount.of(BigDecimal.valueOf(456, 10), "DKK"));
    }
}
