package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc;

import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class ListLoanAccountsResponseTestData {

    static ListLoanAccountsResponse getTestData() {
        ListLoanAccountsResponse response =
                SerializationUtils.deserializeFromString(TEST_DATA, ListLoanAccountsResponse.class);
        Assertions.assertThat(response.size()).isEqualTo(3);
        return response;
    }

    private static final String TEST_DATA =
            "[ {"
                    + "  \"loanType\" : \"KS_ACCOUNT_LOAN\","
                    + "  \"label\" : \"Ansattlån 1,5M\","
                    + "  \"secondaryLabel\" : \"1111.79.33333\","
                    + "  \"amount\" : {"
                    + "    \"localizedValue\" : \"-1,500,148.50\","
                    + "    \"localizedValueWithCurrency\" : \"-1,500,148.50 NOK\","
                    + "    \"value\" : -150014850,"
                    + "    \"scale\" : 2,"
                    + "    \"currency\" : \"NOK\","
                    + "    \"localizedValueWithCurrencyAtEnd\" : \"-1,500,148.50 NOK\","
                    + "    \"roundedAmountWithIsoCurrency\" : \"-NOK1,500,148\","
                    + "    \"roundedAmountWithCurrencySymbol\" : \"-1,500,148 NOK\""
                    + "  },"
                    + "  \"secondayAmount\" : \"\","
                    + "  \"labelValuePairList\" : null,"
                    + "  \"entityKey\" : {"
                    + "    \"accountId\" : \"1111.7933333\","
                    + "    \"agreementId\" : null"
                    + "  }"
                    + "}, {"
                    + "  \"loanType\" : \"KS_ACCOUNT_LOAN\","
                    + "  \"label\" : \"Ansattlån 2,0M\","
                    + "  \"secondaryLabel\" : \"1111.79.22222\","
                    + "  \"amount\" : {"
                    + "    \"localizedValue\" : \"-2,000,241.48\","
                    + "    \"localizedValueWithCurrency\" : \"-2,000,241.48 NOK\","
                    + "    \"value\" : -200024148,"
                    + "    \"scale\" : 2,"
                    + "    \"currency\" : \"NOK\","
                    + "    \"localizedValueWithCurrencyAtEnd\" : \"-2,000,241.48 NOK\","
                    + "    \"roundedAmountWithIsoCurrency\" : \"-NOK2,000,241\","
                    + "    \"roundedAmountWithCurrencySymbol\" : \"-2,000,241 NOK\""
                    + "  },"
                    + "  \"secondayAmount\" : \"\","
                    + "  \"labelValuePairList\" : null,"
                    + "  \"entityKey\" : {"
                    + "    \"accountId\" : \"1111.7922222\","
                    + "    \"agreementId\" : null"
                    + "  }"
                    + "}, {"
                    + "  \"loanType\" : \"KS_ACCOUNT_LOAN\","
                    + "  \"label\" : \"Nedbetalingslån \","
                    + "  \"secondaryLabel\" : \"1111.79.44444\","
                    + "  \"amount\" : {"
                    + "    \"localizedValue\" : \"-593,419.80\","
                    + "    \"localizedValueWithCurrency\" : \"-593,419.80 NOK\","
                    + "    \"value\" : -59341980,"
                    + "    \"scale\" : 2,"
                    + "    \"currency\" : \"NOK\","
                    + "    \"localizedValueWithCurrencyAtEnd\" : \"-593,419.80 NOK\","
                    + "    \"roundedAmountWithIsoCurrency\" : \"-NOK593,420\","
                    + "    \"roundedAmountWithCurrencySymbol\" : \"-593,420 NOK\""
                    + "  },"
                    + "  \"secondayAmount\" : \"\","
                    + "  \"labelValuePairList\" : null,"
                    + "  \"entityKey\" : {"
                    + "    \"accountId\" : \"1111.7944444\","
                    + "    \"agreementId\" : null"
                    + "  }"
                    + "} ]";
}
