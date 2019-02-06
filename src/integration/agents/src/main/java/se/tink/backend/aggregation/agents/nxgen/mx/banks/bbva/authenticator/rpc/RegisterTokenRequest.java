package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.entity.ContactDetailEntity;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.entity.RegisterDeviceEntity;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.entity.RegisterTypeEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegisterTokenRequest {
    private ContactDetailEntity contactDetail;
    private String customerId;
    private String id;
    private RegisterTypeEntity registerType;
    private RegisterDeviceEntity device;

    public RegisterTokenRequest(String phoneNumber, String deviceIdentifier, String tokenId) {
        this.contactDetail = new ContactDetailEntity(phoneNumber);
        this.customerId = "";
        this.id = tokenId;
        this.registerType = new RegisterTypeEntity();
        this.device = new RegisterDeviceEntity(deviceIdentifier);
    }
}
