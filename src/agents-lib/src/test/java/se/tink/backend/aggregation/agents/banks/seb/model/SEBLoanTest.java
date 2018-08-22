package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.text.ParseException;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.system.rpc.Loan;

public class SEBLoanTest {
    private static final String LOAN_RESPONSE_ITEM = "{\"ROW_ID\":1,\"KONTRAKTNR\":\"1337\",\"KTOSLAG_TXT\":\"BOLÅN - BOTTENLÅN MED BUNDEN RÄNTA\",\"SKULD\":47000.00,\"RTE_SATS\":1.760,\"NASTA_FFDAT\":\"2016-09-28\",\"BELOPP\":1069.00,\"LANTAGARE1\":\"NAMN NAMNSSON\",\"LANTAGARE2\":\"\",\"FLER_LANTAGARE_FL\":\"N\",\"RTE_FF_DATUM\":\"2019-04-28\",\"INBETSATT\":\"DEBITERAS  KONTO 31337\",\"OBJBETD1\":\"Örebro\",\"OBJBETD2\":\"Ålsjön\",\"OBJBETD3\":\"3\",\"OBJBETD4\":\"\",\"AMORTERING\":1000.00,\"RANTA\":69.00,\"DRJBEL\":0.00,\"OVRAVGBEL\":0.00,\"KUNDNR_LOP_NR\":\"001\",\"LANTAGARE_FL\":\"1\",\"DATSLUTBET\":\"2019-04-28\",\"DATRTEJUST\":\"\",\"AVISERING_FL\":\"J\",\"FORFALL_KOD\":\"01\",\"AVI_TXT\":\"VARJE MÅNAD\"}";
    private static final String LOAN_RESPONSE_ITEM_WITH_EPMTY_AND_NULLS = "{\"ROW_ID\":1,\"KONTRAKTNR\":null,\"KTOSLAG_TXT\":null,\"SKULD\":0.00,\"RTE_SATS\":0.00,\"NASTA_FFDAT\":null,\"BELOPP\":0.00,\"LANTAGARE1\":null,\"LANTAGARE2\":null,\"FLER_LANTAGARE_FL\":null,\"RTE_FF_DATUM\":null,\"INBETSATT\":null,\"OBJBETD1\":null,\"OBJBETD2\":null,\"OBJBETD3\":null,\"OBJBETD4\":null,\"AMORTERING\":0.00,\"RANTA\":0.00,\"DRJBEL\":0.00,\"OVRAVGBEL\":0.00,\"KUNDNR_LOP_NR\":null,\"LANTAGARE_FL\":null,\"DATSLUTBET\":null,\"DATRTEJUST\":null,\"AVISERING_FL\":null,\"FORFALL_KOD\":null,\"AVI_TXT\":null}";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void testDeserializationOfLoans() throws IOException, ParseException {
        PCBW2581 details = MAPPER.readValue(LOAN_RESPONSE_ITEM, PCBW2581.class);
        String serializedDetails = MAPPER.writeValueAsString(details);

        Loan loan = details.toLoan();

        Assertions.assertThat(loan.getName()).isEqualTo("BOLÅN - BOTTENLÅN MED BUNDEN RÄNTA");
        Assertions.assertThat(loan.getLoanNumber()).isEqualTo("1337");
        Assertions.assertThat(loan.getBalance()).isEqualTo(-47000.00);
        Assertions.assertThat(loan.getLoanDetails()).isNotNull();
        Assertions.assertThat(loan.getMonthlyAmortization()).isEqualTo(1000.00);
        Assertions.assertThat(loan.getInterest()).isEqualTo(0.01760);
        Assertions.assertThat(loan.getSerializedLoanResponse()).isEqualTo(serializedDetails);
    }

    @Test
    public void testDeserializationOfAccounts() throws IOException {
        PCBW2581 details = MAPPER.readValue(LOAN_RESPONSE_ITEM, PCBW2581.class);

        Account account = details.toAccount();

        Assertions.assertThat(account.getBalance()).isEqualTo(-47000.00);
        Assertions.assertThat(account.getBankId()).isEqualTo("1337");
        Assertions.assertThat(account.getName()).isEqualTo("BOLÅN - BOTTENLÅN MED BUNDEN RÄNTA");
        Assertions.assertThat(account.getType()).isEqualTo(AccountTypes.LOAN);
    }

    @Test
    public void testWithNulls() throws IOException, ParseException {
        PCBW2581 details = MAPPER.readValue(LOAN_RESPONSE_ITEM_WITH_EPMTY_AND_NULLS, PCBW2581.class);

        details.toLoan();
        details.toAccount();
    }
}
