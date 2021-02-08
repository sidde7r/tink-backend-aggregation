package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.totalkredit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails.Type;
import se.tink.libraries.account.identifiers.DanishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class TotalKreditLoanTest {

    private static final String INPUT_DATA =
            "{\n"
                    + "  \"title\" : \"FrueSerre 5\",\n"
                    + "  \"description\" : \"F-kort\",\n"
                    + "  \"amount\" : {\n"
                    + "    \"localizedValue\" : \"3.677.446,53\",\n"
                    + "    \"localizedValueWithCurrency\" : \"3.677.446,53 DKK\",\n"
                    + "    \"localizedValueWithoutCurrency\" : \"3.677.446,53\",\n"
                    + "    \"value\" : 367744653,\n"
                    + "    \"scale\" : 2,\n"
                    + "    \"currency\" : \"DKK\"\n"
                    + "  },\n"
                    + "  \"details\" : [ {\n"
                    + "    \"label\" : \"Adresse\",\n"
                    + "    \"value\" : \"FrueSerre 5\\n4185 strup\"\n"
                    + "  }, {\n"
                    + "    \"label\" : \"Låntype\",\n"
                    + "    \"value\" : \"F-kort\"\n"
                    + "  }, {\n"
                    + "    \"label\" : \"Hovedstol\",\n"
                    + "    \"value\" : \"3.686.000,00\"\n"
                    + "  }, {\n"
                    + "    \"label\" : \"Obligationsrestgæld\",\n"
                    + "    \"value\" : \"3.677.446,53\"\n"
                    + "  }, {\n"
                    + "    \"label\" : \"Restgældsdato\",\n"
                    + "    \"value\" : \"01-01-2021\"\n"
                    + "  }, {\n"
                    + "    \"label\" : \"Ydelse 31-12-2020\",\n"
                    + "    \"value\" : \"8.570,24\"\n"
                    + "  }, {\n"
                    + "    \"label\" : \"Afdragsfrihed udløber\",\n"
                    + "    \"value\" : \"31-12-2024\"\n"
                    + "  }, {\n"
                    + "    \"label\" : \"Restløbetid\",\n"
                    + "    \"value\" : \"29 år 3 måneder\"\n"
                    + "  }, {\n"
                    + "    \"label\" : \"Rente\",\n"
                    + "    \"value\" : \"-0,2163%\"\n"
                    + "  }, {\n"
                    + "    \"label\" : \"Bidragssats - før fradrag af kundekroner\",\n"
                    + "    \"value\" : \"1,0816%\"\n"
                    + "  }, {\n"
                    + "    \"label\" : \"Valuta\",\n"
                    + "    \"value\" : \"DKK\"\n"
                    + "  } ]\n"
                    + "}";

    @Test
    public void toLoanAccount() {
        // given
        TotalKreditLoan loan =
                SerializationUtils.deserializeFromString(INPUT_DATA, TotalKreditLoan.class);
        // and
        final String agreementNumber = "sample agreement number";

        // when
        LoanAccount result = loan.toTinkLoan(agreementNumber);

        // then
        assertThat(result.getType()).isEqualTo(AccountTypes.LOAN);
        assertThat(result.getExactBalance()).isEqualTo(ExactCurrencyAmount.of(3677446.53d, "DKK"));
        assertThat(result.getInterestRate()).isEqualTo(-0.002163);

        assertThat(result.getAccountNumber()).isEqualTo("sample agreement number FrueSerre 5");
        assertThat(result.getApiIdentifier()).isEqualTo("sample agreement number FrueSerre 5");
        assertThat(result.getHolderName()).isNull();
        assertThat(result.getHolders()).isEmpty();
        assertThat(result.getIdentifiers())
                .contains(new DanishIdentifier("sampleagreementnumberFrueSerre5"));

        assertThat(result.getIdModule().getAccountName()).isEqualTo("FrueSerre 5");
        assertThat(result.getIdModule().getAccountNumber())
                .isEqualTo("sample agreement number FrueSerre 5");
        assertThat(result.getIdModule().getUniqueId()).isEqualTo("sampleagreementnumberFrueSerre5");
        assertThat(result.getIdModule().getProductName()).isEqualTo("FrueSerre 5");
        assertThat(result.getIdModule().getIdentifiers())
                .contains(new DanishIdentifier("sampleagreementnumberFrueSerre5"));

        assertThat(result.getName()).isEqualTo("FrueSerre 5");

        assertThat(result.getDetails().getType()).isEqualTo(Type.MORTGAGE);
        assertThat(result.getDetails().getInitialBalance())
                .isEqualTo(ExactCurrencyAmount.of(3686000.0d, "DKK"));
        assertThat(result.getDetails().getExactAmortized())
                .isEqualTo(ExactCurrencyAmount.of(8553.47d, "DKK"));
        assertThat(result.getDetails().getLoanNumber())
                .isEqualTo("sample agreement number FrueSerre 5");
        assertThat(result.getDetails().getSecurity()).isEqualTo("FrueSerre 5\n4185 strup");
        assertThat(result.getDetails().getNumMonthsBound()).isEqualTo(351);
        assertThat(result.getDetails().getExactMonthlyAmortization())
                .isEqualTo(ExactCurrencyAmount.of(8570.24d, "DKK"));
    }
}
