package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants.Authorization.Common.SCOPE_USAGE_LIMIT_MULTIPLE;

import java.util.Arrays;
import lombok.experimental.UtilityClass;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.dto.requests.common.PrivilegeItemEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.dto.requests.common.PrivilegeItemWithHistoryEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.dto.requests.common.PrivilegeListEntity;

/**
 * There is a lot of redundancy here - but it done on purpose so it is clearer to read privileges
 * depending on the type.
 */
@UtilityClass
public class PolishApiPostPrivlegeListEntityBuilder {

    static PrivilegeListEntity getAisAccountsPrivileges(String scopeUsageLimit) {
        return PrivilegeListEntity.builder()
                .aisAccountsGetAccounts(
                        PrivilegeItemEntity.builder().scopeUsageLimit(scopeUsageLimit).build())
                .build();
    }

    static PrivilegeListEntity getAisPrivileges(int maxDaysToFetch) {
        return PrivilegeListEntity.builder()
                .aisGetAccount(
                        PrivilegeItemEntity.builder()
                                .scopeUsageLimit(SCOPE_USAGE_LIMIT_MULTIPLE)
                                .build())
                .aisGetTransactionsDone(
                        PrivilegeItemWithHistoryEntity.builder()
                                .scopeUsageLimit(SCOPE_USAGE_LIMIT_MULTIPLE)
                                .maxAllowedHistoryLong(maxDaysToFetch)
                                .build())
                .aisGetTransactionsPending(
                        PrivilegeItemWithHistoryEntity.builder()
                                .scopeUsageLimit(SCOPE_USAGE_LIMIT_MULTIPLE)
                                .maxAllowedHistoryLong(maxDaysToFetch)
                                .build())
                .build();
    }

    static PrivilegeListEntity getAisPrivilegesWithAccountNumber(
            String accountNumber, int maxDaysToFetch) {
        return PrivilegeListEntity.builder()
                .accountNumber(Arrays.asList(accountNumber))
                .aisGetAccount(
                        PrivilegeItemEntity.builder()
                                .scopeUsageLimit(SCOPE_USAGE_LIMIT_MULTIPLE)
                                .build())
                .aisGetTransactionsDone(
                        PrivilegeItemWithHistoryEntity.builder()
                                .scopeUsageLimit(SCOPE_USAGE_LIMIT_MULTIPLE)
                                .maxAllowedHistoryLong(maxDaysToFetch)
                                .build())
                .aisGetTransactionsPending(
                        PrivilegeItemWithHistoryEntity.builder()
                                .scopeUsageLimit(SCOPE_USAGE_LIMIT_MULTIPLE)
                                .maxAllowedHistoryLong(maxDaysToFetch)
                                .build())
                .build();
    }

    static PrivilegeListEntity getAisAndAisAccountsPrivileges(
            String aisAccountsScopeUsageLimit, int maxDaysToFetch) {
        return PrivilegeListEntity.builder()
                .aisGetAccount(
                        PrivilegeItemEntity.builder()
                                .scopeUsageLimit(SCOPE_USAGE_LIMIT_MULTIPLE)
                                .build())
                .aisGetTransactionsDone(
                        PrivilegeItemWithHistoryEntity.builder()
                                .scopeUsageLimit(SCOPE_USAGE_LIMIT_MULTIPLE)
                                .maxAllowedHistoryLong(maxDaysToFetch)
                                .build())
                .aisGetTransactionsPending(
                        PrivilegeItemWithHistoryEntity.builder()
                                .scopeUsageLimit(SCOPE_USAGE_LIMIT_MULTIPLE)
                                .maxAllowedHistoryLong(maxDaysToFetch)
                                .build())
                .aisAccountsGetAccounts(
                        PrivilegeItemEntity.builder()
                                .scopeUsageLimit(aisAccountsScopeUsageLimit)
                                .build())
                .build();
    }
}
