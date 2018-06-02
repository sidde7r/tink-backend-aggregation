package se.tink.backend.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import java.util.List;

public class IgnoredPathPrefixFilter extends Filter<ILoggingEvent> {

    private final ImmutableDotNotationPrefixTrie ignoredPathPrefixes;

    public IgnoredPathPrefixFilter(List<String> ignoredPathPrefixes) {
        this.ignoredPathPrefixes = ImmutableDotNotationPrefixTrie.copyOf(ignoredPathPrefixes);
    }
    
    @Override
    public FilterReply decide(ILoggingEvent event) {
        String loggerName = event.getLoggerName();
        if (loggerName == null)
            // Don't know if this will ever happen. Better safe than sorry...
        {
            return FilterReply.NEUTRAL;
        }

        if (ignoredPathPrefixes.anyStartsWith(loggerName)) {
            return FilterReply.DENY;
        } else {
            return FilterReply.NEUTRAL;
        }
    }

}
