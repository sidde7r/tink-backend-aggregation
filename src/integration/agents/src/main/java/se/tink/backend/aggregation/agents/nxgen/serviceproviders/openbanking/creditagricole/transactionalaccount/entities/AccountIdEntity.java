package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class AccountIdEntity {
    private String iban;
    private IdDetailsEntity other;
    private AreaEntity area;

    public static AccountIdEntity createForConsentRequest(String iban) {
        return new AccountIdEntity(iban, null, null);
    }
}
