package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.AccountLinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.BalanceBaseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Getter
@JsonObject
public class CardEntity {
    private String resourceId;
    private String maskedPan;
    private String currency;
    private String name;
    private String product;

    private List<BalanceBaseEntity> balances;

    @JsonProperty("_links")
    private AccountLinksEntity links;

    public CardEntity() {}

    public CardEntity(
            String resourceId,
            String maskedPan,
            String currency,
            String name,
            AccountLinksEntity links,
            List<BalanceBaseEntity> balances) {
        this.resourceId = resourceId;
        this.maskedPan = maskedPan;
        this.currency = currency;
        this.name = name;
        this.balances = balances;
        this.links = links;
    }

    public ExactCurrencyAmount getInterimBooked() {
        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .filter(b -> "interimBooked".equals(b.getBalanceType()))
                .findFirst()
                .map(BalanceBaseEntity::toTinkAmount)
                .orElse(getZero())
                .abs();
    }

    public ExactCurrencyAmount getInterimAvailable() {
        return Optional.ofNullable(balances).orElse(Collections.emptyList()).stream()
                .filter(b -> "interimAvailable".equals(b.getBalanceType()))
                .findFirst()
                .map(BalanceBaseEntity::toTinkAmount)
                .orElse(getZero())
                .abs();
    }

    private ExactCurrencyAmount getZero() {
        return ExactCurrencyAmount.zero(currency);
    }

    public String getMaskedPan() {
        return maskedPan;
    }
}
