package se.tink.backend.aggregation.phantomjs;

import java.util.Map;
import org.junit.Test;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

public class PhantomJsTest {

    @Test
    public void whenFailingToFindPhantomJsExecutable_raiseCannotFindExecutableException() {
        try {
            new PhantomJSDriver(
                    new Capabilities() {
                        @Override
                        public Map<String, Object> asMap() {
                            return null;
                        }

                        @Override
                        public Object getCapability(String s) {
                            return null;
                        }
                    });
        } catch (IllegalStateException e) {
            assert e.getMessage().contains("The path to the driver executable must");
        }
    }
}
