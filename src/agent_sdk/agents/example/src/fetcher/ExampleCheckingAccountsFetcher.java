package se.tink.agent.agents.example.fetcher;

import java.util.List;
import se.tink.agent.sdk.fetching.accounts.CheckingAccountsFetcher;
import se.tink.agent.sdk.models.account.CheckingAccount;
import se.tink.agent.sdk.models.account.CreditCardAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;

public class ExampleCheckingAccountsFetcher implements CheckingAccountsFetcher {

    @Override
    public List<CheckingAccount> fetchAccounts() {

        CheckingAccount.builder()
                .bankReference("test")
                .identifier(
                        AccountIdentifier.create(
                                AccountIdentifierType.IBAN, "SE12312421421", "Sparkonto"))
                .name(null)
                .unknownCapabilities()
                .noHolderInformation()
                .balance(null)
                .noCredit()
                .noInterestRate()
                .build();

        CheckingAccount testAcc =
                CheckingAccount.builder()
                        .bankReference("someIdentifier")
                        .identifier(null)
                        .name("hej")
                        .unknownCapabilities()
                        .noHolderInformation()
                        .balance(null)
                        .noCredit()
                        .noInterestRate()
                        .build();

        CreditCardAccount account =
                CreditCardAccount.builder()
                        .bankReference("test")
                        .cardNumber("HELLU")
                        .name("VISA")
                        .unknownCapabilities()
                        .holder(null)
                        .balance(null)
                        .credit(null)
                        .noInterestRate()
                        .build();

        return null;
    }
}
