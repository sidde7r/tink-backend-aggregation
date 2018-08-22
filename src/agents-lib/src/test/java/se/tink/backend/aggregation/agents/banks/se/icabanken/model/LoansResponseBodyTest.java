package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import se.tink.backend.system.rpc.Loan;
import java.io.IOException;
import java.text.ParseException;

import static org.assertj.core.api.Assertions.assertThat;

public class LoansResponseBodyTest {
    private static final String SERIALIZED_LOANS_RESPONSE_BODY_ITEM = "{\"LoanList\":{\"Loans\":[{\"LoanNumber\":\"1337\",\"Type\":\"ICA Kundlån anställd\",\"PresentDebt\":\"31 250,00 kr\",\"LoanDetails\":[{\"Key\":\"Lån\",\"Value\":\"ICA Kundlån anställd, 1337\"},{\"Key\":\"Låntagare\",\"Value\":\"Förnamn Efternamn\"},{\"Key\":\"Debiteringskonto\",\"Value\":\"1337\"},{\"Key\":\"Aktuell skuld\",\"Value\":\"31 250,00 kr\"},{\"Key\":\"Ursprunglig skuld\",\"Value\":\"60 000,00 kr\"},{\"Key\":\"Aktuell räntesats\",\"Value\":\"3,59 %\"},{\"Key\":\"Utbetalningsdag\",\"Value\":\"2014-09-17\"},{\"Key\":\"Slutbetalningsdag\",\"Value\":\"2018-09-30\"},{\"Key\":\"Låneskydd\",\"Value\":\"Nej\"}],\"InterestRatesDetails\":[{\"Key\":\"2014-11-14\",\"Value\":\"3,59 %\"},{\"Key\":\"2014-09-17 till 2014-11-14\",\"Value\":\"3,84 %\"}]},{\"LoanNumber\":\"31337\",\"Type\":\"ICA Kundlån anställd\",\"PresentDebt\":\"66 666,64 kr\",\"LoanDetails\":[{\"Key\":\"Lån\",\"Value\":\"ICA Kundlån anställd, 31337\"},{\"Key\":\"Låntagare\",\"Value\":\"Förnamn Efternamn\"},{\"Key\":\"Debiteringskonto\",\"Value\":\"1337\"},{\"Key\":\"Aktuell skuld\",\"Value\":\"66 666,64 kr\"},{\"Key\":\"Ursprunglig skuld\",\"Value\":\"80 000,00 kr\"},{\"Key\":\"Aktuell räntesats\",\"Value\":\"3,59 %\"},{\"Key\":\"Utbetalningsdag\",\"Value\":\"2015-12-07\"},{\"Key\":\"Slutbetalningsdag\",\"Value\":\"2019-12-30\"},{\"Key\":\"Låneskydd\",\"Value\":\"Nej\"}],\"InterestRatesDetails\":[{\"Key\":\"2015-12-07\",\"Value\":\"3,59 %\"}]}]},\"MortgageList\":{\"Mortgages\":[{\"MortgageNumber\":\"31337\",\"Deeds\":\"Stad Brf Objektsnummer\",\"PresentDebt\":\"844 138,00 kr\",\"MortgageDetails\":[{\"Key\":\"Lånenummer\",\"Value\":\"31337\"},{\"Key\":\"Säkerhet\",\"Value\":\"Stad Brf Objektsnummer\"},{\"Key\":\"Typ av objekt\",\"Value\":\"Bostadsrätt (bostadsrättsförening)\"},{\"Key\":\"Typ av lån\",\"Value\":\"Bolån, bottenlån\"},{\"Key\":\"Låntagare\",\"Value\":\"Förnamn Efternamn\"},{\"Key\":\"Aktuell skuld\",\"Value\":\"844 138,00 kr\"},{\"Key\":\"Ursprunglig skuld\",\"Value\":\"862 750,00 kr\"},{\"Key\":\"Aktuell räntesats\",\"Value\":\"2,04 %\"},{\"Key\":\"Räntebindningstid\",\"Value\":\"Bunden 3 år\"},{\"Key\":\"Nästa villkorsändringsdag\",\"Value\":\"2017-12-13\"}]},{\"MortgageNumber\":\"24084647\",\"Deeds\":\"Stad Brf Objektsnummer2\",\"PresentDebt\":\"844 138,00 kr\",\"MortgageDetails\":[{\"Key\":\"Lånenummer\",\"Value\":\"24084647\"},{\"Key\":\"Säkerhet\",\"Value\":\"Stad Brf Objektsnummer\"},{\"Key\":\"Typ av objekt\",\"Value\":\"Bostadsrätt (bostadsrättsförening)\"},{\"Key\":\"Typ av lån\",\"Value\":\"Bolån, bottenlån\"},{\"Key\":\"Låntagare\",\"Value\":\"Förnamn Efternamn\"},{\"Key\":\"Aktuell skuld\",\"Value\":\"844 138,00 kr\"},{\"Key\":\"Ursprunglig skuld\",\"Value\":\"862 750,00 kr\"},{\"Key\":\"Aktuell räntesats\",\"Value\":\"1,39 %\"},{\"Key\":\"Räntebindningstid\",\"Value\":\"Rörlig 3-mån\"},{\"Key\":\"Nästa villkorsändringsdag\",\"Value\":\"2016-10-12\"}]}]}},\"ResponseStatus\":{\"Code\":0,\"ServerMessage\":\"\",\"ClientMessage\":\"\"}";
    private static final String SERIALIZED_LOANS_RESPONSE_CONTAIN_NULLS = "{\"LoanList\":{\"Loans\":[{\"LoanNumber\":\"\",\"Type\":\"\",\"PresentDebt\":\"\",\"LoanDetails\":[{\"Key\":\"\",\"Value\":\"ICA Kundlån anställd, 1337\"},{\"Key\":\"\",\"Value\":\"Förnamn Efternamn\"},{\"Key\":\"Debiteringskonto\",\"Value\":\"31337\"},{\"Key\":\"Aktuell skuld\",\"Value\":\"31 250,00 kr\"},{\"Key\":\"\",\"Value\":\"60 000,00 kr\"},{\"Key\":\"\",\"Value\":\"3,59 %\"},{\"Key\":\"\",\"Value\":\"2014-09-17\"},{\"Key\":\"Slutbetalningsdag\",\"Value\":\"2018-09-30\"},{\"Key\":\"Låneskydd\",\"Value\":\"Nej\"}],\"InterestRatesDetails\":[{\"Key\":\"2014-11-14\",\"Value\":\"3,59 %\"},{\"Key\":\"2014-09-17 till 2014-11-14\",\"Value\":\"3,84 %\"}]},{\"LoanNumber\":null,\"Type\":null,\"PresentDebt\":null,\"LoanDetails\":[{\"Key\":\"Lån\",\"Value\":\"\"},{\"Key\":\"Låntagare\",\"Value\":\"\"},{\"Key\":\"Debiteringskonto\",\"Value\":\"31337\"},{\"Key\":\"Aktuell skuld\",\"Value\":\"\"},{\"Key\":\"Ursprunglig skuld\",\"Value\":\"\"},{\"Key\":\"Aktuell räntesats\",\"Value\":\"\"},{\"Key\":\"Utbetalningsdag\",\"Value\":\"\"},{\"Key\":\"Slutbetalningsdag\",\"Value\":\"2019-12-30\"},{\"Key\":\"Låneskydd\",\"Value\":\"Nej\"}],\"InterestRatesDetails\":[{\"Key\":\"2015-12-07\",\"Value\":\"3,59 %\"}]}]},\"MortgageList\":{\"Mortgages\":[{\"MortgageNumber\":\"\",\"Deeds\":\"Göteborg Brf Fåren På Norra Älvstranden Lghnr 22-3453-1-222-2\",\"PresentDebt\":\"\",\"MortgageDetails\":[{\"Key\":\"\",\"Value\":\"24084639\"},{\"Key\":\"\",\"Value\":\"Stad Brf Objektsnummer\"},{\"Key\":\"\",\"Value\":\"Bostadsrätt (bostadsrättsförening)\"},{\"Key\":\"\",\"Value\":\"Bolån, bottenlån\"},{\"Key\":\"\",\"Value\":\"Förnamn Efternamn\"},{\"Key\":\"\",\"Value\":\"844 138,00 kr\"},{\"Key\":\"\",\"Value\":\"862 750,00 kr\"},{\"Key\":\"\",\"Value\":\"2,04 %\"},{\"Key\":\"\",\"Value\":\"Bunden 3 år\"},{\"Key\":\"\",\"Value\":\"2017-12-13\"}]},{\"MortgageNumber\":null,\"Deeds\":\"Stad Brf Objektsnummer\",\"PresentDebt\":null,\"MortgageDetails\":[{\"Key\":\"Lånenummer\",\"Value\":\"\"},{\"Key\":\"Säkerhet\",\"Value\":\"\"},{\"Key\":\"Typ av objekt\",\"Value\":\"\"},{\"Key\":\"Typ av lån\",\"Value\":\"\"},{\"Key\":\"Låntagare\",\"Value\":\"\"},{\"Key\":\"Aktuell skuld\",\"Value\":\"\"},{\"Key\":\"Ursprunglig skuld\",\"Value\":\"\"},{\"Key\":\"Aktuell räntesats\",\"Value\":\"\"},{\"Key\":\"Räntebindningstid\",\"Value\":\"\"},{\"Key\":\"Nästa villkorsändringsdag\",\"Value\":\"\"}]}]}},\"ResponseStatus\":{\"Code\":0,\"ServerMessage\":\"\",\"ClientMessage\":\"\"}";
    @Test
    public void deserializeConvertToLoan() throws IOException, ParseException {
        ObjectMapper objectMapper = new ObjectMapper();
        LoansResponseBody loansResponseBody = objectMapper
                .readValue(SERIALIZED_LOANS_RESPONSE_BODY_ITEM, LoansResponseBody.class);

        LoanListEntity loansList = loansResponseBody.getLoanList();
        MortgageListEntity mortgageList = loansResponseBody.getMortgageList();

        for (LoanEntity loanEntity : loansList.getLoans()) {
            Loan loan = loanEntity.toLoan();

            assertThat(loan.getType()).isNotNull();
            assertThat(loan.getInterest()).isNotNull();
            assertThat(loan.getName()).isNotNull();
            assertThat(loan.getBalance()).isNotNull();
            assertThat(loan.getInitialBalance()).isNotNull();
            assertThat(loan.getInitialDate()).isNotNull();
            assertThat(loan.getLoanNumber()).isNotNull();
            assertThat(loan.getSerializedLoanResponse()).isNotNull();

        }

        for (MortgageEntity mortgageEntity : mortgageList.getMortgages()) {
            Loan loan = mortgageEntity.toLoan();

            assertThat(loan.getType()).isNotNull();
            assertThat(loan.getInterest()).isNotNull();
            assertThat(loan.getName()).isNotNull();
            assertThat(loan.getBalance()).isNotNull();
            assertThat(loan.getInitialBalance()).isNotNull();
            assertThat(loan.getNextDayOfTermsChange()).isNotNull();
            assertThat(loan.getLoanNumber()).isNotNull();
            assertThat(loan.getNumMonthsBound()).isNotNull();
            assertThat(loan.getSerializedLoanResponse()).isNotNull();
        }
    }

    @Test
    public void deserializedNoNullPointerExceptionsTest() throws IOException, ParseException {
        ObjectMapper objectMapper = new ObjectMapper();
        LoansResponseBody loansResponseBody = objectMapper
                .readValue(SERIALIZED_LOANS_RESPONSE_CONTAIN_NULLS, LoansResponseBody.class);

        LoanListEntity loansList = loansResponseBody.getLoanList();
        MortgageListEntity mortgageList = loansResponseBody.getMortgageList();

        for (LoanEntity loanEntity : loansList.getLoans()) {
            Loan loan = loanEntity.toLoan();
        }

        for (MortgageEntity mortgageEntity : mortgageList.getMortgages()) {
            Loan loan = mortgageEntity.toLoan();
        }
    }
}
