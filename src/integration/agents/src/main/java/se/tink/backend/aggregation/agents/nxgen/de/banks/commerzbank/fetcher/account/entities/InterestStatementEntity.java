package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.account.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InterestStatementEntity {
    private String interestEvaluationTerm;
    private String interestBalancingTerm;

    public String getInterestEvaluationTerm() {
        return interestEvaluationTerm;
    }

    public String getInterestBalancingTerm() {
        return interestBalancingTerm;
    }
}
