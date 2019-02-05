package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ClientEntity {
    private DeviceEntity device;
    private SoftwareEntity software;
    private SoftwareEntity os;
}
