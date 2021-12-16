package se.tink.agent.runtime.operation;

import com.google.common.base.Strings;
import java.util.Map;
import java.util.Optional;
import se.tink.agent.sdk.operation.StaticBankCredentials;

public class StaticBankCredentialsImpl implements StaticBankCredentials {
    private final Map<String, String> fields;

    public StaticBankCredentialsImpl(Map<String, String> fields) {
        this.fields = fields;
    }

    @Override
    public Optional<String> tryGet(String key) {
        if (this.fields == null) {
            return Optional.empty();
        }

        String field = this.fields.get(key);

        if (Strings.isNullOrEmpty(field)) {
            return Optional.empty();
        }

        return Optional.of(field);
    }
}
