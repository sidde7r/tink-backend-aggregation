package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.entity.ContactDetailEntity;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.entity.ContractValidateEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ValidateSubscriptionRequest {
    private ContactDetailEntity contactDetail;
    private ContractValidateEntity contractValidate;

    public ValidateSubscriptionRequest(String phoneNumber, String cardnumber) {
        this.contactDetail = new ContactDetailEntity(phoneNumber);
        this.contractValidate = new ContractValidateEntity(cardnumber);
    }
}
