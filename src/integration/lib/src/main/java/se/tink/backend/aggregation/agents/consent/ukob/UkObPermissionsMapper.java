package se.tink.backend.aggregation.agents.consent.ukob;

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Set;
import se.tink.backend.aggregation.agents.consent.Permission;
import se.tink.backend.aggregation.agents.consent.PermissionsMapper;
import se.tink.libraries.credentials.service.RefreshableItem;

public class UkObPermissionsMapper implements PermissionsMapper {

    @Override
    public Set<Permission> getPermissions(RefreshableItem item) {
        switch (item) {
            case CHECKING_ACCOUNTS:
            case SAVING_ACCOUNTS:
            case CREDITCARD_ACCOUNTS:
            case LOAN_ACCOUNTS:
            case INVESTMENT_ACCOUNTS:
                return Sets.newHashSet(
                        UkObPermission.READ_ACCOUNTS_DETAIL,
                        UkObPermission.READ_BALANCES,
                        UkObPermission.READ_PARTY);
            case CHECKING_TRANSACTIONS:
            case SAVING_TRANSACTIONS:
            case CREDITCARD_TRANSACTIONS:
            case LOAN_TRANSACTIONS:
            case INVESTMENT_TRANSACTIONS:
                return Sets.newHashSet(
                        UkObPermission.READ_TRANSACTIONS_DETAIL,
                        UkObPermission.READ_TRANSACTIONS_DEBITS,
                        UkObPermission.READ_TRANSACTIONS_CREDITS);
            case IDENTITY_DATA:
                return Sets.newHashSet(UkObPermission.READ_PARTY_PSU);
            case LIST_BENEFICIARIES:
            case TRANSFER_DESTINATIONS:
                return Sets.newHashSet(UkObPermission.READ_BENEFICIARIES_DETAIL);
            default:
                return Collections.emptySet();
        }
    }
}
