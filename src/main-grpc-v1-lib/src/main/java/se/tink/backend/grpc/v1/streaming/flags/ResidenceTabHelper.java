package se.tink.backend.grpc.v1.streaming.flags;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountTypes;
import se.tink.backend.core.Market;
import se.tink.backend.core.User;
import se.tink.backend.main.controllers.IdentityServiceController;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.identity.commands.GetIdentityStateCommand;
import se.tink.libraries.identity.model.Identity;

/**
 * Utility class that decides if the users should see the residence tab
 */
public class ResidenceTabHelper {
    private static final LogUtils LOG = new LogUtils(ResidenceTabHelper.class);

    private static final Set<AccountTypes> LOAN_ACCOUNT_TYPES = ImmutableSet
            .of(AccountTypes.LOAN, AccountTypes.MORTGAGE);

    private final IdentityServiceController identityServiceController;

    @Inject
    public ResidenceTabHelper(IdentityServiceController identityServiceController) {
        this.identityServiceController = identityServiceController;
    }

    /**
     * Calculates if the residence tab should be displayed or not.
     * 1. Needs to be one Swedish market.
     * 2. Needs to have at least on loan or mortgage.
     * 3. Needs to have a registered address.
     */
    boolean shouldDisplayResidenceTab(User user, List<Account> accounts) {
        // User needs to be on the Swedish market
        if (user.getProfile().getMarketAsCode() != Market.Code.SE) {
            LOG.debug(user.getId(), String.format("not display residence tab for market code is %s, not SE",
                    user.getProfile().getMarketAsCode()));
            return false;
        }

        // User needs to have at least one loan or one mortgage
        if (!hasLoanAccount(accounts)) {
            LOG.debug(user.getId(), "not display residence tab for no loan accounts found");
            return false;
        }

        GetIdentityStateCommand command = new GetIdentityStateCommand(user.getId());
        Optional<Identity> state = identityServiceController.getIdentityState(command);

        // Show the tab if the user has an address
        boolean hasAddress = state.isPresent() && state.get().getAddress() != null;
        if (!hasAddress) {
            LOG.debug(user.getId(), "not display residence tab for address identity not found");
        }
        return hasAddress;
    }

    boolean hasLoanAccount(List<Account> accounts) {
        return accounts.stream().anyMatch(a -> LOAN_ACCOUNT_TYPES.contains(a.getType()));
    }
}
