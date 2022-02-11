package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.payment.dto;

import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BoursoramaAmountEntity {

    private final BigDecimal amount;
    private final String currency;

    public static BoursoramaAmountEntity of(AmountEntity amount) {
        return new BoursoramaAmountEntity(new BigDecimal(amount.getAmount()), amount.getCurrency());
    }
}
