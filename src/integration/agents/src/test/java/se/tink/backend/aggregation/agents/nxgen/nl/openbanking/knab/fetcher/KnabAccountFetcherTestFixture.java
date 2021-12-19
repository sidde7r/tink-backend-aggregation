package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.fetcher;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Paths;
import java.util.List;
import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.fetcher.entity.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.fetcher.rpc.AccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.fetcher.rpc.BalancesResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceMapper;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class KnabAccountFetcherTestFixture {

    public TransactionalAccount expectedCheckingAccount() {
        return transactionalAccount(
                TransactionalAccountType.CHECKING,
                checkingAccountBalanceResponse(),
                BigDecimal.valueOf(100).setScale(2, RoundingMode.CEILING),
                "NL36KNAB0257714677",
                "Current Account",
                AccountIdentifierType.IBAN,
                "586463");
    }

    public TransactionalAccount expectedSavingsAccount() {
        return transactionalAccount(
                TransactionalAccountType.SAVINGS,
                savingsAccountBalanceResponse(),
                BigDecimal.valueOf(500).setScale(2, RoundingMode.CEILING),
                "53964486",
                "Knab Flexibel Sparen Zakelijk",
                AccountIdentifierType.BBAN,
                "586464");
    }

    public BalancesResponse checkingAccountBalanceResponse() {
        return deserializeFromFile("checking_account_balance.json", BalancesResponse.class);
    }

    public BalancesResponse savingsAccountBalanceResponse() {
        return deserializeFromFile("savings_account_balance.json", BalancesResponse.class);
    }

    public AccountsResponse accountsResponse(String fileName) {
        return deserializeFromFile(fileName, AccountsResponse.class);
    }

    private TransactionalAccount transactionalAccount(
            TransactionalAccountType accountType,
            BalancesResponse accountsBalance,
            BigDecimal balanceAmount,
            String accountNumber,
            String accountName,
            AccountIdentifierType identifierType,
            String resourceId) {
        List<BalanceEntity> balances = accountsBalance.getBalances();

        return TransactionalAccount.nxBuilder()
                .withType(accountType)
                .withPaymentAccountFlag()
                .withBalance(
                        BalanceModule.builder()
                                .withBalance(BalanceMapper.getBookedBalance(balances))
                                .setAvailableBalance(new ExactCurrencyAmount(balanceAmount, "EUR"))
                                .build())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountNumber)
                                .withAccountNumber(accountNumber)
                                .withAccountName(accountName)
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                identifierType, accountNumber, null))
                                .build())
                .addHolderName("My personal portfolio")
                .setApiIdentifier(resourceId)
                .setBankIdentifier(accountNumber)
                .build()
                .orElseThrow(RuntimeException::new);
    }

    private <T> T deserializeFromFile(String fileName, Class<T> className) {
        String resourcesPath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/nl/openbanking/knab/fetcher/resources/";

        return SerializationUtils.deserializeFromString(
                Paths.get(resourcesPath, fileName).toFile(), className);
    }
}
