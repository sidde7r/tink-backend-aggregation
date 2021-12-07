package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.fetcher.account.rpc;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.fetcher.account.entities.AccountsItemEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AccountsResponse {

    private List<AccountsItemEntity> accounts;
}
