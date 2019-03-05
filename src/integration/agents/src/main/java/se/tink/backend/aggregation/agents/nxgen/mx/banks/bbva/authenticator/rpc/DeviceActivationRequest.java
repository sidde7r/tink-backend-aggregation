package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.entity.ContactDetailEntity;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.entity.ContractEntity;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.entity.DeviceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DeviceActivationRequest {
    private boolean isSoftwareTokenActivation;
    private ContactDetailEntity contactDetail;
    private ContractEntity contract;
    private DeviceEntity device;

    @JsonIgnore private String boundary;

    public DeviceActivationRequest(
            String boundary, String phoneNumber, String deviceIdentifer, String cardNumber) {
        this.boundary = boundary;
        this.isSoftwareTokenActivation = false;
        this.contactDetail = new ContactDetailEntity(phoneNumber);
        this.device = new DeviceEntity(deviceIdentifer);
        this.contract = new ContractEntity(cardNumber);
    }
}
