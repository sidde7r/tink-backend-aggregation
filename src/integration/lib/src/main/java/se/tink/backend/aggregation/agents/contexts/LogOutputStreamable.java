package se.tink.backend.aggregation.agents.contexts;

import java.io.ByteArrayOutputStream;

public interface LogOutputStreamable {

    ByteArrayOutputStream getLogOutputStream();
}
