package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SdcAccountTest {

    private static String SDC_ACCOUNT =
            "{\n"
                    + "    \"id\": \"sample.id\", \n"
                    + "    \"amount\": {\n"
                    + "        \"value\": 234,\n"
                    + "        \"scale\": 10,\n"
                    + "        \"currency\": \"sample currency\"\n"
                    + "    },\n"
                    + "    \"availableAmount\": {\n"
                    + "        \"value\": 456,\n"
                    + "        \"scale\": 10,\n"
                    + "        \"currency\": \"sample currency 2\"\n"
                    + "    },\n"
                    + "    \"localizedAccountId\": \"sample localized account id\",\n"
                    + "    \"name\" : \"sample name\"\n"
                    + "}";

    @Test
    public void toTinkCreditCardAccount() {
        // given
        SdcAccount sdcAccount =
                SerializationUtils.deserializeFromString(SDC_ACCOUNT, SdcAccount.class);

        // when
        CreditCardAccount creditCardAccount = sdcAccount.toTinkCreditCardAccount(null);

        // then
        assertThat(creditCardAccount.getName()).isEqualTo("sample name");
        assertThat(creditCardAccount.getAccountNumber()).isEqualTo("sample localized account id");
        assertThat(creditCardAccount.getApiIdentifier()).isEqualTo("sampleid");

        assertThat(creditCardAccount.getExactBalance())
                .isEqualTo(ExactCurrencyAmount.of(BigDecimal.valueOf(234, 10), "sample currency"));

        assertThat(creditCardAccount.getExactAvailableCredit())
                .isEqualTo(
                        ExactCurrencyAmount.of(BigDecimal.valueOf(456, 10), "sample currency 2"));
    }
}
