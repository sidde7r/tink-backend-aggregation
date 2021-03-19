package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.transactionalaccount.entity.transaction;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class CreditorReferenceInformationEntity {
    private CodeAndIssuerEntity creditorReferenceInformation;

    @Override
    public String toString() {
        return String.format(
                "%s-%s-%s",
                creditorReferenceInformation.getType().getCode(),
                creditorReferenceInformation.getType().getIssuer(),
                creditorReferenceInformation.getReference());
    }
}
