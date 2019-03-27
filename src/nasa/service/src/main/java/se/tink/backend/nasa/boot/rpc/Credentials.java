package se.tink.backend.nasa.boot.rpc;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.Map;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class Credentials {
    private static class FieldsMap extends HashMap<String, String> {}

    private String fieldsSerialized;
    private String id;
    private String providerName;
    private CredentialsType type;
    private String userId;
    private String sensitiveDataSerialized;

    public void setUsername(String username) {
        if (Strings.isNullOrEmpty(username)) {
            return;
        }

        setField(Field.Key.USERNAME, username);
    }

    public void setPassword(String password) {
        if (Strings.isNullOrEmpty(password)) {
            return;
        }

        setField(Field.Key.PASSWORD, password);
    }

    public void setProviderName(String provider) {
        this.providerName = provider;
    }

    public void setField(Field.Key key, String value) {
        setField(key.getFieldKey(), value);
    }

    public void setField(String key, String value) {
        Map<String, String> fields = getFields();

        if (fields == null) {
            fields = Maps.newHashMap();
        }

        if (value != null) {
            fields.put(key, value);
        }

        setFields(fields);
    }

    public void setFields(Map<String, String> fields) {
        this.fieldsSerialized = SerializationUtils.serializeToString(fields);
    }

    public Map<String, String> getFields() {
        if (Strings.isNullOrEmpty(fieldsSerialized)) {
            return Maps.newHashMap();
        }

        return SerializationUtils.deserializeFromString(fieldsSerialized, FieldsMap.class);
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setType(CredentialsType type) {
        this.type = type;
    }
}
