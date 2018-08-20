package se.tink.backend.queue;

public interface QueuableJob {
    AutomaticRefreshStatus getStatus();
    void setStatus(AutomaticRefreshStatus status);
}
