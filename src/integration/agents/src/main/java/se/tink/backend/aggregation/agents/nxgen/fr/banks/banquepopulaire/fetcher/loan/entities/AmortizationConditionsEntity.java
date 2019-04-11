package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.entities.TypeEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.fetcher.transactionalaccounts.entities.MontantEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AmortizationConditionsEntity {
    @JsonProperty("dateProchaineEcheance")
    @JsonFormat(pattern = "yyyy-MM-dd'T'hh:mm:ss")
    private Date nextDueDate;

    @JsonProperty("montantProchaineEcheance")
    private MontantEntity amountDueNext;

    @JsonProperty("tauxNominal")
    private double nominalRate;

    @JsonProperty("typeRemboursement")
    private TypeEntity refundType;

    public double getNominalRate() {
        return nominalRate;
    }

    public Date getNextDueDate() {
        return nextDueDate;
    }
}
