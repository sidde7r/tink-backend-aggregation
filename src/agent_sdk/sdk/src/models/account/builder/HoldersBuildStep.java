package se.tink.agent.sdk.models.account.builder;

import java.util.List;
import se.tink.agent.sdk.models.account.AccountHolder;

public interface HoldersBuildStep<T> {
    T holder(AccountHolder holder);

    T holders(List<AccountHolder> holders);
}
