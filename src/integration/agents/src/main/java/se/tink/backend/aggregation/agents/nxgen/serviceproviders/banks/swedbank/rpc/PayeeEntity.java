package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBaseConstants;
import se.tink.libraries.account.AccountIdentifier;

public class PayeeEntity extends AbstractPayeeEntity implements GeneralAccountEntity {
    @JsonIgnore private static final Logger log = LoggerFactory.getLogger(PayeeEntity.class);

    private String referenceType;
    private LinksEntity links;
    private String lastUsed;

    public String getReferenceType() {
        return referenceType;
    }

    public LinksEntity getLinks() {
        return links;
    }

    public String getLastUsed() {
        return lastUsed;
    }

    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        Optional<AccountIdentifier.Type> tinkType = getTinkType();
        if (!tinkType.isPresent()) {
            return AccountIdentifier.create(null);
        }

        return AccountIdentifier.create(tinkType.get(), this.accountNumber, this.name);
    }

    @Override
    public String generalGetBank() {
        return null;
    }

    @Override
    public String generalGetName() {
        return this.name;
    }

    private Optional<AccountIdentifier.Type> getTinkType() {
        if (this.type == null) {
            return Optional.empty();
        }

        switch (this.type.toUpperCase()) {
            case SwedbankBaseConstants.PaymentAccountType.BGACCOUNT:
                return Optional.of(AccountIdentifier.Type.SE_BG);
            case SwedbankBaseConstants.PaymentAccountType.PGACCOUNT:
                return Optional.of(AccountIdentifier.Type.SE_PG);
            default:
                log.warn("Unknown payee entity type: {}", this.type);
                return Optional.empty();
        }
    }
}
