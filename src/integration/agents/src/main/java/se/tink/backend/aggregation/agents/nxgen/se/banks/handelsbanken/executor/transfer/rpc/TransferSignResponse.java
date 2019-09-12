package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.ExecutorExceptionResolver;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.entities.ApproveSignEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.entities.ComponentEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.entities.ComponentsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.entities.StartMelittaSignEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.URL;

// BaseResponse is extended just for common Error handling.
@JsonObject
public class TransferSignResponse extends BaseResponse
        implements ExecutorExceptionResolver.Messageable {

    private ApproveSignEntity approveSign;
    private List<ComponentEntity> components;
    private String confirmStep;
    private StartMelittaSignEntity startMelittaSign;

    @Override
    public String getStatus() {
        return String.valueOf(getResponseStatus());
    }

    @JsonIgnore
    public URL getConfirmTransferLink(ExecutorExceptionResolver exceptionResolver) {
        if (StringUtils.isNotEmpty(startMelittaSign.getLink().getHref())) {
            return startMelittaSign.getLink().toURL();
        } else {
            throw exceptionResolver.asException(this);
        }
    }

    @JsonIgnore
    public ComponentsEntity getComponentWithForm() {
        return components.stream().findFirst().get().getComponentWithForm().get();
    }

    @JsonIgnore
    public URL getApprovalLink(ExecutorExceptionResolver exceptionResolver) {
        if (StringUtils.isNotEmpty(this.approveSign.getLink().getHref())) {
            return this.approveSign.getLink().toURL();
        } else {
            throw exceptionResolver.asException(this);
        }
    }

    @JsonIgnore
    public void validateResponse(ExecutorExceptionResolver exceptionResolver) {
        if (!this.getErrors().isEmpty()) {
            throw exceptionResolver.asException(this);
        }
    }
}
