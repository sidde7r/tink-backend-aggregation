package se.tink.backend.aggregation.agents.contexts;

import java.io.OutputStream;

public interface LogOutputStreamable {

    OutputStream getLogOutputStream();
}
