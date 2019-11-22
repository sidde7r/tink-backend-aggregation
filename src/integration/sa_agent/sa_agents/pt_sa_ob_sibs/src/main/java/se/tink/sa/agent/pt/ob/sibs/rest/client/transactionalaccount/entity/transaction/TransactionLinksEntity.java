package se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import se.tink.sa.agent.pt.ob.sibs.SibsConstants;

@Getter
@Setter
public class TransactionLinksEntity {

    private String viewAccount;
    private String first;
    private String next;
    private String last;

    @JsonIgnore
    public String getNextKey() {
        return Optional.ofNullable(next)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        SibsConstants.ErrorMessages.MISSING_PAGINATION_KEY));
    }

    @JsonIgnore
    public Boolean canFetchMore() {
        return StringUtils.isNotBlank(next);
    }
}
