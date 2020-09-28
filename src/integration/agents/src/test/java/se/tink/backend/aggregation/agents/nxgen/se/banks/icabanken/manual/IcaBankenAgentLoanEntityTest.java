package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.manual;

import static java.math.BigDecimal.valueOf;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans.entities.LoansBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans.rpc.LoanOverviewResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class IcaBankenAgentLoanEntityTest {
    private static final String FIRST_INTEREST_RATE_SINGLE_INTEREST_RATE_RESPONSE =
            "{\"Body\":{\"LoanList\":{\"Loans\":[{\"LoanNumber\":\"12341231233\",\"Type\":\"ICA Privatlån\",\"PresentDebt\":\"179 112,31 kr\",\"LoanDetails\":[{\"Key\":\"Lån\",\"Value\":\"ICA Privatlån, 1234-123 123 3\"},{\"Key\":\"Låntagare\",\"Value\":\"Firstname Lastname och Firstname2 Lastname\"},{\"Key\":\"Debiteringskonto\",\"Value\":\"1234-123 123 1\"},{\"Key\":\"Aktuell skuld\",\"Value\":\"179 112,31 kr\"},{\"Key\":\"Ursprunglig skuld\",\"Value\":\"224 000,00 kr\"},{\"Key\":\"Aktuell räntesats\",\"Value\":\"3,88 %\"},{\"Key\":\"Utbetalningsdag\",\"Value\":\"2019-09-24\"},{\"Key\":\"Slutbetalningsdag\",\"Value\":\"2023-09-30\"},{\"Key\":\"Låneskydd\",\"Value\":\"Nej\"}],\"InterestRatesDetails\":[{\"Key\":\"2019-12-23\",\"Value\":\"3,88 %\"}]}],\"TotalDebt\":179112.31},\"MortgageList\":{\"Mortgages\":[],\"TotalDebt\":0.0}},\"ResponseStatus\":{\"Code\":0,\"ServerMessage\":\"\",\"ClientMessage\":\"\"}}";

    private static final String FIRST_INTEREST_RATE_SINGLE_INTEREST_RATE_FUTURE_RESPONSE =
            "{\"Body\":{\"LoanList\":{\"Loans\":[{\"LoanNumber\":\"12341231233\",\"Type\":\"ICA Privatlån\",\"PresentDebt\":\"179 112,31 kr\",\"LoanDetails\":[{\"Key\":\"Lån\",\"Value\":\"ICA Privatlån, 1234-123 123 3\"},{\"Key\":\"Låntagare\",\"Value\":\"Firstname Lastname och Firstname2 Lastname\"},{\"Key\":\"Debiteringskonto\",\"Value\":\"1234-123 123 1\"},{\"Key\":\"Aktuell skuld\",\"Value\":\"179 112,31 kr\"},{\"Key\":\"Ursprunglig skuld\",\"Value\":\"224 000,00 kr\"},{\"Key\":\"Aktuell räntesats\",\"Value\":\"3,88 %\"},{\"Key\":\"Utbetalningsdag\",\"Value\":\"2019-09-24\"},{\"Key\":\"Slutbetalningsdag\",\"Value\":\"2023-09-30\"},{\"Key\":\"Låneskydd\",\"Value\":\"Nej\"}],\"InterestRatesDetails\":[{\"Key\":\"2021-12-23\",\"Value\":\"3,88 %\"}]}],\"TotalDebt\":179112.31},\"MortgageList\":{\"Mortgages\":[],\"TotalDebt\":0.0}},\"ResponseStatus\":{\"Code\":0,\"ServerMessage\":\"\",\"ClientMessage\":\"\"}}";

    private static final String FIRST_INTEREST_RATE_DOUBLE_INTEREST_RATE_RESPONSE =
            "{\"Body\":{\"LoanList\":{\"Loans\":[{\"LoanNumber\":\"12341231233\",\"Type\":\"ICA Privatlån\",\"PresentDebt\":\"179 112,31 kr\",\"LoanDetails\":[{\"Key\":\"Lån\",\"Value\":\"ICA Privatlån, 1234-123 123 3\"},{\"Key\":\"Låntagare\",\"Value\":\"Firstname Lastname och Firstname2 Lastname\"},{\"Key\":\"Debiteringskonto\",\"Value\":\"1234-123 123 1\"},{\"Key\":\"Aktuell skuld\",\"Value\":\"179 112,31 kr\"},{\"Key\":\"Ursprunglig skuld\",\"Value\":\"224 000,00 kr\"},{\"Key\":\"Aktuell räntesats\",\"Value\":\"3,88 %\"},{\"Key\":\"Utbetalningsdag\",\"Value\":\"2019-09-24\"},{\"Key\":\"Slutbetalningsdag\",\"Value\":\"2023-09-30\"},{\"Key\":\"Låneskydd\",\"Value\":\"Nej\"}],\"InterestRatesDetails\":[{\"Key\":\"2019-12-23\",\"Value\":\"3,88 %\"},{\"Key\":\"2019-09-24 till 2019-12-23\",\"Value\":\"3,63 %\"}]}],\"TotalDebt\":179112.31},\"MortgageList\":{\"Mortgages\":[],\"TotalDebt\":0.0}},\"ResponseStatus\":{\"Code\":0,\"ServerMessage\":\"\",\"ClientMessage\":\"\"}}\n";

    private static final String SECOND_INTEREST_RATE_DOUBLE_INTEREST_RATE_RESPONSE =
            "{\"Body\":{\"LoanList\":{\"Loans\":[{\"LoanNumber\":\"12341231233\",\"Type\":\"ICA Privatlån\",\"PresentDebt\":\"179 112,31 kr\",\"LoanDetails\":[{\"Key\":\"Lån\",\"Value\":\"ICA Privatlån, 1234-123 123 3\"},{\"Key\":\"Låntagare\",\"Value\":\"Firstname Lastname och Firstname2 Lastname\"},{\"Key\":\"Debiteringskonto\",\"Value\":\"1234-123 123 1\"},{\"Key\":\"Aktuell skuld\",\"Value\":\"179 112,31 kr\"},{\"Key\":\"Ursprunglig skuld\",\"Value\":\"224 000,00 kr\"},{\"Key\":\"Aktuell räntesats\",\"Value\":\"3,88 %\"},{\"Key\":\"Utbetalningsdag\",\"Value\":\"2019-09-24\"},{\"Key\":\"Slutbetalningsdag\",\"Value\":\"2023-09-30\"},{\"Key\":\"Låneskydd\",\"Value\":\"Nej\"}],\"InterestRatesDetails\":[{\"Key\":\"2021-12-23\",\"Value\":\"3,88 %\"},{\"Key\":\"2019-09-24 till 2021-12-23\",\"Value\":\"3,63 %\"}]}],\"TotalDebt\":179112.31},\"MortgageList\":{\"Mortgages\":[],\"TotalDebt\":0.0}},\"ResponseStatus\":{\"Code\":0,\"ServerMessage\":\"\",\"ClientMessage\":\"\"}}";

    private static final String FIRST_INTEREST_RATE_TRIPLE_INTEREST_RATE_RESPONSE =
            "{\"Body\":{\"LoanList\":{\"Loans\":[{\"LoanNumber\":\"12341231233\",\"Type\":\"ICA Privatlån\",\"PresentDebt\":\"179 112,31 kr\",\"LoanDetails\":[{\"Key\":\"Lån\",\"Value\":\"ICA Privatlån, 1234-123 123 3\"},{\"Key\":\"Låntagare\",\"Value\":\"Firstname Lastname och Firstname2 Lastname\"},{\"Key\":\"Debiteringskonto\",\"Value\":\"1234-123 123 1\"},{\"Key\":\"Aktuell skuld\",\"Value\":\"179 112,31 kr\"},{\"Key\":\"Ursprunglig skuld\",\"Value\":\"224 000,00 kr\"},{\"Key\":\"Aktuell räntesats\",\"Value\":\"3,88 %\"},{\"Key\":\"Utbetalningsdag\",\"Value\":\"2019-09-24\"},{\"Key\":\"Slutbetalningsdag\",\"Value\":\"2023-09-30\"},{\"Key\":\"Låneskydd\",\"Value\":\"Nej\"}],\"InterestRatesDetails\":[{\"Key\":\"2019-12-23\",\"Value\":\"3,88 %\"},{\"Key\":\"2018-12-23 till 2019-12-23\",\"Value\":\"3,74 %\"},{\"Key\":\"2018-09-24 till 2018-12-23\",\"Value\":\"3,63 %\"}]}],\"TotalDebt\":179112.31},\"MortgageList\":{\"Mortgages\":[],\"TotalDebt\":0.0}},\"ResponseStatus\":{\"Code\":0,\"ServerMessage\":\"\",\"ClientMessage\":\"\"}}";

    private static final String SECOND_INTEREST_RATE_TRIPLE_INTEREST_RATE_RESPONSE =
            "{\"Body\":{\"LoanList\":{\"Loans\":[{\"LoanNumber\":\"12341231233\",\"Type\":\"ICA Privatlån\",\"PresentDebt\":\"179 112,31 kr\",\"LoanDetails\":[{\"Key\":\"Lån\",\"Value\":\"ICA Privatlån, 1234-123 123 3\"},{\"Key\":\"Låntagare\",\"Value\":\"Firstname Lastname och Firstname2 Lastname\"},{\"Key\":\"Debiteringskonto\",\"Value\":\"1234-123 123 1\"},{\"Key\":\"Aktuell skuld\",\"Value\":\"179 112,31 kr\"},{\"Key\":\"Ursprunglig skuld\",\"Value\":\"224 000,00 kr\"},{\"Key\":\"Aktuell räntesats\",\"Value\":\"3,88 %\"},{\"Key\":\"Utbetalningsdag\",\"Value\":\"2019-09-24\"},{\"Key\":\"Slutbetalningsdag\",\"Value\":\"2023-09-30\"},{\"Key\":\"Låneskydd\",\"Value\":\"Nej\"}],\"InterestRatesDetails\":[{\"Key\":\"2021-12-23\",\"Value\":\"3,88 %\"},{\"Key\":\"2018-12-23 till 2021-12-23\",\"Value\":\"3,74 %\"},{\"Key\":\"2018-09-24 till 2018-12-23\",\"Value\":\"3,63 %\"}]}],\"TotalDebt\":179112.31},\"MortgageList\":{\"Mortgages\":[],\"TotalDebt\":0.0}},\"ResponseStatus\":{\"Code\":0,\"ServerMessage\":\"\",\"ClientMessage\":\"\"}}";

    private static final String THIRD_INTEREST_RATE_TRIPLE_INTEREST_RATE_RESPONSE =
            "{\"Body\":{\"LoanList\":{\"Loans\":[{\"LoanNumber\":\"12341231233\",\"Type\":\"ICA Privatlån\",\"PresentDebt\":\"179 112,31 kr\",\"LoanDetails\":[{\"Key\":\"Lån\",\"Value\":\"ICA Privatlån, 1234-123 123 3\"},{\"Key\":\"Låntagare\",\"Value\":\"Firstname Lastname och Firstname2 Lastname\"},{\"Key\":\"Debiteringskonto\",\"Value\":\"1234-123 123 1\"},{\"Key\":\"Aktuell skuld\",\"Value\":\"179 112,31 kr\"},{\"Key\":\"Ursprunglig skuld\",\"Value\":\"224 000,00 kr\"},{\"Key\":\"Aktuell räntesats\",\"Value\":\"3,88 %\"},{\"Key\":\"Utbetalningsdag\",\"Value\":\"2019-09-24\"},{\"Key\":\"Slutbetalningsdag\",\"Value\":\"2023-09-30\"},{\"Key\":\"Låneskydd\",\"Value\":\"Nej\"}],\"InterestRatesDetails\":[{\"Key\":\"2022-12-23\",\"Value\":\"3,88 %\"},{\"Key\":\"2021-12-23 till 2022-12-23\",\"Value\":\"3,74 %\"},{\"Key\":\"2018-09-24 till 2021-12-23\",\"Value\":\"3,63 %\"}]}],\"TotalDebt\":179112.31},\"MortgageList\":{\"Mortgages\":[],\"TotalDebt\":0.0}},\"ResponseStatus\":{\"Code\":0,\"ServerMessage\":\"\",\"ClientMessage\":\"\"}}";

    @Test
    public void testFirstInterestRateSingle() {
        LoansBodyEntity loansBodyEntity =
                SerializationUtils.deserializeFromString(
                                FIRST_INTEREST_RATE_SINGLE_INTEREST_RATE_RESPONSE,
                                LoanOverviewResponse.class)
                        .getBody();
        Assert.assertEquals(
                valueOf(3.88),
                valueOf(loansBodyEntity.getLoanList().getLoans().get(0).getInterestRate()));
    }

    @Test
    public void testFirstInterestRateSingleFuture() {
        LoansBodyEntity loansBodyEntity =
                SerializationUtils.deserializeFromString(
                                FIRST_INTEREST_RATE_SINGLE_INTEREST_RATE_FUTURE_RESPONSE,
                                LoanOverviewResponse.class)
                        .getBody();
        Assert.assertEquals(
                valueOf(0.0),
                valueOf(loansBodyEntity.getLoanList().getLoans().get(0).getInterestRate()));
    }

    @Test
    public void testFirstInterestRateDouble() {
        LoansBodyEntity loansBodyEntity =
                SerializationUtils.deserializeFromString(
                                FIRST_INTEREST_RATE_DOUBLE_INTEREST_RATE_RESPONSE,
                                LoanOverviewResponse.class)
                        .getBody();
        Assert.assertEquals(
                valueOf(3.88),
                valueOf(loansBodyEntity.getLoanList().getLoans().get(0).getInterestRate()));
    }

    @Test
    public void testSecondInterestRateDouble() {
        LoansBodyEntity loansBodyEntity =
                SerializationUtils.deserializeFromString(
                                SECOND_INTEREST_RATE_DOUBLE_INTEREST_RATE_RESPONSE,
                                LoanOverviewResponse.class)
                        .getBody();
        Assert.assertEquals(
                valueOf(3.63),
                valueOf(loansBodyEntity.getLoanList().getLoans().get(0).getInterestRate()));
    }

    @Test
    public void testFirstInterestRateTriple() {
        LoansBodyEntity loansBodyEntity =
                SerializationUtils.deserializeFromString(
                                FIRST_INTEREST_RATE_TRIPLE_INTEREST_RATE_RESPONSE,
                                LoanOverviewResponse.class)
                        .getBody();
        Assert.assertEquals(
                valueOf(3.88),
                valueOf(loansBodyEntity.getLoanList().getLoans().get(0).getInterestRate()));
    }

    @Test
    public void testSecondInterestRateTriple() {
        LoansBodyEntity loansBodyEntity =
                SerializationUtils.deserializeFromString(
                                SECOND_INTEREST_RATE_TRIPLE_INTEREST_RATE_RESPONSE,
                                LoanOverviewResponse.class)
                        .getBody();
        Assert.assertEquals(
                valueOf(3.74),
                valueOf(loansBodyEntity.getLoanList().getLoans().get(0).getInterestRate()));
    }

    @Test
    public void testThirdInterestRateTriple() {
        LoansBodyEntity loansBodyEntity =
                SerializationUtils.deserializeFromString(
                                THIRD_INTEREST_RATE_TRIPLE_INTEREST_RATE_RESPONSE,
                                LoanOverviewResponse.class)
                        .getBody();
        Assert.assertEquals(
                valueOf(3.63),
                valueOf(loansBodyEntity.getLoanList().getLoans().get(0).getInterestRate()));
    }
}
