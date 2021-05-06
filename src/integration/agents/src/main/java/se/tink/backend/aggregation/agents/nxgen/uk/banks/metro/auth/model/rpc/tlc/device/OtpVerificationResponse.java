package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.tlc.device;

import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.device.ControlFlowEntity.AssertionEntity;

public class OtpVerificationResponse extends DeviceOperationResponse {

    @Override
    public String getAssertionId() {
        return data.getControlFlow().stream()
                .findFirst()
                .flatMap(
                        controlFlowEntity -> controlFlowEntity.getAssertions().stream().findFirst())
                .map(AssertionEntity::getAssertionId)
                .orElseThrow(() -> new IllegalArgumentException("Could not find assertionId"));
    }
}
