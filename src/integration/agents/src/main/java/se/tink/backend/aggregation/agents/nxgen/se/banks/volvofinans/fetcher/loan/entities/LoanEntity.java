package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.loan.entities;

import static se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.VolvoFinansConstants.Values.SEK;
import static se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails.Type.VEHICLE;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Getter
public class LoanEntity {

    @JsonProperty("avtal")
    private String agreement;

    @JsonProperty("avtalSuffix")
    private String agreementSuffix;

    private String regnr;

    @JsonProperty("produktbeskrivning")
    private String productDescprition;

    @JsonProperty("LanLeaseTagareEntity")
    private String loanTypeName;

    @JsonProperty("lanLeasing")
    private String loanType;

    @JsonProperty("modellbeskrivning")
    private String modelDescription;

    @JsonProperty("arsmodell")
    private String yearModel;

    @JsonProperty("lanLeaseTagare")
    private List<CustomerInfoEntity> customerInfoEntityList;

    @JsonProperty("OCRnummer")
    private String ocrNumber;

    @JsonProperty("betalsatt")
    private String payment;

    private String gironummer;
    private String kontoId;

    @JsonProperty("faktiskKreditfordran")
    private double outstandingDebt;

    @JsonProperty("aktuellLaneranta")
    private Double interestRate;

    @JsonProperty("slutdatumAvtalet")
    private String loanExpirationDate;

    @JsonProperty("saljforetagNamn")
    private String salesCompanyName;

    @JsonProperty("saljforetagOrt")
    private String salesCompanyLocalization;

    @JsonProperty("saljforetagTelnr")
    private String salesCompanyTelNo;

    private String fakturanrSenaste;

    @JsonProperty("fakturadatumSenaste")
    private String lastInvoiceDate;

    @JsonProperty("forfallodatum")
    private String dueDate;

    @JsonProperty("slutsummaAvi")
    private double paymentAmount;

    @JsonProperty("datumSlutlosen")
    private String finalPaymentDate;

    @JsonProperty("senastAviseradePeriod")
    private int latestNotifiedPeriod;

    @JsonProperty("totaltAntalPerioder")
    private int totalPeriodsNumber;

    @JsonProperty("FakturaRaderEntity")
    private List<InvoiceDetailEntity> invoiceDetailEntityList;

    public LoanAccount toTinkLoanAccount() {
        return LoanAccount.nxBuilder()
                .withLoanDetails(
                        LoanModule.builder()
                                .withType(VEHICLE)
                                .withBalance(getBalance())
                                .withInterestRate(getInterestRate(interestRate))
                                .setLoanNumber(agreement)
                                .setNumMonthsBound(totalPeriodsNumber)
                                .setApplicants(getApplicants(customerInfoEntityList))
                                .build())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getAccountNumber())
                                .withAccountNumber(getAccountNumber())
                                .withAccountName(regnr)
                                .addIdentifier(new SwedishIdentifier(agreement))
                                .setProductName(productDescprition)
                                .build())
                .build();
    }

    @JsonIgnore
    private Double getInterestRate(Double interestRate) {
        if (interestRate != null) {
            return interestRate / 100;
        }
        return 0.0;
    }

    @JsonIgnore
    private List<String> getApplicants(List<CustomerInfoEntity> list) {
        List<String> applicants = new ArrayList<>();
        list.forEach(customerInfoEntity -> applicants.add(customerInfoEntity.getCustomerName()));
        return applicants;
    }

    @JsonIgnore
    private ExactCurrencyAmount getBalance() {
        ExactCurrencyAmount balance = ExactCurrencyAmount.of(outstandingDebt, SEK);
        if (balance.getExactValue().compareTo(BigDecimal.ZERO) == 0) {
            return balance;
        }
        return balance.negate();
    }

    @JsonIgnore
    private String getAccountNumber() {
        return String.format(
                "%s-%s",
                agreement,
                getCustomerInfoEntityList().stream().findFirst().get().getCustomerNumber());
    }
}
