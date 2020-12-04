package se.tink.libraries.http.client.masker;

import agents_platform_framework.com.jayway.jsonpath.DocumentContext;
import agents_platform_framework.com.jayway.jsonpath.JsonPath;
import agents_platform_framework.com.jayway.jsonpath.PathNotFoundException;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;

public class SensitiveDataMasker {

    private static final String MASK = "**HASHED:%s**";

    private static final Set<String> X_PATHS_FOR_SENSITIVE_FIELDS =
            ImmutableSet.of(
                    "user.username",
                    "user.nationalId",
                    "originatingUserIp",
                    "accounts..accountNumber",
                    "accounts..holderName",
                    "accounts..availableCredit",
                    "accounts..balance");

    public static String mask(String json) {
        if (json == null) {
            return null;
        }
        DocumentContext parsed = JsonPath.parse(json);
        X_PATHS_FOR_SENSITIVE_FIELDS.forEach(
                xpath -> {
                    try {
                        parsed.map(xpath, (o, configuration) -> String.format(MASK, hash(o)));
                    } catch (PathNotFoundException ignored) {
                        // ignored
                    }
                });
        return parsed.jsonString();
    }

    private static String hash(Object p) {
        String hash = Hash.sha256Base64(p.toString().getBytes());
        return hash.substring(0, 2);
    }
}
