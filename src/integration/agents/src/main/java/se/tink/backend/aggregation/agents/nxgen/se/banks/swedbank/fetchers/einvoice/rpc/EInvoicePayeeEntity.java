package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.einvoice.rpc;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.AbstractPayeeEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.enums.AccountIdentifierType;

@JsonObject
public class EInvoicePayeeEntity extends AbstractPayeeEntity {
    public Optional<AccountIdentifierType> getTinkType() {
        if (this.type == null) {
            return Optional.empty();
        }

        switch (this.type.toUpperCase()) {
            case SwedbankBaseConstants.PaymentAccountType.BGACCOUNT:
                return Optional.of(AccountIdentifierType.SE_BG);
            case SwedbankBaseConstants.PaymentAccountType.PGACCOUNT:
                return Optional.of(AccountIdentifierType.SE_PG);
            default:
                return Optional.empty();
        }
    }
}
