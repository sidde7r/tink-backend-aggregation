package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanEntity {
    private String name;
    private String id;
    private LoanResponseAccountEntity account;
    private String interestRate;
    private AmountEntity debt;
    private LinksEntity links;

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public LoanResponseAccountEntity getAccount() {
        return account;
    }

    public String getInterestRate() {
        return interestRate;
    }

    public AmountEntity getDebt() {
        return debt;
    }

    public LinksEntity getLinks() {
        return links;
    }
}
