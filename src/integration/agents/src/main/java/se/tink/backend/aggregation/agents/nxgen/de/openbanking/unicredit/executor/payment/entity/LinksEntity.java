package se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit.executor.payment.entity;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.entity.LinkEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    private LinkEntity authoriseTransaction;

    public String getScaRedirect() {
        return Optional.ofNullable(authoriseTransaction)
                .map(LinkEntity::getHref)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_SCA_URL));
    }
}
