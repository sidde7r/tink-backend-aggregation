package se.tink.backend.connector.rpc;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import se.tink.libraries.http.annotations.validation.StringNotNullOrEmpty;

public class IngestTransactionEntity {

    @StringNotNullOrEmpty
    @ApiModelProperty(value = "An id of this entity. Will only be used to return back to caller.", example = "2d3bd65493b549e1927d97a2d0683ab9", required = true)
    private String entityId;

    @StringNotNullOrEmpty
    @ApiModelProperty(value = "Persistent external identifier for the user.", example = "2d3bd65493b549e1927d97a2d0683ab9", required = true)
    private String externalUserId;

    @NotNull
    @Valid
    @ApiModelProperty(value = "The create transaction container.", required = true)
    private CreateTransactionAccountContainer container;

    public String getExternalUserId() {
        return externalUserId;
    }

    public void setExternalUserId(String externalUserId) {
        this.externalUserId = externalUserId;
    }

    public CreateTransactionAccountContainer getContainer() {
        return container;
    }

    public void setContainer(CreateTransactionAccountContainer container) {
        this.container = container;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }
}
