package se.tink.backend.connector.rpc.seb;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Strings;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import se.tink.backend.utils.LogUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeleteTransactionEntity {

    private static final LogUtils log = new LogUtils(DeleteTransactionEntity.class);

    @ApiModelProperty(name = "externalId", value = "Persistent identifier for the transaction.", example = "40dc04e5353547378c84f34ffc88f853", required = true)
    private String externalId;

    private Date entityCreated;

    public DeleteTransactionEntity() {
        // Timestamp of when the entity was created. Used to measure the time of the "whole processing chain" from when
        // we received a transaction in the connector until it is saved and statistics and activities are generated.
        entityCreated = new Date();
    }

    public String getExternalId() {
        return externalId;
    }

    /**
     * Check that all required fields are set and valid.
     */
    @JsonIgnore
    public boolean isValid(String externalUserId) {
        if (Strings.isNullOrEmpty(externalId)) {
            log.info("'externalId' is null or empty for user " + externalUserId);
            return false;
        }

        return true;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public Date getEntityCreated() {
        return entityCreated;
    }
}
