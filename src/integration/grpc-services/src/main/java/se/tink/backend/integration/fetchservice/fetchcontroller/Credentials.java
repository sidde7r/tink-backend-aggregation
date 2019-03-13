package se.tink.backend.integration.fetchservice.fetchcontroller;

import se.tink.backend.integration.api.models.IntegrationCredentials;

class Credentials {
    private enum Type {
        UNKNOWN, PASSWORD, THIRD_PARTY_AUTHENTICATION, KEYFOB, FRAUD;

        static Type of(IntegrationCredentials.Type publicType) {
            switch (publicType) {
            case TYPE_PASSWORD:
                return PASSWORD;
            case TYPE_THIRD_PARTY_AUTHENTICATION:
                return THIRD_PARTY_AUTHENTICATION;
            case TYPE_KEYFOB:
                return KEYFOB;
            case TYPE_FRAUD:
                return FRAUD;
            case UNRECOGNIZED:
                // Intentional fall trough
            case TYPE_UNKNOWN:
                // Intentional fall trough
            default:
                return UNKNOWN;
            }
        }
    }

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

    static Credentials of(IntegrationCredentials integrationCredentials) {
        return new Credentials(
                integrationCredentials.getId(),
                integrationCredentials.getUserId(),
                integrationCredentials.getFieldsSerialized(),
                Type.of(integrationCredentials.getType())
        );
    }


}
