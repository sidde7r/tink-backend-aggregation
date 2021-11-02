package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.rpc;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class CollateralsEntity {
    private String description;
    private List<LoanEntity> loans;
    private boolean moreCollateralsExists;
    private AmountEntity totalDebt;
}
