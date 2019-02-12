package se.tink.libraries.api.annotations.failing;

import javax.ws.rs.Path;

public interface FailingTestInterface {
    @Path("/some/random/path")
    void someApiMethod();
}
