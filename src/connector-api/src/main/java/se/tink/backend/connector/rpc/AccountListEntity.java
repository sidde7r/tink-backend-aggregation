package se.tink.backend.connector.rpc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import javax.validation.Valid;
import se.tink.libraries.http.annotations.validation.ListNotNullOrEmpty;
import se.tink.libraries.http.annotations.validation.NoNullElements;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class AccountListEntity {

    @ListNotNullOrEmpty
    @NoNullElements
    @Valid
    @ApiModelProperty(value = "The accounts.", required = true)
    private List<AccountEntity> accounts;

    public List<AccountEntity> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<AccountEntity> accounts) {
        this.accounts = accounts;
    }
}
