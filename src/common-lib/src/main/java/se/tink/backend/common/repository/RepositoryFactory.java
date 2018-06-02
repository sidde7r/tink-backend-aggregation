package se.tink.backend.common.repository;

public interface RepositoryFactory {
    <R> R getRepository(Class<R> cls);
    <T> T getDao(Class<T> key);
}
