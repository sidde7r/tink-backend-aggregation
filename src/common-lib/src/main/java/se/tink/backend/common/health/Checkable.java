package se.tink.backend.common.health;

public interface Checkable {
    void check() throws Exception;
}
