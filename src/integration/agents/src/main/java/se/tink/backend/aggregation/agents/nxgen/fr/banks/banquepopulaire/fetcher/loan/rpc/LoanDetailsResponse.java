package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.fetcher.loan.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.BanquePopulaireConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.entities.ContractOverviewEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.fetcher.loan.entities.CreditEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;

@JsonObject
public class LoanDetailsResponse {
    @JsonProperty("libelleProduit")
    private String productLabel;
    @JsonProperty("refExterneContrat")
    private String externalReference;
    private String statutJuridique;
    private boolean topAffichageMontantAuto;
    @JsonProperty("dateOuverture")
    @JsonFormat(pattern = "yyyy-MM-dd'T'hh:mm:ss")
    private Date openingDate;
    private String dateEcheance;
    private CreditEntity credit;

    @JsonIgnore
    public LoanAccount toTinkLoanAccount(ContractOverviewEntity account) {
        String holderName = account.getClient().getDescriptionClient();
        return LoanAccount.builder(externalReference, account.getBalance().toTinkAmount().negate())
                .setInterestRate(credit.getAmortizationsConditions().getNominalRate()/100.0)
                .setAccountNumber(externalReference)
                .setName(productLabel)
                .setHolderName(new HolderName(holderName))
                .setDetails(
                        LoanDetails.builder(BanquePopulaireConstants.Loan.toTinkLoanType(account.getProductId().getCode()))
                        .setApplicants(ImmutableList.of(holderName))
                        .setLoanNumber(externalReference)
                        .setInitialDate(openingDate)
                        .setNextDayOfTermsChange(credit.getAmortizationsConditions().getNextDueDate())
                                .build()
                ).build();
    }
}
