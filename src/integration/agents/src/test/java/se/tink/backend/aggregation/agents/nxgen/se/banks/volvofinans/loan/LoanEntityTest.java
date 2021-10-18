package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.loan;

import static org.assertj.core.api.Assertions.assertThat;
import static se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails.Type.VEHICLE;

import java.util.Collections;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.loan.entities.LoanEntity;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class LoanEntityTest {

    @Test
    public void shouldMapGivenLoanToTinkLoanAccount() {
        // given
        LoanEntity loanEntity = getLoanEntity();

        // when
        LoanAccount result = loanEntity.toTinkLoanAccount();
        LoanAccount loanAccount = getLoanAccount(0.0335);

        // then
        assertThat(result).usingRecursiveComparison().isEqualTo(loanAccount);
    }

    @Test
    public void shouldMapGivenLoanWithNullInterestRateToTinkLoanAccount() {
        // given
        LoanEntity loanEntity = getLoanEntityWithNullInterestRate();

        // when
        LoanAccount result = loanEntity.toTinkLoanAccount();
        LoanAccount loanAccount = getLoanAccount(0.0);

        // then
        assertThat(result).usingRecursiveComparison().isEqualTo(loanAccount);
    }

    private LoanEntity getLoanEntityWithNullInterestRate() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"avtal\": \"CAR74B\",\n"
                        + "    \"avtalSuffix\": \"1\",\n"
                        + "    \"regnr\": \"CAR74B\",\n"
                        + "    \"produktbeskrivning\": \"Kreditköp Avbetalning\",\n"
                        + "    \"lanLeasing\": \"LAN\",\n"
                        + "    \"modellbeskrivning\": \"V90\",\n"
                        + "    \"arsmodell\": \"2020\",\n"
                        + "    \"lanLeaseTagare\": [\n"
                        + "        {\n"
                        + "            \"kundnamn\": \"Firstname Lastname\",\n"
                        + "            \"kundnummer\": \"1234567890\"\n"
                        + "        }\n"
                        + "    ],\n"
                        + "    \"OCRnummer\": \"86636811415\",\n"
                        + "    \"betalsatt\": \"Kortkonto\",\n"
                        + "    \"gironummer\": null,\n"
                        + "    \"kontoId\": \"kontoId1\",\n"
                        + "    \"faktiskKreditfordran\": 240163.00,\n"
                        + "    \"aktuellLaneranta\": null,\n"
                        + "    \"slutdatumAvtalet\": \"2027-06-30\",\n"
                        + "    \"saljforetagNamn\": \"VOLVOFINANS BANK AB\",\n"
                        + "    \"saljforetagOrt\": \"GÖTEBORG\",\n"
                        + "    \"saljforetagTelnr\": \"031 - 83 89 30\",\n"
                        + "    \"fakturanrSenaste\": \"866368114\",\n"
                        + "    \"fakturadatumSenaste\": \"2021-10-01\",\n"
                        + "    \"forfallodatum\": \"2021-10-29\",\n"
                        + "    \"slutsummaAvi\": 3831.00,\n"
                        + "    \"datumSlutlosen\": null,\n"
                        + "    \"senastAviseradePeriod\": 4,\n"
                        + "    \"totaltAntalPerioder\": 72,\n"
                        + "    \"fakturaRader\": [\n"
                        + "        {\n"
                        + "            \"fakturaText\": \"Amortering 20211001-20211031\",\n"
                        + "            \"fakturaBelopp\": 3161.00\n"
                        + "        },\n"
                        + "        {\n"
                        + "            \"fakturaText\": \"Ränta\",\n"
                        + "            \"fakturaBelopp\": 670.00\n"
                        + "        }\n"
                        + "    ]\n"
                        + "}",
                LoanEntity.class);
    }

    private LoanEntity getLoanEntity() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"avtal\": \"CAR74B\",\n"
                        + "    \"avtalSuffix\": \"1\",\n"
                        + "    \"regnr\": \"CAR74B\",\n"
                        + "    \"produktbeskrivning\": \"Kreditköp Avbetalning\",\n"
                        + "    \"lanLeasing\": \"LAN\",\n"
                        + "    \"modellbeskrivning\": \"V90\",\n"
                        + "    \"arsmodell\": \"2020\",\n"
                        + "    \"lanLeaseTagare\": [\n"
                        + "        {\n"
                        + "            \"kundnamn\": \"Firstname Lastname\",\n"
                        + "            \"kundnummer\": \"1234567890\"\n"
                        + "        }\n"
                        + "    ],\n"
                        + "    \"OCRnummer\": \"86636811415\",\n"
                        + "    \"betalsatt\": \"Kortkonto\",\n"
                        + "    \"gironummer\": null,\n"
                        + "    \"kontoId\": \"kontoId1\",\n"
                        + "    \"faktiskKreditfordran\": 240163.00,\n"
                        + "    \"aktuellLaneranta\": 3.35,\n"
                        + "    \"slutdatumAvtalet\": \"2027-06-30\",\n"
                        + "    \"saljforetagNamn\": \"VOLVOFINANS BANK AB\",\n"
                        + "    \"saljforetagOrt\": \"GÖTEBORG\",\n"
                        + "    \"saljforetagTelnr\": \"031 - 83 89 30\",\n"
                        + "    \"fakturanrSenaste\": \"866368114\",\n"
                        + "    \"fakturadatumSenaste\": \"2021-10-01\",\n"
                        + "    \"forfallodatum\": \"2021-10-29\",\n"
                        + "    \"slutsummaAvi\": 3831.00,\n"
                        + "    \"datumSlutlosen\": null,\n"
                        + "    \"senastAviseradePeriod\": 4,\n"
                        + "    \"totaltAntalPerioder\": 72,\n"
                        + "    \"fakturaRader\": [\n"
                        + "        {\n"
                        + "            \"fakturaText\": \"Amortering 20211001-20211031\",\n"
                        + "            \"fakturaBelopp\": 3161.00\n"
                        + "        },\n"
                        + "        {\n"
                        + "            \"fakturaText\": \"Ränta\",\n"
                        + "            \"fakturaBelopp\": 670.00\n"
                        + "        }\n"
                        + "    ]\n"
                        + "}",
                LoanEntity.class);
    }

    private LoanAccount getLoanAccount(double interestRate) {
        return LoanAccount.nxBuilder()
                .withLoanDetails(
                        LoanModule.builder()
                                .withType(VEHICLE)
                                .withBalance(ExactCurrencyAmount.of(-240163.00, "SEK"))
                                .withInterestRate(interestRate)
                                .setLoanNumber("CAR74B")
                                .setNumMonthsBound(72)
                                .setApplicants(Collections.singletonList("Firstname Lastname"))
                                .build())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier("CAR74B-1234567890")
                                .withAccountNumber("CAR74B-1234567890")
                                .withAccountName("CAR74B")
                                .addIdentifier(new SwedishIdentifier("CAR74B"))
                                .setProductName("Kreditköp Avbetalning")
                                .build())
                .build();
    }
}
