package se.tink.backend.aggregation.nxgen.core.transaction;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import se.tink.libraries.serialization.utils.SerializationUtils;

/**
 * A holder for storing old values of fields for which the mapping is being changed. Each mapping is
 * associated with version, however at the time of writing that message versions are not supported
 * yet required by transfer logic. The content of that holder is later translated into JSON and sent
 * in {@code payload} field under {@code FIELD_MAPPER_MIGRATIONS} key. The exemplary payload can
 * look like:
 *
 * <pre>
 *
 * "FIELD_MAPPER_MIGRATIONS": "[
 *    {"v1": {"originalDate": 1605190038000}}
 * ]"
 * </pre>
 *
 * That implementation is strictly related to:
 * <li>https://github.com/tink-ab/tink-backend/pull/26338
 * <li>https://docs.google.com/document/d/1LqbXHA6bGULQLygGLFuJ2mzC5AM-sfe829E8KtJQidA
 */
@Value
@Builder
public class FieldsMigrations {
    @Singular List<Map<String, FieldMigration>> migrations;

    public boolean isNotEmpty() {
        return !migrations.isEmpty();
    }

    public String toJSON() {
        return SerializationUtils.serializeToString(migrations);
    }
}
