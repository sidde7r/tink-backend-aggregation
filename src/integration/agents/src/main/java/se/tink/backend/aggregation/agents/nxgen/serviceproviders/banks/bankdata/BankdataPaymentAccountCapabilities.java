package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities.BankdataAccountEntity;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;

public class BankdataPaymentAccountCapabilities {

    public static AccountCapabilities.Answer canExecuteExternalTransfer(
            String productName,
            AccountTypes accountType,
            BankdataAccountEntity bankdataAccountEntity) {
        Boolean canExecuteExternalTransfer = bankdataAccountEntity.getTransfersFromAllowed();
        AccountCapabilities.Answer answer =
                AccountCapabilities.Answer.From(canExecuteExternalTransfer);
        if (answer == AccountCapabilities.Answer.YES || answer == AccountCapabilities.Answer.NO) {
            return answer;
        }

        // by default checking accounts are having this capability
        if (accountType == AccountTypes.CHECKING) {
            return AccountCapabilities.Answer.YES;
        }
        // can be verified e.g. by looking at account history and/or verifying if another
        // ambassador's account is able to make a transfer to this
        // account (check saved recipients in the first place)
        return TypeMapper.<AccountCapabilities.Answer>builder()
                .put(AccountCapabilities.Answer.YES, "Opsparing")
                .build()
                .translate(productName)
                .orElse(AccountCapabilities.Answer.UNKNOWN);
    }

    public static AccountCapabilities.Answer canReceiveExternalTransfer(
            String productName,
            AccountTypes accountType,
            BankdataAccountEntity bankdataAccountEntity) {
        Boolean canReceiveDomesticTransfer = bankdataAccountEntity.getTransfersToAllowed();
        AccountCapabilities.Answer answer =
                AccountCapabilities.Answer.From(canReceiveDomesticTransfer);
        if (answer == AccountCapabilities.Answer.YES || answer == AccountCapabilities.Answer.NO) {
            return answer;
        }
        // by default checking accounts are having this capability
        // but can be verified exactly the same way as below (for non-checking accounts)
        if (accountType == AccountTypes.CHECKING) {
            return AccountCapabilities.Answer.YES;
        }
        // can be verified e.g. by looking at account history and/or verifying if another
        // ambassador's account is able to make a transfer to this
        // account (check saved recipients in the first place)
        return TypeMapper.<AccountCapabilities.Answer>builder()
                .put(AccountCapabilities.Answer.YES, "Opsparing")
                .build()
                .translate(productName)
                .orElse(AccountCapabilities.Answer.UNKNOWN);
    }

    public static AccountCapabilities.Answer canWithdrawCash(
            String productName, AccountTypes accountType) {
        // by default checking accounts are having this capability
        // but can be verified exactly the same way as below (for non-checking accounts)
        if (accountType == AccountTypes.CHECKING) {
            return AccountCapabilities.Answer.YES;
        }
        return TypeMapper.<AccountCapabilities.Answer>builder()
                .put(
                        AccountCapabilities.Answer.UNKNOWN,
                        "Opsparing") // no history about withdrawals available
                .build()
                .translate(productName)
                .orElse(AccountCapabilities.Answer.UNKNOWN);
    }

    public static AccountCapabilities.Answer canPlaceFunds(
            String productName,
            AccountTypes accountType,
            BankdataAccountEntity bankdataAccountEntity) {

        // our current understanding is that canPlaceFunds is fulfilled if one of the following is
        // true:
        // - canReceiveExternalTransfer is true or
        // - you can make a physical deposit at a bank office or by depositing through a depositing
        // box/machine
        AccountCapabilities.Answer canReceiveExternalTransfer =
                canReceiveExternalTransfer(productName, accountType, bankdataAccountEntity);
        if (canReceiveExternalTransfer == AccountCapabilities.Answer.YES
                || canReceiveExternalTransfer == AccountCapabilities.Answer.NO) {
            return canReceiveExternalTransfer;
        }
        // by default checking accounts are having this capability
        if (accountType == AccountTypes.CHECKING) {
            return AccountCapabilities.Answer.YES;
        }
        // can be verified e.g. by looking at account history
        return TypeMapper.<AccountCapabilities.Answer>builder()
                .put(AccountCapabilities.Answer.UNKNOWN, "Opsparing")
                .build()
                .translate(productName)
                .orElse(AccountCapabilities.Answer.YES);
    }
}
