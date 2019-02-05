package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.einvoice.rpc;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.AbstractPayeeEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;

@JsonObject
public class EInvoicePayeeEntity extends AbstractPayeeEntity {
    public Optional<AccountIdentifier.Type> getTinkType() {
        if (this.type == null) {
            return Optional.empty();
        }

        switch (this.type.toUpperCase()) {
        case SwedbankBaseConstants.PaymentAccountType.BGACCOUNT:
            return Optional.of(AccountIdentifier.Type.SE_BG);
        case SwedbankBaseConstants.PaymentAccountType.PGACCOUNT:
            return Optional.of(AccountIdentifier.Type.SE_PG);
        default:
            return Optional.empty();
        }
    }
}
