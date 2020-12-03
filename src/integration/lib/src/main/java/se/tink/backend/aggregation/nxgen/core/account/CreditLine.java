package se.tink.backend.aggregation.nxgen.core.account;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Builder
@Getter
@Setter
public class CreditLine {

    private CreditLineType type;
    private boolean includedInBalance;
    private ExactCurrencyAmount amount;

    public static se.tink.backend.agents.rpc.CreditLine toSystemCreditLine(CreditLine source) {
        se.tink.backend.agents.rpc.CreditLine dest = new se.tink.backend.agents.rpc.CreditLine();
        dest.setAmount(source.getAmount());
        dest.setIncludedInBalance(source.isIncludedInBalance());
        dest.setType(se.tink.backend.agents.rpc.CreditLineType.valueOf(source.getType().name()));
        return dest;
    }
}
