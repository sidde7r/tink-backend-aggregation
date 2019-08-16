package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalanceEntity {
    private static final Logger log = LoggerFactory.getLogger(BalanceEntity.class);

    @JsonProperty private String balanceType;
    @JsonProperty private AmountEntity balanceAmount;
    @JsonProperty private boolean creditLimitIncluded;
    @JsonProperty private String lastChangeDateTime;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "UTC")
    private Date referenceDate;

    @JsonIgnore
    private ExactCurrencyAmount getAmount() {
        return balanceAmount.toTinkAmount();
    }

    private Date getReferenceDate() {
        return referenceDate;
    }

    private String getBalanceType() {
        return balanceType;
    }

    private static List<BalanceEntity> getBalancesOfType(
            List<BalanceEntity> balances, String balanceType) {
        return balances.stream()
                .filter(
                        balanceEntity ->
                                balanceEntity.getBalanceType().equalsIgnoreCase(balanceType))
                .collect(Collectors.toList());
    }

    public static ExactCurrencyAmount getBalanceOfType(
            List<BalanceEntity> balances, String... types) {
        List<BalanceEntity> balancesOfType = Lists.newArrayList();
        if (balances == null) {
            // No balances (sandbox)
            log.error("Account has no balances. Is this sandbox?");
            return ExactCurrencyAmount.of(1337.42, "EUR");
        } else if (balances.stream()
                .allMatch(balanceEntity -> Strings.isNullOrEmpty(balanceEntity.getBalanceType()))) {
            // Balances without type (sandbox)
            log.error("Account balances have no balanceType. Is this sandbox?");
            balancesOfType.addAll(balances);
        } else {
            // Get first matching balance type
            for (String balanceType : types) {
                balancesOfType = getBalancesOfType(balances, balanceType);
                if (!balancesOfType.isEmpty()) {
                    break;
                }
            }
        }

        switch (balancesOfType.size()) {
            case 0:
                log.warn("Account has no balances of types {}", Joiner.on(",").join(types));
                return null;
            case 1:
                return balancesOfType.get(0).getAmount();
            default:
                // If there are several balances of the same type, get the latest
                return balancesOfType.stream()
                        .max(Comparator.comparing(BalanceEntity::getReferenceDate))
                        .map(BalanceEntity::getAmount)
                        .get();
        }
    }
}
