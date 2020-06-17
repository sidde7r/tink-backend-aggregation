package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.dto.AccountResourceDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.AccountsPageLinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class FetchAccountsResponse {

    private List<AccountResourceDto> accounts;

    @JsonProperty("_links")
    private AccountsPageLinksEntity links;
}
