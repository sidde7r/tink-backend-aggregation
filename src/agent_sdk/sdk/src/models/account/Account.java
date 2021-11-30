package se.tink.agent.sdk.models.account;

import java.util.List;
import lombok.Getter;
import se.tink.agent.sdk.storage.SerializableReference;
import se.tink.libraries.account.AccountIdentifier;

@Getter
public class Account {
    private final SerializableReference bankReference;

    private final List<AccountIdentifier> identifiers;

    // Compliance, e.g. can this account be a PAYMENT account?
    private final AccountCapabilities capabilities;

    private final List<AccountHolder> holders;

    private final List<AccountBalance> balances;

    private final List<AccountCredit> credits;

    private final AccountInterestRate interestRate;

    private final String name;

    Account(
            SerializableReference bankReference,
            List<AccountIdentifier> identifiers,
            AccountCapabilities capabilities,
            List<AccountHolder> holders,
            List<AccountBalance> balances,
            List<AccountCredit> credits,
            AccountInterestRate interestRate,
            String name) {
        this.bankReference = bankReference;
        this.identifiers = identifiers;
        this.capabilities = capabilities;
        this.holders = holders;
        this.balances = balances;
        this.credits = credits;
        this.interestRate = interestRate;
        this.name = name;
    }
}
