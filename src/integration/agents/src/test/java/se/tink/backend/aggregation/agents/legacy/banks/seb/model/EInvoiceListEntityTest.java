package se.tink.backend.aggregation.agents.banks.seb.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Predicate;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import se.tink.libraries.account.identifiers.BankGiroIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.social.security.time.SwedishTimeRule;

@RunWith(Enclosed.class)
public class EInvoiceListEntityTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static class ValidEntity {
        private EInvoiceListEntity deserialized;

        @Rule public SwedishTimeRule timeRule = new SwedishTimeRule();

        @Before
        public void setupModel() throws IOException {
            deserialized =
                    MAPPER.readValue(
                            "{\n"
                                    + "\t\t\t\t\"ROW_ID\": 2,\n"
                                    + "\t\t\t\t\"SEB_KUND_NR\": \"86070155370009\",\n"
                                    + "\t\t\t\t\"BG_NR\": \"297-9391\",\n"
                                    + "\t\t\t\t\"BETALAR_NR\": \"198607015537\",\n"
                                    + "\t\t\t\t\"REFERENS\": \"5586070155372\",\n"
                                    + "\t\t\t\t\"TIMESTAMP\": \"2016-03-16-10.54.00.941499\",\n"
                                    + "\t\t\t\t\"BELOPP\": 100.00,\n"
                                    + "\t\t\t\t\"BELOPP_URSPR\": 100.00,\n"
                                    + "\t\t\t\t\"FF_DATUM\": \"2016-03-31\",\n"
                                    + "\t\t\t\t\"FF_DATUM_URSPR\": \"2016-03-31\",\n"
                                    + "\t\t\t\t\"VALUTAKOD\": \"SEK\",\n"
                                    + "\t\t\t\t\"FAKT_SPEC_URL\": \"https://efaktpriv.edb.se/visa/visa.ashx\",\n"
                                    + "\t\t\t\t\"BEL_ANDR_KOD\": \"J\",\n"
                                    + "\t\t\t\t\"E_GIROTYP\": \"EG\",\n"
                                    + "\t\t\t\t\"E_GIROFAKTTYP\": \"1\",\n"
                                    + "\t\t\t\t\"KONTO_NR\": \"53680239572\",\n"
                                    + "\t\t\t\t\"STAT\": \"1\",\n"
                                    + "\t\t\t\t\"KORT_NAMN\": \"\",\n"
                                    + "\t\t\t\t\"NAMN\": \"AEA (AKADEMIKERNAS A-KASS\",\n"
                                    + "\t\t\t\t\"KTOSLAG_TXT\": \"PRIVATKONTO\",\n"
                                    + "\t\t\t\t\"KTOBEN_TXT\": \"PENGAPÅSEN\",\n"
                                    + "\t\t\t\t\"BOKF_SALDO\": 2656.99,\n"
                                    + "\t\t\t\t\"DISP_BEL\": 2456.99,\n"
                                    + "\t\t\t\t\"KREDBEL\": 0.00,\n"
                                    + "\t\t\t\t\"KHAV\": \"DANIEL LERVIK\",\n"
                                    + "\t\t\t\t\"FAKTURA_ID\": \"2016-03-14-15.38.30.124657\",\n"
                                    + "\t\t\t\t\"E_REFERENSTEXT\": \"AEA\",\n"
                                    + "\t\t\t\t\"REF_ANV_KOD\": \"\",\n"
                                    + "\t\t\t\t\"REF_TIDIGARE\": \"\",\n"
                                    + "\t\t\t\t\"KONTO_NR_EA\": \"\",\n"
                                    + "\t\t\t\t\"E_KATEGORI\": \"\",\n"
                                    + "\t\t\t\t\"BELOPP_VAT\": 0.00,\n"
                                    + "\t\t\t\t\"PROCENT_VAT\": \"000\",\n"
                                    + "\t\t\t\t\"TRANS_KOD_E\": \"82\",\n"
                                    + "\t\t\t\t\"UTSTALLARE_ID\": \"20160314111458-0004370\",\n"
                                    + "\t\t\t\t\"BILJETT_TYP\": \"EG40\",\n"
                                    + "\t\t\t\t\"GIRO_TYP\": \"BG\",\n"
                                    + "\t\t\t\t\"GIRO_NR\": \"5862-8082\",\n"
                                    + "\t\t\t\t\"FU_IDENT\": \"008020054691.02000001970.FSPA.SE\",\n"
                                    + "\t\t\t\t\"STATUS_EGIRO2\": \"\",\n"
                                    + "\t\t\t\t\"UPPDRAG_KOD_ANTAL\": 0,\n"
                                    + "\t\t\t\t\"ENCRYPTED_TICKET\": \"someticket.......................\"\n"
                                    + "\t\t\t}",
                            EInvoiceListEntity.class);
        }

        @Test
        public void hasAmount() {
            assertThat(deserialized.getCurrentAmount().getCurrencyCode()).isEqualTo("SEK");
            assertThat(deserialized.getCurrentAmount().getDoubleValue()).isEqualTo(100.00d);
        }

        @Test
        public void hasDestination() {
            BankGiroIdentifier expectedIdentifier = new BankGiroIdentifier("58628082");
            assertThat(deserialized.getDestination()).isEqualTo(expectedIdentifier);
        }

        @Test
        public void hasDestinationMessage() {
            assertThat(deserialized.getDestinationMessage()).isEqualTo("5586070155372");
        }

        @Test
        public void hasDueDate() throws ParseException {
            Date expectedDate =
                    new DateTime()
                            .withYear(2016)
                            .withMonthOfYear(3)
                            .withDayOfMonth(31)
                            .withHourOfDay(12)
                            .withMinuteOfHour(0)
                            .withSecondOfMinute(0)
                            .withMillisOfSecond(0)
                            .toDate();

            assertThat(deserialized.getOriginalDueDate()).isEqualTo(expectedDate);
        }

        @Test
        public void hasSource() {
            SwedishIdentifier expectedIdentifier = new SwedishIdentifier("53680239572");
            assertThat(deserialized.getSource()).isEqualTo(expectedIdentifier);
        }

        @Test
        public void hasSourceMessage() {
            assertThat(deserialized.getSourceMessage()).isEqualTo("Aea Akademikernas A-kass");
        }

        @Test
        public void hasProviderUniqueId() {
            assertThat(deserialized.getProviderUniqueId())
                    .isEqualTo("20160314111458-0004370|2016-03-14-15.38.30.124657");
        }

        @Test
        public void isNotConsideredEmpty() {
            Predicate<EInvoiceListEntity> predicate = EInvoiceListEntity.IS_EMPTY;
            assertThat(predicate.apply(deserialized)).isFalse();
        }
    }

    public static class EmptyEntity {
        private EInvoiceListEntity deserialized;

        @Before
        public void setupModel() throws IOException {
            deserialized =
                    MAPPER.readValue(
                            "{\n"
                                    + "\t\t\t\t\"ROW_ID\": 1,\n"
                                    + "\t\t\t\t\"SEB_KUND_NR\": \"\",\n"
                                    + "\t\t\t\t\"BG_NR\": \"\",\n"
                                    + "\t\t\t\t\"BETALAR_NR\": \"\",\n"
                                    + "\t\t\t\t\"REFERENS\": \"\",\n"
                                    + "\t\t\t\t\"TIMESTAMP\": \"\",\n"
                                    + "\t\t\t\t\"BELOPP\": 0.00,\n"
                                    + "\t\t\t\t\"BELOPP_URSPR\": 0.00,\n"
                                    + "\t\t\t\t\"FF_DATUM\": \"\",\n"
                                    + "\t\t\t\t\"FF_DATUM_URSPR\": \"\",\n"
                                    + "\t\t\t\t\"VALUTAKOD\": \"\",\n"
                                    + "\t\t\t\t\"FAKT_SPEC_URL\": \"\",\n"
                                    + "\t\t\t\t\"BEL_ANDR_KOD\": \"\",\n"
                                    + "\t\t\t\t\"E_GIROTYP\": \"\",\n"
                                    + "\t\t\t\t\"E_GIROFAKTTYP\": \"\",\n"
                                    + "\t\t\t\t\"KONTO_NR\": \"\",\n"
                                    + "\t\t\t\t\"STAT\": \"\",\n"
                                    + "\t\t\t\t\"KORT_NAMN\": \"\",\n"
                                    + "\t\t\t\t\"NAMN\": \"\",\n"
                                    + "\t\t\t\t\"KTOSLAG_TXT\": \"\",\n"
                                    + "\t\t\t\t\"KTOBEN_TXT\": \"\",\n"
                                    + "\t\t\t\t\"BOKF_SALDO\": 0.00,\n"
                                    + "\t\t\t\t\"DISP_BEL\": 0.00,\n"
                                    + "\t\t\t\t\"KREDBEL\": 0.00,\n"
                                    + "\t\t\t\t\"KHAV\": \"\",\n"
                                    + "\t\t\t\t\"FAKTURA_ID\": \"\",\n"
                                    + "\t\t\t\t\"E_REFERENSTEXT\": \"\",\n"
                                    + "\t\t\t\t\"REF_ANV_KOD\": \"\",\n"
                                    + "\t\t\t\t\"REF_TIDIGARE\": \"\",\n"
                                    + "\t\t\t\t\"KONTO_NR_EA\": \"\",\n"
                                    + "\t\t\t\t\"E_KATEGORI\": \"\",\n"
                                    + "\t\t\t\t\"BELOPP_VAT\": 0.00,\n"
                                    + "\t\t\t\t\"PROCENT_VAT\": \"\",\n"
                                    + "\t\t\t\t\"TRANS_KOD_E\": \"\",\n"
                                    + "\t\t\t\t\"UTSTALLARE_ID\": \"\",\n"
                                    + "\t\t\t\t\"BILJETT_TYP\": \"\",\n"
                                    + "\t\t\t\t\"GIRO_TYP\": \"\",\n"
                                    + "\t\t\t\t\"GIRO_NR\": \"\",\n"
                                    + "\t\t\t\t\"FU_IDENT\": \"\",\n"
                                    + "\t\t\t\t\"STATUS_EGIRO2\": \"N\",\n"
                                    + "\t\t\t\t\"UPPDRAG_KOD_ANTAL\": 0,\n"
                                    + "\t\t\t\t\"ENCRYPTED_TICKET\": \"someticket.......................\"\n"
                                    + "\t\t\t}",
                            EInvoiceListEntity.class);
        }

        @Test
        public void isConsideredEmpty() {
            Predicate<EInvoiceListEntity> predicate = EInvoiceListEntity.IS_EMPTY;
            assertThat(predicate.apply(deserialized)).isTrue();
        }
    }
}
