package se.tink.backend.main.transports.converters.user;

import javax.ws.rs.core.HttpHeaders;
import se.tink.backend.common.resources.RequestHeaderUtils;
import se.tink.backend.rpc.DeleteUserCommand;
import se.tink.backend.rpc.DeleteUserRequest;

public class DeleteUserCommandConverter {
    public static DeleteUserCommand convertFrom(DeleteUserRequest deleteRequest, HttpHeaders httpHeaders) {
        if (deleteRequest == null) {
            return new DeleteUserCommand(null, null, RequestHeaderUtils.getHeadersMap(httpHeaders));
        } else {
            return new DeleteUserCommand(deleteRequest.getReasons(), deleteRequest.getComment(),
                    RequestHeaderUtils.getHeadersMap(httpHeaders));
        }
    }
}
