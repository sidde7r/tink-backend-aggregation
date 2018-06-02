package se.tink.backend.connector.rpc.seb;

import io.swagger.annotations.ApiModelProperty;
import java.util.List;

public class RollbackRequest {
    
    @ApiModelProperty(name = "checkpointIds", value = "The checkpoint IDs to be rolled back", required = true, example = "[\"1243\", \"1244\", \"1245\"]")
    private List<String> checkpointIds;

    public List<String> getCheckpointIds() {
        return checkpointIds;
    }

    public void setCheckpointIds(List<String> checkpointIds) {
        this.checkpointIds = checkpointIds;
    }
}
