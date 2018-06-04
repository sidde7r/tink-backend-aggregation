package se.tink.backend.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;
import org.xerial.snappy.Snappy;
import se.tink.libraries.uuid.UUIDUtils;

public class CompressedDocument {

    private UUID userId;
    private UUID token;
    private String identifier;
    private ByteBuffer compressedDocument;
    private String mimeType;

    public CompressedDocument() {

    }

    public CompressedDocument(String userId, DocumentContainer document) throws IOException {
        this.userId = UUIDUtils.fromTinkUUID(userId);
        this.token = UUID.randomUUID();
        this.identifier = document.getIdentifier();
        this.mimeType = document.getMimeType();
        this.compressedDocument = ByteBuffer.wrap(Snappy.compress(document.getBinaryDocument()));
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getToken() {
        return token;
    }

    public void setToken(UUID token) {
        this.token = token;
    }

    public ByteBuffer getCompressedDocument() {
        return compressedDocument;
    }

    public void setCompressedDocument(ByteBuffer compressedDocument) {
        this.compressedDocument = compressedDocument;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public byte[] getUncompressed() throws IOException {
        byte[] bytes = new byte[compressedDocument.remaining()];
        compressedDocument.get(bytes);
        return Snappy.uncompress(bytes);
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
