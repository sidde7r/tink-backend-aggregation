package se.tink.backend.aggregation.agents.banks.danskebank.v2;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

@RunWith(Enclosed.class)
public class DanskeBankAccountIdentifierFormatterTest {
    public static class FormattingDestinationAccount {
        @Test
        public void swedbank_10DigitAccountNumber() {
            AccountIdentifier accountIdentifier = new SwedishIdentifier("821499246657853");

            String formattedDestination =
                    accountIdentifier.getIdentifier(new DanskeBankAccountIdentifierFormatter());

            assertThat(formattedDestination).isEqualTo("82149246657853");
        }

        @Test
        public void swedbank_LessThan10DigitAccountNumber() {
            AccountIdentifier accountIdentifier = new SwedishIdentifier("82149112233");

            String formattedDestination =
                    accountIdentifier.getIdentifier(new DanskeBankAccountIdentifierFormatter());

            assertThat(formattedDestination).isEqualTo("82140000112233");
        }

        @Test
        public void savingsbank() {
            AccountIdentifier accountIdentifier = new SwedishIdentifier("8422831270465");

            String formattedDestination =
                    accountIdentifier.getIdentifier(new DanskeBankAccountIdentifierFormatter());

            assertThat(formattedDestination).isEqualTo("84220031270465");
        }

        @Test
        public void nordea() {
            AccountIdentifier accountIdentifier = new SwedishIdentifier("16034332648");

            String formattedDestination =
                    accountIdentifier.getIdentifier(new DanskeBankAccountIdentifierFormatter());

            assertThat(formattedDestination).isEqualTo("16034332648");
        }

        @Test
        public void nordeaPersonkonto() {
            AccountIdentifier accountIdentifier = new SwedishIdentifier("33008401141935");

            String formattedDestination =
                    accountIdentifier.getIdentifier(new DanskeBankAccountIdentifierFormatter());

            assertThat(formattedDestination).isEqualTo("8401141935");
        }
    }

    public static class Parsing {
        @Test
        public void swedbank_10DigitAccountNumber() {
            String accountNumber = "82149246657853";
            String bankId = "SWEDBANK";

            DanskeBankAccountIdentifierFormatter identifierFormatter =
                    new DanskeBankAccountIdentifierFormatter();
            SwedishIdentifier parsedIdentifier =
                    identifierFormatter.parseSwedishIdentifier(bankId, accountNumber);

            AccountIdentifier expectedIdentifier = new SwedishIdentifier("821499246657853");
            assertThat(parsedIdentifier).isEqualTo(expectedIdentifier);
        }

        @Test
        public void swedbank_PaddedAccountNumber() {
            String accountNumber = "82140000112233";
            String bankId = "SWEDBANK";

            DanskeBankAccountIdentifierFormatter identifierFormatter =
                    new DanskeBankAccountIdentifierFormatter();
            SwedishIdentifier parsedIdentifier =
                    identifierFormatter.parseSwedishIdentifier(bankId, accountNumber);

            AccountIdentifier expectedIdentifier = new SwedishIdentifier("821490000112233");
            assertThat(parsedIdentifier).isEqualTo(expectedIdentifier);
        }

        @Test
        public void nordeaGeneral() {
            String accountNumber = "16034332648";
            String bankId = "NORDEA";

            DanskeBankAccountIdentifierFormatter identifierFormatter =
                    new DanskeBankAccountIdentifierFormatter();
            SwedishIdentifier parsedIdentifier =
                    identifierFormatter.parseSwedishIdentifier(bankId, accountNumber);

            AccountIdentifier expectedIdentifier = new SwedishIdentifier("16034332648");
            assertThat(parsedIdentifier).isEqualTo(expectedIdentifier);
        }

        @Test
        public void nordeaPersonkonto() {
            String accountNumber = "8401141935";
            String bankId = "NORDEA - PERSONKONTON";

            DanskeBankAccountIdentifierFormatter identifierFormatter =
                    new DanskeBankAccountIdentifierFormatter();
            SwedishIdentifier parsedIdentifier =
                    identifierFormatter.parseSwedishIdentifier(bankId, accountNumber);

            AccountIdentifier expectedIdentifier = new SwedishIdentifier("33008401141935");
            assertThat(parsedIdentifier).isEqualTo(expectedIdentifier);
        }
    }
}
