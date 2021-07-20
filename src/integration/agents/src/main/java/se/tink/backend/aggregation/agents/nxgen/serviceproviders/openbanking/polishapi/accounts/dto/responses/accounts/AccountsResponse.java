package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts.dto.responses.accounts;

import java.util.List;
import javax.annotation.Nullable;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.common.dto.responses.PageInfoEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AccountsResponse {
    private List<AccountsEntity> accounts;
    @Nullable private PageInfoEntity pageInfo;
}
