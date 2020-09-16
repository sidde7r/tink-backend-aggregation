package se.tink.backend.aggregation.agents.abnamro.ics.mappers;

import com.google.common.base.Preconditions;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.abnamro.client.model.creditcards.CreditCardAccountEntity;
import se.tink.backend.aggregation.agents.abnamro.utils.AbnAmroUtils;

public class AccountMapper {

    private static final Logger log = LoggerFactory.getLogger(AccountMapper.class);

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

        // Temporary log to investigate Zero balance - Only log when balance is Zero
        String creditLeftAmount = Double.toString(input.getCreditLeftToUse());
        String creditLimitAmount = Double.toString(input.getCreditLimit());
        String authorizedBalance = Double.toString(input.getAuthorizedBalance());
        log.info(
                String.format(
                        "Credit left: %s, Credit limit: %s, Authorized balance: %s",
                        creditLeftAmount, creditLimitAmount, authorizedBalance));

        account.setBalance(input.getCreditLeftToUse() - input.getCreditLimit());

        return account;
    }
}
