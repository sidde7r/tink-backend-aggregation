package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.entities.transaction;

import com.google.common.base.Strings;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionLinksEntity {

    private String next;

    public String getNextKey() {
        return Optional.ofNullable(next)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        SibsConstants.ErrorMessages.MISSING_PAGINATION_KEY));
    }

    public Boolean canFetchMore() {
        return !Strings.isNullOrEmpty(next);
    }
}
