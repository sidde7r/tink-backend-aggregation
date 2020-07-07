package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.transactionalaccount.entity.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AccountsResponse {
    @JsonProperty("result")
    private List<AccountEntity> accounts;
}
