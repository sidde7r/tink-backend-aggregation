package se.tink.backend.connector.rpc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import io.swagger.annotations.ApiModelProperty;
import se.tink.libraries.http.annotations.validation.StringNotNullOrEmpty;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class DeleteTransactionEntity implements TransactionEntity {

    @StringNotNullOrEmpty
    @ApiModelProperty(value = "Persistent identifier for the transaction.", example = "40dc04e5353547378c84f34ffc88f853", required = true)
    private String externalId;

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }
}
