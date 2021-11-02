package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.entities;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class LoanEntity {
    private String name;
    private String id;
    private LoanResponseAccountEntity account;
    private String interestRate;
    private AmountEntity debt;
    private LinksEntity links;
    private String type;
}
