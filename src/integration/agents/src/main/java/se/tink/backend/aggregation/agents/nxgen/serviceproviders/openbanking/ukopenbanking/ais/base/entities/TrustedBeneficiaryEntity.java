package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TrustedBeneficiaryEntity implements GeneralAccountEntity {
    private String accountId;
    private String beneficiaryId;
    private TrustedBeneficiaryDetailsEntity creditorAccount;

    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        return UkOpenBankingV31Constants.toAccountIdentifier(
                creditorAccount.getSchemeName(), creditorAccount.getIdentification());
    }

    @Override
    public String generalGetBank() {
        return "";
    }

    @Override
    public String generalGetName() {
        return creditorAccount.getName();
    }
}
