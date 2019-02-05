package se.tink.backend.aggregation.agents.nxgen.fr.banks.labanquepostale.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CristalLoansEntity {
    @JsonProperty("problemeTechnique")
    private boolean technicalProblem;
    @JsonProperty("offresCreditRenouvelable")
    private List<RenewableCreditOffersEntity> renewableCreditOffers;
    @JsonProperty("offresPretPersonnel")
    private List<PersonalLoanOffersEntity> personalLoanOffers;
    @JsonProperty("offresPretsEtudiantApprentis")
    private List<StudentLoanStudentLoansEntity> studentLoanStudentLoans;
    @JsonProperty("offresRachatCredit")
    private List<CreditBuybackOffersEntity> creditBuybackOffers;
}
