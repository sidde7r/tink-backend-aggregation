package se.tink.backend.aggregation.agents.contexts;

import se.tink.backend.aggregation.log.LogMasker;

public interface LogMaskable {

    LogMasker getLogMasker();

    void setLogMasker(LogMasker logMasker);
}
