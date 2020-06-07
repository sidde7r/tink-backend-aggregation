package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata;

import com.google.common.collect.ImmutableMap;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;

public class BankdataPaymentAccountCapabilities {

    public static AccountCapabilities.Answer canMakeDomesticTransfer(String productName, AccountTypes accountType) {
        // by default checking accounts are having this capability
        if (accountType == AccountTypes.CHECKING) {
            return AccountCapabilities.Answer.YES;
        }
        return ImmutableMap.<String, AccountCapabilities.Answer>builder()
                // TODO describe
                .put("Opsparing", AccountCapabilities.Answer.YES) // TODO verify if this account can make transfer!!!!
                .build()
                .getOrDefault(productName, AccountCapabilities.Answer.UNKNOWN);
    }

    public static AccountCapabilities.Answer canReceiveDomesticTransfer(String productName, AccountTypes accountType) {
        // by default checking accounts are having this capability
        // but can be verified exactly the same way as below (for non-checking accounts)
        if (accountType == AccountTypes.CHECKING) {
            return AccountCapabilities.Answer.YES;
        }
        return ImmutableMap.<String, AccountCapabilities.Answer>builder()
                // can be verified e.g. by looking at account history and/or
                // verifying if another ambassador's account is able to make a transfer to this account (check saved
                // recipients in the first place)
                .put("Opsparing", AccountCapabilities.Answer.YES)
                .build()
                .getOrDefault(productName, AccountCapabilities.Answer.UNKNOWN);
    }

    public static AccountCapabilities.Answer canWithdrawFunds(String productName, AccountTypes accountType) {
        // by default checking accounts are having this capability
        // but can be verified exactly the same way as below (for non-checking accounts)
        if (accountType == AccountTypes.CHECKING) {
            return AccountCapabilities.Answer.YES;
        }
        return ImmutableMap.<String, AccountCapabilities.Answer>builder()
                // can be verified e.g. by looking at account history and/or
                // verifying if another ambassador's account is able to make a transfer to this account (check saved
                // recipients in the first place)
                .put("Opsparing", AccountCapabilities.Answer.YES) // TODO
                .build()
                .getOrDefault(productName, AccountCapabilities.Answer.UNKNOWN);
    }

    public static AccountCapabilities.Answer canPlaceFunds(String productName, AccountTypes accountType) {
        // by default checking accounts are having this capability
        // but can be verified exactly the same way as below (for non-checking accounts)
        if (accountType == AccountTypes.CHECKING) {
            return AccountCapabilities.Answer.YES;
        }
        return ImmutableMap.<String, AccountCapabilities.Answer>builder()
                // can be verified e.g. by looking at account history and/or
                // verifying if another ambassador's account is able to make a transfer to this account (check saved
                // recipients in the first place)
                .put("Opsparing", AccountCapabilities.Answer.YES) // TODO
                .build()
                .getOrDefault(productName, AccountCapabilities.Answer.UNKNOWN);
    }
}
