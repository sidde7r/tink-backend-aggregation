package se.tink.backend.aggregation.nxgen.core.account;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Builder
@Getter
@Setter
public class Balance {

    private BalanceType type;
    private ExactCurrencyAmount amount;
    private List<CreditLine> creditLines;

    public static se.tink.backend.agents.rpc.Balance toSystemBalance(Balance source) {
        se.tink.backend.agents.rpc.Balance dest = new se.tink.backend.agents.rpc.Balance();
        dest.setType(se.tink.backend.agents.rpc.BalanceType.valueOf(source.getType().name()));
        dest.setAmount(source.getAmount());
        dest.setCreditLines(
                source.getCreditLines() != null
                        ? source.getCreditLines().stream()
                                .map(CreditLine::toSystemCreditLine)
                                .collect(Collectors.toList())
                        : ImmutableList.of());
        return dest;
    }
}
