package se.tink.backend.main.resources;

import com.google.api.client.util.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.UUID;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import se.tink.backend.api.DocumentService;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.repository.cassandra.DocumentRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.CompressedDocument;
import se.tink.backend.core.User;
import se.tink.backend.rpc.TinkMediaType;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.http.utils.HttpResponseHelper;
import se.tink.libraries.uuid.UUIDUtils;

@Path("/api/v1/documents")
public class DocumentServiceResource implements DocumentService {

    private static final ImmutableSet<String> ALLOWED_MEDIA_TYPES = ImmutableSet.of(
            TinkMediaType.APPLICATION_PDF
    );

    private static final LogUtils log = new LogUtils(DocumentServiceResource.class);
    private final HttpResponseHelper httpResponseHelper;

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    public DocumentServiceResource(ServiceContext context) {
        this.httpResponseHelper = new HttpResponseHelper(log);

        this.documentRepository = context.getRepository(DocumentRepository.class);
        this.userRepository = context.getRepository(UserRepository.class);
    }

    @Override
    public Response getDocument(AuthenticatedUser authenticatedUser, String userId, UUID token) {

        /* THIS IS A METHOD FOR UNAUTHORIZED USERS */

        if (!UUIDUtils.isValidTinkUUID(userId)) {
            httpResponseHelper.error(Response.Status.BAD_REQUEST, "Invalid userId");
        }

        User user = userRepository.findOne(userId);

        if (user == null) {
            httpResponseHelper.error(Response.Status.BAD_REQUEST, "User doesn't exist");
        }

        if (token == null) {
            httpResponseHelper.error(Response.Status.BAD_REQUEST, "Invalid token");
        }

        CompressedDocument row = documentRepository.findOneByUserIdAndToken(UUIDUtils.fromTinkUUID(userId), token);

        if (row == null) {
            httpResponseHelper.error(Response.Status.BAD_REQUEST, "Document not found");
        }

        // If having another MimeType than the allowed ones--and it is OK to serve--just add
        // it to the allowed ones and don't forget to also add that MimeType to
        // DocumentService#getDocument's @Produces
        Preconditions.checkArgument(ALLOWED_MEDIA_TYPES.contains(row.getMimeType()));

        try {
            return Response.ok(row.getUncompressed(), row.getMimeType()).build();
        } catch (IOException e) {
            log.error(userId, "Could not uncompress document:", e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
