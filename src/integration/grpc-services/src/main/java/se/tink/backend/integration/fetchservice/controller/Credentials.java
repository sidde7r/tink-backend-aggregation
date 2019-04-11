package se.tink.backend.integration.fetchservice.controller;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Objects;

public class Credentials {

    private final String id;
    private final String userId;
    private final String fieldsSerialized;
    private final Type type;

    private Credentials(String id, String userId, String fieldsSerialized, Type type) {
        this.id = id;
        this.userId = userId;
        this.fieldsSerialized = fieldsSerialized;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getFieldsSerialized() {
        return fieldsSerialized;
    }

    public Type getType() {
        return type;
    }

    public static Credentials of(String id, String userId, String fieldsSerialized, Type type) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(id));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(userId));
        Preconditions.checkNotNull(fieldsSerialized);
        Preconditions.checkArgument(!Objects.isNull(type));
        return new Credentials(id, userId, fieldsSerialized, type);
    }

    public enum Type {
        UNKNOWN,
        PASSWORD,
        THIRD_PARTY_AUTHENTICATION,
        KEYFOB,
        FRAUD;
    }
}
