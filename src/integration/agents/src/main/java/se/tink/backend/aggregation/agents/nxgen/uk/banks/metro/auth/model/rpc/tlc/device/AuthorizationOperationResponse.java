package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.tlc.device;

import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.device.ControlFlowEntity.MethodEntity;

public class AuthorizationOperationResponse extends DeviceOperationResponse {

    @Override
    public String getAssertionId() {
        return data.getControlFlow().stream()
                .findFirst()
                .map(controlFlowEntity -> controlFlowEntity.findMethod("pin"))
                .map(MethodEntity::getAssertionId)
                .orElseThrow(() -> new IllegalArgumentException("Could not find assertionId"));
    }
}
