package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.payment.dto;

import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class BoursoramaAmountEntity {

    private BigDecimal amount;
    private String currency;

    public static BoursoramaAmountEntity of(AmountEntity amount) {
        return new BoursoramaAmountEntity(new BigDecimal(amount.getAmount()), amount.getCurrency());
    }
}
