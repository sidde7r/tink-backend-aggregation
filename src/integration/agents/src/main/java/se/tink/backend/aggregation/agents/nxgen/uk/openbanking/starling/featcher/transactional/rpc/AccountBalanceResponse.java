package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.rpc;

import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.entity.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountBalanceResponse {

    private AmountEntity clearedBalance;
    private AmountEntity effectiveBalance;
    private AmountEntity pendingTransactions;
    private AmountEntity availableToSpend;
    private AmountEntity acceptedOverdraft;
    private AmountEntity amount;

    public AmountEntity getClearedBalance() {
        return clearedBalance;
    }

    public AmountEntity getEffectiveBalance() {
        return effectiveBalance;
    }

    public AmountEntity getPendingTransactions() {
        return pendingTransactions;
    }

    public AmountEntity getAvailableToSpend() {
        return availableToSpend;
    }

    public AmountEntity getAcceptedOverdraft() {
        return acceptedOverdraft;
    }

    public AmountEntity getAmount() {
        return amount;
    }
}
