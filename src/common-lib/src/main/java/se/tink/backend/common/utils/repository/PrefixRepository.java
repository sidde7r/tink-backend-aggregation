package se.tink.backend.common.utils.repository;

import java.util.List;

public interface PrefixRepository<T> {
    int countByIdPrefix(String prefix);

    List<T> listByIdPrefix(String prefix);
}
