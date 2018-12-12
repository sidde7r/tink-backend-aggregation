package se.tink.backend.aggregation.agents.nxgen.at.banks.ing.authenticator.entities;

import org.codehaus.jackson.annotate.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@JsonObject
public class AccountReferenceEntity {
    private String id;
    private String type;
    private String url;

    private AccountReferenceEntity() {}

    public AccountReferenceEntity(String id, String type, String uri) {
        this.id = id;
        this.type = type;
        this.url = uri;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    @JsonIgnore
    public String getAccountIndex() {
        final Pattern pattern = Pattern.compile("kontoTableRepeater-(\\d+)-kontoDetailLink");
        final Matcher matcher = pattern.matcher(getUrl());
        if (!matcher.find()) {
            throw new IllegalStateException();
        }
        return matcher.group(1);
    }
}
