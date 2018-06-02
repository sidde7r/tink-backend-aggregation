package se.tink.backend.grpc.v1.converter.follow;

import java.util.List;
import se.tink.backend.core.follow.FollowItem;
import se.tink.grpc.v1.models.FollowItems;

public class FollowItemGrpcConverter {
    private final CoreFollowItemToGrpcFollowItemConverter coreFollowItemToGrpcFollowItemConverter;

    public FollowItemGrpcConverter(
            CoreFollowItemToGrpcFollowItemConverter coreFollowItemToGrpcFollowItemConverter) {
        this.coreFollowItemToGrpcFollowItemConverter = coreFollowItemToGrpcFollowItemConverter;
    }

    public FollowItems convertFrom(List<FollowItem> input) {
        List<se.tink.grpc.v1.models.FollowItem> followItems = coreFollowItemToGrpcFollowItemConverter.convertFrom(input);
        return FollowItems.newBuilder().addAllFollowItem(followItems).build();
    }
}
