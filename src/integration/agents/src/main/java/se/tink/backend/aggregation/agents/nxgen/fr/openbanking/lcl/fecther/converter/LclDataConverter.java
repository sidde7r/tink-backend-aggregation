package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.fecther.converter;

import java.math.BigDecimal;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.common.AmountDto;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class LclDataConverter {

    public static ExactCurrencyAmount convertAmountDtoToExactCurrencyAmount(AmountDto amountDto) {
        return new ExactCurrencyAmount(
                new BigDecimal(amountDto.getAmount()), amountDto.getCurrency());
    }
}
