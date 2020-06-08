package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata;

import com.google.common.collect.ImmutableMap;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities.BankdataAccountEntity;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;

public class BankdataPaymentAccountCapabilities {

    public static AccountCapabilities.Answer canMakeDomesticTransfer(
            String productName,
            AccountTypes accountType,
            BankdataAccountEntity bankdataAccountEntity) {
        Boolean canMakeDomesticTransfer = bankdataAccountEntity.isTransfersFromAllowed();
        if (Boolean.TRUE.equals(canMakeDomesticTransfer)) {
            return AccountCapabilities.Answer.YES;
        } else if (Boolean.FALSE.equals(canMakeDomesticTransfer)) {
            return AccountCapabilities.Answer.NO;
        }

        // by default checking accounts are having this capability
        if (accountType == AccountTypes.CHECKING) {
            return AccountCapabilities.Answer.YES;
        }
        return ImmutableMap.<String, AccountCapabilities.Answer>builder()
                // can be verified e.g. by looking at account history and/or
                // verifying if another ambassador's account is able to make a transfer to this
                // account (check saved
                // recipients in the first place)
                .put("Opsparing", AccountCapabilities.Answer.YES)
                .build()
                .getOrDefault(productName, AccountCapabilities.Answer.UNKNOWN);
    }

    public static AccountCapabilities.Answer canReceiveDomesticTransfer(
            String productName,
            AccountTypes accountType,
            BankdataAccountEntity bankdataAccountEntity) {
        Boolean canReceiveDomesticTransfer = bankdataAccountEntity.isTransfersToAllowed();
        if (Boolean.TRUE.equals(canReceiveDomesticTransfer)) {
            return AccountCapabilities.Answer.YES;
        } else if (Boolean.FALSE.equals(canReceiveDomesticTransfer)) {
            return AccountCapabilities.Answer.NO;
        }
        // by default checking accounts are having this capability
        // but can be verified exactly the same way as below (for non-checking accounts)
        if (accountType == AccountTypes.CHECKING) {
            return AccountCapabilities.Answer.YES;
        }
        return ImmutableMap.<String, AccountCapabilities.Answer>builder()
                // can be verified e.g. by looking at account history and/or
                // verifying if another ambassador's account is able to make a transfer to this
                // account (check saved
                // recipients in the first place)
                .put("Opsparing", AccountCapabilities.Answer.YES)
                .build()
                .getOrDefault(productName, AccountCapabilities.Answer.UNKNOWN);
    }

    public static AccountCapabilities.Answer canWithdrawFunds(
            String productName, AccountTypes accountType) {
        // by default checking accounts are having this capability
        // but can be verified exactly the same way as below (for non-checking accounts)
        if (accountType == AccountTypes.CHECKING) {
            return AccountCapabilities.Answer.YES;
        }
        return ImmutableMap.<String, AccountCapabilities.Answer>builder()
                // can be verified e.g. by looking at account history
                .put(
                        "Opsparing",
                        AccountCapabilities.Answer
                                .UNKNOWN) // no history about withdrawals available
                .build()
                .getOrDefault(productName, AccountCapabilities.Answer.UNKNOWN);
    }

    public static AccountCapabilities.Answer canPlaceFunds(
            String productName,
            AccountTypes accountType,
            BankdataAccountEntity bankdataAccountEntity) {

        // our current understanding is that canPlaceFunds is fulfilled if one of the
        // following is true:
        // - canReceiveDomesticTransfer is true or
        // - you can make a physical deposit at a bank office or by depositing through a
        // depositing box/machine
        AccountCapabilities.Answer canReceiveDomesticTransfer =
                canReceiveDomesticTransfer(productName, accountType, bankdataAccountEntity);
        if (canReceiveDomesticTransfer == AccountCapabilities.Answer.YES
                || canReceiveDomesticTransfer == AccountCapabilities.Answer.NO) {
            return canReceiveDomesticTransfer;
        }
        // by default checking accounts are having this capability
        if (accountType == AccountTypes.CHECKING) {
            return AccountCapabilities.Answer.YES;
        }
        return ImmutableMap.<String, AccountCapabilities.Answer>builder()
                // can be verified e.g. by looking at account history
                .put("Opsparing", AccountCapabilities.Answer.YES)
                .build()
                .getOrDefault(productName, AccountCapabilities.Answer.UNKNOWN);
    }
}
