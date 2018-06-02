package se.tink.backend.utils;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Provider;

public class ProviderDisplayNameFinder {

    private final Map<String, Provider> providersByName;
    private final Map<String, Credentials> credentialsById;
    private final Map<String, String> providerNameByCredentialsId;

    public ProviderDisplayNameFinder(Map<String, Provider> providersByName, List<Credentials> credentials) {
        this.providersByName = providersByName;
        this.credentialsById = Maps.uniqueIndex(credentials, new Function<Credentials, String>() {
            @Nullable
            @Override
            public String apply(Credentials c) {
                return c.getId();
            }
        });

        if (credentialsById != null) {
            this.providerNameByCredentialsId = Maps
                    .transformValues(credentialsById, new Function<Credentials, String>() {
                        @Nullable
                        @Override
                        public String apply(@Nullable Credentials credentials) {
                            return credentials != null ? credentials.getProviderName() : null;
                        }
                    });
        }
        else {
            providerNameByCredentialsId = Maps.newConcurrentMap();
        }
    }

    public Map<String, String> getIndexedByCredentialsId() {
        return providerNameByCredentialsId;
    }

    public String tryTurnDisplayNameIntoProviderName(String unknownDisplayName) {
        if (Strings.isNullOrEmpty(unknownDisplayName)) {
            return null;
        }

        String clean = unknownDisplayName.toLowerCase()
                .replace("-", "")
                .replace(" ", "")
                .replace("å", "a")
                .replace("ä", "a")
                .replace("ö", "o")
                .replaceAll("\\(.*\\)", "").trim();

        if (providersByName.containsKey(clean)) {
            return clean;
        }

        if (providersByName.containsKey(clean+"-bankid")) {
            return clean;
        }

        for (String providerName : providersByName.keySet()) {
            String displayName = providersByName.get(providerName).getCleanDisplayName();
            if (unknownDisplayName.toLowerCase().equals(displayName.toLowerCase())) {
                return providerName;
            }
        }

        return clean;
    }
}
