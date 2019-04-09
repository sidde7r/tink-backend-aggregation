package se.tink.backend.aggregation.agents.abnamro.ics.mappers;

import com.google.common.base.Preconditions;
import java.util.regex.Pattern;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.abnamro.client.model.creditcards.CreditCardAccountEntity;
import se.tink.backend.aggregation.agents.abnamro.utils.AbnAmroUtils;

public class AccountMapper {
    private static final Pattern VALID_BANK_ID_PATTERN = Pattern.compile("\\d{11,16}");

    public static Account toAccount(CreditCardAccountEntity input) {
        return toAccount(input, false);
    }

    public static Account toAccount(CreditCardAccountEntity input, boolean shouldCleanBankId) {
        Preconditions.checkNotNull(input.getContractNumber());
        Preconditions.checkArgument(
                VALID_BANK_ID_PATTERN.matcher(input.getContractNumber()).matches(),
                "Invalid format of contract number");

        String bankId =
                shouldCleanBankId
                        ? AbnAmroUtils.creditCardIdToAccountId(input.getContractNumber())
                        : input.getContractNumber();

        Account account = new Account();
        account.setAccountNumber(
                AbnAmroUtils.maskCreditCardContractNumber(input.getContractNumber()));
        account.setBankId(bankId);
        account.setType(AccountTypes.CREDIT_CARD);

        // Balance on the account is the sum of the "current balance" (settled amount) and the
        // "authorized balance"
        // (not settled amount). We include the "authorized balance" since we include pending
        // transactions and the
        // sum of the transactions should match the balance.
        account.setBalance(-(input.getCurrentBalance() + input.getAuthorizedBalance()));

        return account;
    }
}
