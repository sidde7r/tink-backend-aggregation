package se.tink.backend.aggregation.agents.consent.uk;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;
import se.tink.backend.aggregation.agents.consent.Permission;
import se.tink.backend.aggregation.agents.consent.PermissionsMapper;
import se.tink.libraries.credentials.service.RefreshableItem;

public class UkPermissionsMapper implements PermissionsMapper {

    @Override
    public Set<Permission> getPermissions(RefreshableItem item) {
        switch (item) {
            case CHECKING_ACCOUNTS:
            case SAVING_ACCOUNTS:
            case CREDITCARD_ACCOUNTS:
            case LOAN_ACCOUNTS:
            case INVESTMENT_ACCOUNTS:
                return Sets.newHashSet(
                        UkPermission.READ_ACCOUNTS_DETAIL,
                        UkPermission.READ_BALANCES,
                        UkPermission.READ_PARTY);
            case CHECKING_TRANSACTIONS:
            case SAVING_TRANSACTIONS:
            case CREDITCARD_TRANSACTIONS:
            case LOAN_TRANSACTIONS:
            case INVESTMENT_TRANSACTIONS:
                return Sets.newHashSet(
                        UkPermission.READ_TRANSACTIONS_DETAIL,
                        UkPermission.READ_TRANSACTIONS_DEBITS,
                        UkPermission.READ_TRANSACTIONS_CREDITS);
            case IDENTITY_DATA:
                return Sets.newHashSet(UkPermission.READ_PARTY_PSU);
            case LIST_BENEFICIARIES:
            case TRANSFER_DESTINATIONS:
                return Sets.newHashSet(UkPermission.READ_BENEFICIARIES_DETAIL);
            default:
                return Collections.emptySet();
        }
    }
}
