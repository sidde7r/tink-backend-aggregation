package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanDetailsAccountEntity extends AccountEntity {
    @Getter private String interest;
}
