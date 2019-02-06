package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.entity.ContactDetailEntity;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.entity.ContractEntity;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.entity.DeviceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DigitalActivationRequest {
    private ContactDetailEntity contactDetail;
    private boolean isSoftwareTokenActivation;
    private ContractEntity contract;
    private boolean isDefaultCarrier;
    private DeviceEntity device;

    public DigitalActivationRequest(String phoneNumber, String cardNumber, String deviceId) {
        this.isDefaultCarrier = true;
        this.isSoftwareTokenActivation = true;
        this.contactDetail = new ContactDetailEntity(phoneNumber);
        this.contract = new ContractEntity(cardNumber);
        this.device = new DeviceEntity(deviceId);
    }
}
