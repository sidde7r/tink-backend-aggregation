package se.tink.backend.grpc.v1.converter.user;

import java.util.Map;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.rpc.DeleteUserCommand;
import se.tink.grpc.v1.rpc.DeleteUserRequest;

public class DeleteUserRequestConverter implements Converter<DeleteUserRequest, DeleteUserCommand> {
    private final Map<String, String> headers;

    public DeleteUserRequestConverter(Map<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public DeleteUserCommand convertFrom(DeleteUserRequest deleteUserRequest) {
        return new DeleteUserCommand(
                deleteUserRequest.getReasonsList(),
                deleteUserRequest.hasComment() ? deleteUserRequest.getComment().getValue() : null,
                headers
        );
    }
}
