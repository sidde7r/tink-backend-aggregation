package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts.dto.responses.accounts;

import java.util.List;
import javax.annotation.Nullable;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts.dto.responses.common.AccountTypeEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.accounts.dto.responses.common.PsuRelationsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AccountsEntity {
    private String accountNumber;
    private String accountTypeName;
    private AccountTypeEntity accountType;
    @Nullable private List<PsuRelationsEntity> psuRelations;
}
