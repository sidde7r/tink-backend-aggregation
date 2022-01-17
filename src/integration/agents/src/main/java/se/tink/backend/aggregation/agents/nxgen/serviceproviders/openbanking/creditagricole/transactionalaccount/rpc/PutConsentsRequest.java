package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc;

import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities.AccountIdEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class PutConsentsRequest {

    private List<AccountIdEntity> balances;
    private List<AccountIdEntity> transactions;
    private Boolean trustedBeneficiaries;
    private Boolean psuIdentity;

    public static PutConsentsRequest create(List<AccountIdEntity> accounts) {
        List<AccountIdEntity> transformedAccounts = mapAccountIdsForConsentRequest(accounts);
        return new PutConsentsRequest(transformedAccounts, transformedAccounts, true, true);
    }

    private static List<AccountIdEntity> mapAccountIdsForConsentRequest(
            List<AccountIdEntity> accounts) {
        return accounts.stream()
                .map(AccountIdEntity::getIban)
                .map(AccountIdEntity::createForConsentRequest)
                .collect(Collectors.toList());
    }
}
