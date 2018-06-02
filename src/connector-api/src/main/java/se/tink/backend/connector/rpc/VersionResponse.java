package se.tink.backend.connector.rpc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class VersionResponse {

    @ApiModelProperty(value = "The version of the build", example = "4513", required = true)
    private String version;

    @ApiModelProperty(value = "The last commit of the build", example = "e764d0eed748d6c137c30fc94c7e17544d101ff3", required = true)
    private String commit;

    @ApiModelProperty(value = "The date of the build", example = "1455740874875", required = true)
    private Date date;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCommit() {
        return commit;
    }

    public Date getDate() {
        return date;
    }
}
