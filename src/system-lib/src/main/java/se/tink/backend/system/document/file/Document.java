package se.tink.backend.system.document.file;

public interface Document {

    byte[] generateInMemoryDocument();

    String getDocumentName();

    String getDocumentNameWithExtension();

}
