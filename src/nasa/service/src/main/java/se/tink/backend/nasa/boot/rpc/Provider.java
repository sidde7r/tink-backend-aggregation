package se.tink.backend.nasa.boot.rpc;

import java.util.List;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class Provider {
    private String className;
    private String name;
    private CredentialsType credentialsType;
    private String currency;
    private String displayName;
    private String fields;
    private String market;
    //providerStatuses
    //type

    public Provider() {}

    public void setClassName(String className) {
        this.className = className;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCredentialsType(CredentialsType credentialsType) {
        this.credentialsType = credentialsType;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setFields(List<Field> fields) {
        this.fields = SerializationUtils.serializeToString(fields);
    }

    public void setMarket(String market) {
        this.market = market;
    }
}
