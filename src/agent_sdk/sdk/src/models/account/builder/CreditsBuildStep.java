package se.tink.agent.sdk.models.account.builder;

import java.util.List;
import se.tink.agent.sdk.models.account.AccountCredit;

public interface CreditsBuildStep<T> {
    T credit(AccountCredit credit);

    T credits(List<AccountCredit> credits);
}
