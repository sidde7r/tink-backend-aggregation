package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.tlc.device;

import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.device.ControlFlowEntity.MethodEntity.ChannelEntity;

public class RegisterDeviceOperationResponse extends DeviceOperationResponse {

    @Override
    public String getAssertionId() {
        return data.getControlFlow().stream()
                .findFirst()
                .flatMap(controlFlowEntity -> controlFlowEntity.getMethods().stream().findFirst())
                .flatMap(methodEntity -> methodEntity.getChannels().stream().findFirst())
                .map(ChannelEntity::getAssertionId)
                .orElseThrow(() -> new IllegalArgumentException("Could not find assertionId"));
    }
}
