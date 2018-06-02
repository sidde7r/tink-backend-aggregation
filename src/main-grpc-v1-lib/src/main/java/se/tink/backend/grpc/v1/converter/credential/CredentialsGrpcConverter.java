package se.tink.backend.grpc.v1.converter.credential;

import java.util.List;
import se.tink.grpc.v1.models.Credential;
import se.tink.grpc.v1.models.Credentials;

public class CredentialsGrpcConverter {

    private CoreCredentialToGrpcCredentialConverter coreCredentialToGrpcCredentialConverter;

    public CredentialsGrpcConverter(
            CoreCredentialToGrpcCredentialConverter coreCredentialToGrpcCredentialConverter) {
        this.coreCredentialToGrpcCredentialConverter = coreCredentialToGrpcCredentialConverter;
    }

    public Credentials convertFrom(List<se.tink.backend.core.Credentials> input) {
        List<Credential> credentials = coreCredentialToGrpcCredentialConverter.convertFrom(input);
        return Credentials.newBuilder().addAllCredential(credentials).build();
    }
}
