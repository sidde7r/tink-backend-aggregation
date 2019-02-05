package se.tink.backend.aggregation.agents.utils.jersey;

import java.util.Objects;

public class EntityIdentifier {
    String url;
    Class<?> entityClass;

    public static EntityIdentifier create(String url, Class<?> entityClass) {
        EntityIdentifier key = new EntityIdentifier();
        key.url = url;
        key.entityClass = entityClass;
        return key;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EntityIdentifier)) {
            return false;
        }

        EntityIdentifier other = (EntityIdentifier) obj;
        return Objects.equals(url, other.url) && Objects.equals(entityClass, other.entityClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, entityClass);
    }
}
