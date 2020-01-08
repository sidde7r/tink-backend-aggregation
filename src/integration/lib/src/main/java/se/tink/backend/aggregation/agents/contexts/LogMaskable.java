package se.tink.backend.aggregation.agents.contexts;

import se.tink.backend.aggregation.logmasker.LogMasker;

public interface LogMaskable {

    LogMasker getLogMasker();

    void setLogMasker(LogMasker logMasker);
}
