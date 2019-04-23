package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v30.fetcher.rpc.account;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.rpc.BaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v30.fetcher.entities.account.AccountBalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountBalanceV30Response extends BaseResponse<List<AccountBalanceEntity>> {

    public AccountBalanceEntity getBalance() {

        // Convert list of AccountBalanceEntity to map, using type as key.
        Map<UkOpenBankingApiDefinitions.AccountBalanceType, AccountBalanceEntity> balanceTypeMap =
                toMap();

        return UkOpenBankingApiDefinitions.AccountBalanceType.getPreferredBalanceEntity(
                        balanceTypeMap)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Account does not have any balance type that we recognize as useful."));
    }

    private Map<UkOpenBankingApiDefinitions.AccountBalanceType, AccountBalanceEntity> toMap() {
        return getData().stream()
                .collect(Collectors.toMap(AccountBalanceEntity::getType, Function.identity()));
    }
}
