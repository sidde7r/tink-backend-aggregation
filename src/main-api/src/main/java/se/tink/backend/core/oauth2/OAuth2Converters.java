package se.tink.backend.core.oauth2;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import se.tink.oauth.grpc.Client;

public class OAuth2Converters {

    public static OAuth2Client toClient(Client grpcClient) {
        OAuth2Client oAuth2Client = new OAuth2Client();

        oAuth2Client.setId(grpcClient.getId());
        oAuth2Client.setIconUrl(grpcClient.getIconUrl());
        oAuth2Client.setName(grpcClient.getName());
        oAuth2Client.setPayloadSerialized(grpcClient.getPayload());
        oAuth2Client.setSecret(grpcClient.getSecret());
        oAuth2Client.setRedirectUris(Sets.newHashSet(grpcClient.getRedirectUrisList()));
        oAuth2Client.setUrl(grpcClient.getUrl());

        // TODO: change scope to be a list of strings and not a CSV string.
        oAuth2Client.setScope(String.join(",", Lists.newArrayList(grpcClient.getScopeList())));

        return oAuth2Client;
    }
}
