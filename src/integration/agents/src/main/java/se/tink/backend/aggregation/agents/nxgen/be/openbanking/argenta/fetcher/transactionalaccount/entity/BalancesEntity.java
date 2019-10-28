package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaConstants.BalanceTypes;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.ArgentaConstants.Formats;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalancesEntity {

    private String balanceType;
    private AmountEntity balanceAmount;

    @JsonFormat(pattern = Formats.RESPONSE_DATE_FORMAT)
    private Date referenceDate;

    public boolean isInterimAvailable() {
        return BalanceTypes.INTERIM_AVAILABLE.equalsIgnoreCase(balanceType);
    }

    public ExactCurrencyAmount getBalanceAmount() {
        return Optional.ofNullable(balanceAmount)
                .orElseGet(() -> new AmountEntity("0", Formats.CURRENCY))
                .toTinkAmount();
    }
}
