package se.tink.backend.aggregation.queue;

import java.io.IOException;
import java.util.Base64;
import org.xerial.snappy.Snappy;
import se.tink.backend.aggregation.queue.models.RefreshInformation;
import se.tink.libraries.queue.sqs.EncodingHandler;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AutomaticRefreshQueueEncoder implements EncodingHandler<RefreshInformation> {
    @Override
    public String encode(RefreshInformation refreshInformation) throws IOException {
        return Base64.getEncoder()
                .encodeToString(
                        Snappy.compress(SerializationUtils.serializeToBinary(refreshInformation)));
    }

    @Override
    public RefreshInformation decode(String message) throws IOException {
        return SerializationUtils.deserializeFromBinary(
                Snappy.uncompress(Base64.getDecoder().decode(message)), RefreshInformation.class);
    }
}
