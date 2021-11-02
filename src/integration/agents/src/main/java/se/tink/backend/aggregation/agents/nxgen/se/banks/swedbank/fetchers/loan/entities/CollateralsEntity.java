package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.entities;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class CollateralsEntity extends LoanEntity {
    private String description;
    private AmountEntity totalDebt;
    private List<LoanEntity> loans;
    private boolean moreCollateralsExists;
}
