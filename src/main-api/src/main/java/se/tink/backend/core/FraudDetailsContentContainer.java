package se.tink.backend.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import org.xerial.snappy.Snappy;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.backend.utils.LogUtils;

@Entity
@Table(name = "fraud_details_content")
public class FraudDetailsContentContainer {

    private static final LogUtils log = new LogUtils(FraudDetailsContentContainer.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.enableDefaultTyping();
    }

    private static final TypeReference<List<? extends FraudDetailsContent>> DETAILS_CONTENT_TYPE_REFERENCE =
            new TypeReference<List<? extends FraudDetailsContent>>() { };

    public FraudDetailsContentContainer() {
    }

    public FraudDetailsContentContainer(byte[] data) {
        this.data = data;
    }

    public FraudDetailsContentContainer(List<FraudDetailsContent> detailsContents) {
        setDetailsContents(detailsContents);
    }

    @Lob
    @Column(length = 1048576)
    private byte[] data;

    @Id
    private String userId;

    public byte[] getData() {
        return data;
    }

    public String getUserId() {
        return userId;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setDetailsContents(List<FraudDetailsContent> detailsContents) {
        try {
            String str = mapper.writerWithType(DETAILS_CONTENT_TYPE_REFERENCE).writeValueAsString(detailsContents);
            this.data = Snappy.compress(SerializationUtils.serializeToBinary(str));
        } catch (IOException e) {
            log.error("Was not able to serialize and compress data", e);
        }
    }

    public List<FraudDetailsContent> getDetailsContent() {
        if (data == null) {
            return null;
        }

        try {
            String str = SerializationUtils.deserializeFromBinary(Snappy.uncompress(data), String.class);
            return mapper.readValue(str, DETAILS_CONTENT_TYPE_REFERENCE);
        } catch (Exception e) {
            log.error("Was not able to uncompress and deserialize data", e);
            return null;
        }
    }
}
