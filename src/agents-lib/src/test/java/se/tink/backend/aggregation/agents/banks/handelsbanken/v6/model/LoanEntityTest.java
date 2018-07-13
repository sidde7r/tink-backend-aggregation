package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.system.rpc.Loan;

public class LoanEntityTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String LOAN_ITEM = "{\"links\":[{\"rel\":\"loan-condition-context\",\"href\":\"https://m2.handelsbanken.se/app/priv/loan/loan-condition-context?agreementNumber=EENzmvdAS250G1TvNG_V_FywhZynbNxyJnKfAkeTE5E&currentDebt=MTE1MzEyNTAwMA&fixationdateText=MyBtw6VuYWRlcg&hasMoreBorrower=true&amortizationPeriod=&conditionChangeDate=MjAxNi0wMy0wMQ&authToken=e0e0f2952917\"},{\"rel\":\"inet-loan-about-condition-change\",\"href\":\"http://www.handelsbanken.se/shb/Inet/ICentSv.nsf/default/q3EF3A250A0ECD80EC1257E03003ACCE1?OpenDocument\"}],\"segments\":[{\"title\":\"Låneinformation\",\"properties\":[{\"label\":\"Lånenummer\",\"value\":\"1337\"},{\"label\":\"Fler låntagare finns\",\"value\":\"Ja\"},{\"label\":\"Lånets bindningstid\",\"value\":\"3 månader\"},{\"label\":\"Villkorsändringsdag\",\"value\":\"2016-03-01\"},{\"label\":\"Amortering\",\"value\":\"Amorteringsfritt\"},{\"label\":\"Debiteringsperiod\",\"value\":\"Månad\"},{\"label\":\"Nästa förfallodatum\",\"value\":\"2016-01-01\"},{\"label\":\"Konto som belastas\",\"value\":\"1337\"}]},{\"title\":null,\"properties\":[{\"label\":null,\"value\":\"Ditt 3-månaderslån förlängs automatiskt till en ny 3-månadersperiod på villkorsändringsdagen, om du inte väljer en ny bindningstid.\"}]},{\"title\":\"Villkorsändringsdag\",\"properties\":[{\"label\":null,\"value\":\"2016-03-01\"},{\"label\":null,\"value\":\"Om du vill ändra räntebindningstid, villkorsändra senast 2016-02-29. I annat fall förlängs lånet automatiskt till en ny räntebindningstid på tre månader på villkorsändringsdagen.\\n\\nGenom att dela upp lånen på olika räntebindningstider kan din ekonomi bli mindre sårbar om räntorna skulle stiga.\"}]}],\"lenderId\":\"SLAN\",\"lender\":\"Stadshypotek\",\"agreementNumber\":\"1337\",\"currentDebt\":1153125.0,\"currentDebtFormatted\":\"SEK 1a0153a0125,00\",\"currentDebtAmount\":{\"currency\":\"SEK\",\"amount\":1153125.0,\"amountFormatted\":\"1a0153a0125,00\",\"unit\":null},\"interestRateFormatted\":\"1,47%\",\"toPayFormatted\":\"Ej aviserat\",\"displayBadge\":false,\"unpaid\":false,\"displayDetails\":true,\"unpaidText\":null,\"contactOffice\":false,\"contactOfficeText\":null,\"fixationdateText\":\"3 månader\"}";
    private static final String LOAN_ITEM_WITH_NULLS = "{\"links\":[{\"rel\":\"loan-condition-context\",\"href\":\"https://m2.handelsbanken.se/app/priv/loan/loan-condition-context?agreementNumber=EENzmvdAS250G1TvNG_V_FywhZynbNxyJnKfAkeTE5E&currentDebt=MTE1MzEyNTAwMA&fixationdateText=MyBtw6VuYWRlcg&hasMoreBorrower=true&amortizationPeriod=&conditionChangeDate=MjAxNi0wMy0wMQ&authToken=e0e0f2952917\"},{\"rel\":\"inet-loan-about-condition-change\",\"href\":\"http://www.handelsbanken.se/shb/Inet/ICentSv.nsf/default/q3EF3A250A0ECD80EC1257E03003ACCE1?OpenDocument\"}],\"segments\":[{\"title\":\"Låneinformation\",\"properties\":[{\"label\":\"Lånenummer\",\"value\":null},{\"label\":\"Fler låntagare finns\",\"value\":null},{\"label\":\"Lånets bindningstid\",\"value\":null},{\"label\":\"Villkorsändringsdag\",\"value\":null},{\"label\":\"Amortering\",\"value\":null},{\"label\":\"Debiteringsperiod\",\"value\":\"Månad\"},{\"label\":\"Nästa förfallodatum\",\"value\":\"2016-01-01\"},{\"label\":\"Konto som belastas\",\"value\":\"1337\"}]},{\"title\":null,\"properties\":[{\"label\":null,\"value\":\"Ditt 3-månaderslån förlängs automatiskt till en ny 3-månadersperiod på villkorsändringsdagen, om du inte väljer en ny bindningstid.\"}]},{\"title\":\"Villkorsändringsdag\",\"properties\":[{\"label\":null,\"value\":\"2016-03-01\"},{\"label\":null,\"value\":\"Om du vill ändra räntebindningstid, villkorsändra senast 2016-02-29. I annat fall förlängs lånet automatiskt till en ny räntebindningstid på tre månader på villkorsändringsdagen.\\n\\nGenom att dela upp lånen på olika räntebindningstider kan din ekonomi bli mindre sårbar om räntorna skulle stiga.\"}]}],\"lenderId\":\"SLAN\",\"lender\":null,\"agreementNumber\":null,\"currentDebt\":null,\"currentDebtFormatted\":\"SEK 1a0153a0125,00\",\"currentDebtAmount\":{\"currency\":\"SEK\",\"amount\":1153125.0,\"amountFormatted\":\"1a0153a0125,00\",\"unit\":null},\"interestRateFormatted\":\"1,47%\",\"toPayFormatted\":\"Ej aviserat\",\"displayBadge\":false,\"unpaid\":false,\"displayDetails\":true,\"unpaidText\":null,\"contactOffice\":false,\"contactOfficeText\":null,\"fixationdateText\":null}";

    @Test
    public void testDeserialize() throws IOException {
        LoanEntity loanEntity = MAPPER.readValue(LOAN_ITEM,LoanEntity.class);
        String loanEntityAsString = MAPPER.writeValueAsString(loanEntity);
        Loan loan = loanEntity.toloan();

        Assertions.assertThat(loan.getBalance()).isEqualTo(-1153125.0);
        Assertions.assertThat(loan.getNumMonthsBound()).isEqualTo(3);
        Assertions.assertThat(loan.getMonthlyAmortization()).isEqualTo(0);
        Assertions.assertThat(loan.getLoanNumber()).isEqualTo("1337");
        Assertions.assertThat(loan.getInterest()).isEqualTo(0.0147);
        Assertions.assertThat(loan.getName()).isEqualTo("Stadshypotek");
        Assertions.assertThat(loan.getNextDayOfTermsChange()).isNotNull();
        Assertions.assertThat(loan.getLoanDetails().isCoApplicant()).isTrue();
        Assertions.assertThat(loan.getSerializedLoanResponse()).isEqualTo(loanEntityAsString);
    }

    @Test
    public void testWithNulls() throws IOException {
        LoanEntity loanEntity = MAPPER.readValue(LOAN_ITEM_WITH_NULLS,LoanEntity.class);
        loanEntity.toloan();
    }
}
