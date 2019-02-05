package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.entities.TypeEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.fetcher.transactionalaccounts.entities.MontantEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditConditionsEntity {
    private String dateRealisation;
    @JsonProperty("dureePret")
    private int loanDuration;
    @JsonProperty("montantNominal")
    private MontantEntity loanLimit;
    @JsonProperty("montantRestantDebloquer")
    private MontantEntity remainingAmountToWithdraw;
    @JsonProperty("libelleBien")
    private TypeEntity libelleBien;
}
