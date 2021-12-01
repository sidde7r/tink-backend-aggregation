package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class AccountResponse {
    @JsonProperty("accounts")
    private List<AccountEntity> accountList;
}
