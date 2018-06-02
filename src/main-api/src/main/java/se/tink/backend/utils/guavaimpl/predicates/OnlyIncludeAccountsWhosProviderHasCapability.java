package se.tink.backend.utils.guavaimpl.predicates;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Provider;

public class OnlyIncludeAccountsWhosProviderHasCapability implements Predicate<Account> {

    private final Map<String, Credentials> validCredentialsById;

    public OnlyIncludeAccountsWhosProviderHasCapability(
            Provider.Capability capability,
            List<Credentials> credentialsById,
            ImmutableMap<String, Provider> providersByName) {

        this.validCredentialsById = FluentIterable
                .from(credentialsById)
                .filter(new OnlyIncludeCredentialsWhosProviderHasCapability(providersByName, capability))
                .uniqueIndex(Credentials::getId);
    }

    @Override
    public boolean apply(@Nullable Account account) {
        if (account == null || account.getCredentialsId() == null) {
            return false;
        }

        return validCredentialsById.containsKey(account.getCredentialsId());
    }
}
