package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.transferdestination;

import org.junit.Ignore;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities.Answer;

@Ignore
public class SkandiaBankenTransferDestinationFetcherTestData {

    static Account getCheckingAccountWithExternalTransferCapability() {
        Account account = new Account();
        account.setBankId("111111");
        account.setAccountNumber("111111");
        account.setType(AccountTypes.CHECKING);
        account.setCapabilities(
                new AccountCapabilities(Answer.YES, Answer.YES, Answer.YES, Answer.YES));

        return account;
    }

    static Account getSavingsAccountWithExternalTransferCapability() {
        Account account = new Account();
        account.setBankId("222222");
        account.setAccountNumber("222222");
        account.setType(AccountTypes.SAVINGS);
        account.setCapabilities(
                new AccountCapabilities(Answer.NO, Answer.YES, Answer.YES, Answer.YES));

        return account;
    }

    static Account getInvestmentAccount() {
        Account account = new Account();
        account.setBankId("333333");
        account.setAccountNumber("333333");
        account.setType(AccountTypes.INVESTMENT);

        return account;
    }

    static Account getCheckingAccountWithNoCapabilities() {
        Account account = new Account();
        account.setBankId("444444");
        account.setAccountNumber("444444");
        account.setType(AccountTypes.CHECKING);

        return account;
    }

    static Account getCheckingAccountWithNoExternalTransferCapability() {
        Account account = new Account();
        account.setBankId("555555");
        account.setAccountNumber("555555");
        account.setType(AccountTypes.CHECKING);
        account.setCapabilities(
                new AccountCapabilities(Answer.YES, Answer.YES, Answer.NO, Answer.YES));

        return account;
    }
}
