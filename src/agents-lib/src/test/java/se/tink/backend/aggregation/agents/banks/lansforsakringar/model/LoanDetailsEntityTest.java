package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.text.ParseException;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.system.rpc.Loan;

public class LoanDetailsEntityTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String LOAN_ITEM = "{\"loanName\":\"Bolån Hypotek\",\"loanNumber\":\"1337\",\"originalDebt\":\"900 000,00\",\"currentDebt\":\"900 000,00\",\"currentInterestRate\":\"1,30 %\",\"rateBoundUntil\":\"2016-12-01\",\"rateBindingPeriodLength\":\"3 månader\",\"borrowers\":[{\"name\":\"Namnsson, Namn\",\"debtOwnershipShare\":\"50,00 %\",\"interestRateOwnershipShare\":\"0,00 %\"},{\"name\":\"Efternamn, Förnamn\",\"debtOwnershipShare\":\"50,00 %\",\"interestRateOwnershipShare\":\"0,00 %\"}],\"securities\":[{\"securityText\":\"EN BRF NÅGONSTANS\",\"securityType\":\"Nyttjanderätt\"}],\"fixedRate\":true,\"modificationStatus\":\"BINDING_ALLOWED\",\"infoText\":\"Du kan räntebinda dina lån alla dagar kl. 8-22.\",\"nearExpiryDate\":false,\"bindingPeriodInfoModel\":null}";
    private static final String LOAN_ITEM_WITH_NULL = "{\"loanName\":null,\"loanNumber\":null,\"originalDebt\":null,\"currentDebt\":null,\"currentInterestRate\":null,\"rateBoundUntil\":null,\"rateBindingPeriodLength\":null,\"borrowers\":[{\"name\":null,\"debtOwnershipShare\":null,\"interestRateOwnershipShare\":null},{\"name\":null,\"debtOwnershipShare\":null,\"interestRateOwnershipShare\":null}],\"securities\":[{\"securityText\":null,\"securityType\":null}],\"fixedRate\":true,\"modificationStatus\":\"BINDING_ALLOWED\",\"infoText\":\"Du kan räntebinda dina lån alla dagar kl. 8-22.\",\"nearExpiryDate\":false,\"bindingPeriodInfoModel\":null}";

    @Test
    public void testDeserialization() throws IOException, ParseException {
        LoanDetailsEntity details = MAPPER.readValue(LOAN_ITEM,LoanDetailsEntity.class);
        Loan loan = details.toLoan(LOAN_ITEM);

        Assertions.assertThat(loan.getName()).isEqualTo("Bolån Hypotek");
        Assertions.assertThat(loan.getLoanNumber()).isEqualTo("1337");
        Assertions.assertThat(loan.getAmortized()).isEqualTo(0);
        Assertions.assertThat(loan.getNextDayOfTermsChange()).isNotNull();
        Assertions.assertThat(loan.getSerializedLoanResponse()).isEqualTo(LOAN_ITEM);
        Assertions.assertThat(loan.getBalance()).isEqualTo(900000.0);
        Assertions.assertThat(loan.getInitialBalance()).isEqualTo(900000.0);
        Assertions.assertThat(loan.getNumMonthsBound()).isEqualTo(3);

        Assertions.assertThat(loan.getLoanDetails().isCoApplicant()).isEqualTo(true);
        Assertions.assertThat(loan.getLoanDetails().getApplicants()).isNotEmpty();
        Assertions.assertThat(loan.getLoanDetails().getLoanSecurity()).isNotEmpty();
    }

    @Test
    public void testForNullPointers() throws IOException, ParseException {
        LoanDetailsEntity details = MAPPER.readValue(LOAN_ITEM_WITH_NULL,LoanDetailsEntity.class);
        details.toLoan(LOAN_ITEM_WITH_NULL);
    }

}
