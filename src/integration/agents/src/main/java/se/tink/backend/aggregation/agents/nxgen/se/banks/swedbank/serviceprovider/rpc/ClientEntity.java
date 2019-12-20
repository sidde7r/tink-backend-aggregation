package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ClientEntity {
    private DeviceEntity device;
    private SoftwareEntity software;
    private SoftwareEntity os;
}
