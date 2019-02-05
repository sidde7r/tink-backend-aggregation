package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans.entities.MortgageEntity;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;

public class MortgageEntityTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private String mortgageEntityString =
            "{\n" +
                "\"MortgageNumber\": \"12345678\",\n" +
                "\"Deeds\": \"BRF Example Address\",\n" +
                "\"PresentDebt\": \"1 000 000,00 kr\",\n" +
                "\"MortgageDetails\": [{\n" +
                    "\"Key\": \"Lånenummer\",\n" +
                    "\"Value\": \"12345678\"\n" +
                "}, {\n" +
                    "\"Key\": \"Säkerhet\",\n" +
                    "\"Value\": \"BRF Example Address\"\n" +
                "}, {\n" +
                    "\"Key\": \"Typ av objekt\",\n" +
                    "\"Value\": \"Bostadsrätt (bostadsrättsförening)\"\n" +
                "}, {\n" +
                    "\"Key\": \"Typ av lån\",\n" +
                    "\"Value\": \"Bolån, bottenlån\"\n" +
                "}, {\n" +
                    "\"Key\": \"Låntagare\",\n" +
                    "\"Value\": \"Firstname Lastname\"\n" +
                "}, {\n" +
                    "\"Key\": \"Aktuell skuld\",\n" +
                    "\"Value\": \"1 000 000,00 kr\"\n" +
                "}, {\n" +
                    "\"Key\": \"Ursprunglig skuld\",\n" +
                    "\"Value\": \"1 500 000,00 kr\"\n" +
                "}, {\n" +
                    "\"Key\": \"Aktuell räntesats\",\n" +
                    "\"Value\": \"1,23 %\"\n" +
                "}, {\n" +
                    "\"Key\": \"Räntebindningstid\",\n" +
                    "\"Value\": \"Rörlig 3-mån\"\n" +
                "}, {\n" +
                    "\"Key\": \"Nästa ränteändringsdag\",\n" +
                    "\"Value\": \"2018-12-01\"\n" +
                "}, {\n" +
                    "\"Key\": \"Nästa villkorsändringsdag\",\n" +
                    "\"Value\": \"2020-01-01\"\n" +
                "}]\n" +
            "}";

    @Test
    public void testMortgageParsing() throws IOException {
        MortgageEntity mortgageEntity = MAPPER.readValue(mortgageEntityString, MortgageEntity.class);

        LoanAccount loanAccount = mortgageEntity.toTinkLoan();
        System.out.println(loanAccount.getBalance());
    }
}
