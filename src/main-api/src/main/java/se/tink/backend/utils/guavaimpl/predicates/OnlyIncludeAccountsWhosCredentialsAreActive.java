package se.tink.backend.utils.guavaimpl.predicates;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsStatus;

public class OnlyIncludeAccountsWhosCredentialsAreActive implements Predicate<Account> {

    private Map<String, Credentials> credentialsById;

    public OnlyIncludeAccountsWhosCredentialsAreActive(List<Credentials> userCredentials) {
        credentialsById = FluentIterable.from(userCredentials)
                .uniqueIndex(Credentials::getId);
    }

    @Override
    public boolean apply(@Nullable Account account) {
        if (account == null || account.getCredentialsId() == null) {
            return false;
        }

        Credentials credentials = credentialsById.get(account.getCredentialsId());
        if (credentials == null) {
            return false;
        }

        return !Objects.equal(credentials.getStatus(), CredentialsStatus.DISABLED);
    }
}
