package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.PayPalConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.entities.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.paypal.fetcher.entities.account.EmailEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchAccountResponse {

    @JsonProperty("user_id")
    private String userId;

    private List<EmailEntity> emails;

    @JsonIgnore
    public List<AccountEntity> getAccountList() {
        EmailEntity email =
                emails.stream()
                        .filter(EmailEntity::isConfirmedAndPrimary)
                        .findFirst()
                        .orElseThrow(
                                () -> new IllegalStateException(ErrorMessages.EMAIL_NOT_FOUND));

        return Collections.singletonList(new AccountEntity(userId, email.getValue()));
    }
}
