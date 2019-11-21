package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response.AccountDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response.AccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response.ContextEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response.HeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response.HeaderEntityWrapper;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.BodyEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetAccountsResponse extends HeaderEntityWrapper {

    @JsonProperty("Body")
    private BodyEntity body;

    public BodyEntity getBody() {
        return body;
    }

    public Collection<AccountDetailsEntity> getAccountDetailsEntities() {
        return Optional.of(getHeader())
                .map(HeaderEntity::getContext)
                .map(ContextEntity::getAccounts)
                .map(AccountsEntity::getList)
                .orElse(Collections.emptyList());
    }
}
