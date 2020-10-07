package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.fetcher.transactionalaccount;

import java.io.File;
import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.fetcher.transactionalaccount.rpc.TransactionResponse;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class BankDataTransactionalAccountFixtures {

    private static final String TRANSACTION_RESPONSE_WITH_NEXT_KEY_FILE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/bankdata/resources/transactionResponseWithNextKey.json";
    private static final String TRANSACTION_RESPONSE_FILE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/bankdata/resources/transactionResponseWithoutNextKey.json";

    static TransactionalAccount transactionalAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withoutFlags()
                .withBalance(
                        BalanceModule.builder()
                                .withBalance(ExactCurrencyAmount.inEUR(1))
                                .setAvailableCredit(ExactCurrencyAmount.inEUR(1))
                                .setAvailableBalance(ExactCurrencyAmount.of(1.0, "EUR"))
                                .setCreditLimit(ExactCurrencyAmount.of(1.0, "EUR"))
                                .setInterestRate(0.1)
                                .build())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier("123")
                                .withAccountNumber("123")
                                .withAccountName("name")
                                .addIdentifier(new IbanIdentifier("DK123123123"))
                                .build())
                .build()
                .orElse(null);
    }

    static TransactionResponse transactionResponseWithNextKey() {
        return SerializationUtils.deserializeFromString(
                new File(TRANSACTION_RESPONSE_WITH_NEXT_KEY_FILE_PATH), TransactionResponse.class);
    }

    static TransactionResponse transactionResponseWithoutNextKey() {
        return SerializationUtils.deserializeFromString(
                new File(TRANSACTION_RESPONSE_FILE_PATH), TransactionResponse.class);
    }
}
