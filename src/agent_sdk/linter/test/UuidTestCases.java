package src.agent_sdk.linter.test;

import java.util.UUID;

public class UuidTestCases {
    public void preventUuid() {
        // BUG: Diagnostic contains: Disallowed usage of method or class.
        new UUID(42, 42);

        // BUG: Diagnostic contains: Disallowed usage of method or class.
        UUID.randomUUID();

        // BUG: Diagnostic contains: Disallowed usage of method or class.
        UUID.nameUUIDFromBytes("foobar".getBytes());
    }
}
