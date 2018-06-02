package se.tink.backend.consent.core;

import org.junit.Test;
import se.tink.backend.consent.core.exceptions.ConsentRequestInvalid;
import se.tink.backend.consent.rpc.ConsentRequest;

public class ConsentRequestTest {
    @Test
    public void validateCorrectRequest() throws ConsentRequestInvalid {
        ConsentRequest request = new ConsentRequest();
        request.setKey("key");
        request.setAction(Action.ACCEPTED);
        request.setVersion("1.0.0");
        request.setChecksum("checksum");

        request.validate();
    }

    @Test(expected = ConsentRequestInvalid.class)
    public void validateMissingKey() throws ConsentRequestInvalid {
        ConsentRequest request = new ConsentRequest();
        request.setAction(Action.ACCEPTED);
        request.setVersion("1.0.0");
        request.setChecksum("checksum");

        request.validate();
    }

    @Test(expected = ConsentRequestInvalid.class)
    public void validateMissingAction() throws ConsentRequestInvalid {
        ConsentRequest request = new ConsentRequest();
        request.setKey("key");
        request.setVersion("1.0.0");
        request.setChecksum("checksum");

        request.validate();
    }

    @Test(expected = ConsentRequestInvalid.class)
    public void validateMissingVersion() throws ConsentRequestInvalid {
        ConsentRequest request = new ConsentRequest();
        request.setKey("key");
        request.setAction(Action.ACCEPTED);
        request.setChecksum("checksum");

        request.validate();
    }

    @Test(expected = ConsentRequestInvalid.class)
    public void validateMissingChecksum() throws ConsentRequestInvalid {
        ConsentRequest request = new ConsentRequest();
        request.setKey("key");
        request.setAction(Action.ACCEPTED);
        request.setVersion("1.0.0");

        request.validate();
    }
}
