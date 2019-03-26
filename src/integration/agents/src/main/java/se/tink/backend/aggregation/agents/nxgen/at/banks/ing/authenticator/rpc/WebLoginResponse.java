package se.tink.backend.aggregation.agents.nxgen.at.banks.ing.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.authenticator.entities.AccountReferenceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class WebLoginResponse {
    private String username;
    private String accountHolder;
    private String basePage;
    private List<AccountReferenceEntity> accountReferenceEntities;

    private WebLoginResponse() {}

    public WebLoginResponse(
            final String username,
            final String accountHolder,
            final String basePage,
            final List<AccountReferenceEntity> accountReferenceEntities) {
        this.username = username;
        this.accountHolder = accountHolder;
        this.basePage = basePage;
        this.accountReferenceEntities = accountReferenceEntities;
    }

    public String getUsername() {
        return username;
    }

    public String getAccountHolder() {
        return accountHolder;
    }

    public String getBasePage() {
        return basePage;
    }

    public List<AccountReferenceEntity> getAccountReferenceEntities() {
        return accountReferenceEntities;
    }

    public AccountReferenceEntity getAccountReferenceEntity(String id) {
        for (AccountReferenceEntity r : accountReferenceEntities) {
            if (r.getId().equals(id)) {
                return r;
            }
        }
        throw new IllegalStateException("Failed to get the account with id=\"" + id + "\"");
    }

    @JsonIgnore
    public int getPageNumber() {
        final Pattern pattern = Pattern.compile("page\\?(\\d+)");
        final Matcher matcher = pattern.matcher(getBasePage());
        if (!matcher.find()) {
            throw new IllegalStateException();
        }
        return Integer.parseInt(matcher.group(1));
    }
}
