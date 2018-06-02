package se.tink.backend.grpc.v1.transports;

import com.google.common.collect.BiMap;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticationContext;
import se.tink.backend.common.exceptions.DuplicateException;
import se.tink.backend.common.exceptions.LockException;
import se.tink.backend.core.User;
import se.tink.backend.core.follow.FollowItem;
import se.tink.backend.core.follow.FollowItemHistory;
import se.tink.backend.core.oauth2.OAuth2AuthorizationScopeTypes;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.backend.grpc.v1.converter.follow.CoreFollowItemHistoryToGrpcConverter;
import se.tink.backend.grpc.v1.converter.follow.CoreFollowItemToGrpcFollowItemConverter;
import se.tink.backend.grpc.v1.converter.follow.CreateFollowItemRequestConverter;
import se.tink.backend.grpc.v1.converter.follow.SuggestFollowItemRequestConverter;
import se.tink.backend.grpc.v1.converter.follow.UpdateFollowItemRequestConverter;
import se.tink.backend.grpc.v1.converter.periods.CorePeriodToGrpcPeriodConverter;
import se.tink.backend.grpc.v1.errors.ApiError;
import se.tink.backend.grpc.v1.interceptors.AuthenticationInterceptor;
import se.tink.backend.main.controllers.FollowServiceController;
import se.tink.backend.main.controllers.UserServiceController;
import se.tink.backend.main.controllers.exceptions.FollowItemNotFoundException;
import se.tink.grpc.v1.models.Period;
import se.tink.grpc.v1.rpc.CreateFollowItemRequest;
import se.tink.grpc.v1.rpc.CreateFollowItemResponse;
import se.tink.grpc.v1.rpc.DeleteFollowItemRequest;
import se.tink.grpc.v1.rpc.DeleteFollowItemResponse;
import se.tink.grpc.v1.rpc.GetFollowItemHistoryRequest;
import se.tink.grpc.v1.rpc.GetFollowItemHistoryResponse;
import se.tink.grpc.v1.rpc.GetFollowItemRequest;
import se.tink.grpc.v1.rpc.GetFollowItemResponse;
import se.tink.grpc.v1.rpc.ListFollowItemRequest;
import se.tink.grpc.v1.rpc.ListFollowItemResponse;
import se.tink.grpc.v1.rpc.SuggestFollowItemRequest;
import se.tink.grpc.v1.rpc.SuggestFollowItemResponse;
import se.tink.grpc.v1.rpc.UpdateFollowItemRequest;
import se.tink.grpc.v1.rpc.UpdateFollowItemResponse;
import se.tink.grpc.v1.services.FollowServiceGrpc;

public class FollowGrpcTransport extends FollowServiceGrpc.FollowServiceImplBase {
    private final FollowServiceController followServiceController;
    private final UserServiceController userServiceController;

    private final BiMap<String, String> categoryCodeById;

    @Inject
    public FollowGrpcTransport(FollowServiceController followServiceController,
            UserServiceController userServiceController,
            @Named("categoryCodeById") BiMap<String, String> categoryCodeById) {
        this.followServiceController = followServiceController;
        this.userServiceController = userServiceController;
        this.categoryCodeById = categoryCodeById;
    }

    @Override
    @Authenticated(scopes = OAuth2AuthorizationScopeTypes.FOLLOW_WRITE)
    public void createFollowItem(CreateFollowItemRequest createFollowItemRequest,
            StreamObserver<CreateFollowItemResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();
        CreateFollowItemRequestConverter followItemRequestConverter = new CreateFollowItemRequestConverter(
                categoryCodeById.inverse());

        try {
            CoreFollowItemToGrpcFollowItemConverter followItemConverter = new CoreFollowItemToGrpcFollowItemConverter(
                    getUserPeriodsMap(user), user.getProfile().getCurrency(), categoryCodeById);

            FollowItem followItem = followServiceController
                    .create(user, followItemRequestConverter.convertFrom(createFollowItemRequest),
                            authenticationContext.getRemoteAddress());
            streamObserver.onNext(CreateFollowItemResponse.newBuilder()
                    .setFollowItem(followItemConverter.convertFrom(followItem))
                    .build());
            streamObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            throw ApiError.FollowItems.BAD_REQUEST.withCause(e).exception();
        } catch (DuplicateException e) {
            throw ApiError.FollowItems.CONFLICT.withCause(e).withInfoSeverity().exception();
        }
    }

    @Override
    @Authenticated(scopes = OAuth2AuthorizationScopeTypes.FOLLOW_WRITE)
    public void updateFollowItem(UpdateFollowItemRequest updateFollowItemRequest,
            StreamObserver<UpdateFollowItemResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();

        UpdateFollowItemRequestConverter updateFollowItemRequestConverter = new UpdateFollowItemRequestConverter(
                categoryCodeById.inverse());

        try {
            CoreFollowItemToGrpcFollowItemConverter followItemConverter = new CoreFollowItemToGrpcFollowItemConverter(
                    getUserPeriodsMap(user), user.getProfile().getCurrency(), categoryCodeById);

            FollowItem followItem = followServiceController.update(user, updateFollowItemRequest.getFollowItemId(),
                    updateFollowItemRequestConverter.convertFrom(updateFollowItemRequest),
                    authenticationContext.getRemoteAddress());

            streamObserver.onNext(UpdateFollowItemResponse.newBuilder()
                    .setFollowItem(followItemConverter.convertFrom(followItem))
                    .build());
            streamObserver.onCompleted();
        } catch (DuplicateException e) {
            throw ApiError.FollowItems.CONFLICT.withCause(e).exception();
        } catch (IllegalArgumentException e) {
            throw ApiError.FollowItems.BAD_REQUEST.withCause(e).exception();
        } catch (NoSuchElementException e) {
            throw ApiError.FollowItems.NOT_FOUND.withWarnSeverity().withCause(e).exception();
        }
    }

    @Override
    @Authenticated(scopes = OAuth2AuthorizationScopeTypes.FOLLOW_READ)
    public void getFollowItem(GetFollowItemRequest getFollowItemRequest,
            StreamObserver<GetFollowItemResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();

        try {
            CoreFollowItemToGrpcFollowItemConverter followItemConverter = new CoreFollowItemToGrpcFollowItemConverter(
                    getUserPeriodsMap(user), user.getProfile().getCurrency(), categoryCodeById);

            FollowItem followItem = followServiceController
                    .get(user, getFollowItemRequest.getFollowItemId(), getFollowItemRequest.getMonthPeriod());
            streamObserver.onNext(GetFollowItemResponse.newBuilder()
                    .setFollowItem(followItemConverter.convertFrom(followItem))
                    .build());
            streamObserver.onCompleted();
        } catch (NoSuchElementException e) {
            throw ApiError.FollowItems.NOT_FOUND.withCause(e).exception();
        }
    }

    @Override
    @Authenticated(scopes = OAuth2AuthorizationScopeTypes.FOLLOW_READ)
    public void getFollowItemHistory(GetFollowItemHistoryRequest request,
            StreamObserver<GetFollowItemHistoryResponse> streamObserver) {
        User user = AuthenticationInterceptor.CONTEXT.get().getUser();

        try {
            CoreFollowItemHistoryToGrpcConverter converter = new CoreFollowItemHistoryToGrpcConverter();

            FollowItemHistory history = followServiceController.getFollowItemHistory(user, request.getFollowItemId(),
                    EnumMappers.CORE_PERIOD_MODE_TO_GRPC_MAP.inverse().get(request.getPeriodMode()));

            streamObserver.onNext(GetFollowItemHistoryResponse.newBuilder()
                    .setFollowItemHistory(converter.convertFrom(history))
                    .build());
            streamObserver.onCompleted();
        } catch (FollowItemNotFoundException e) {
            throw ApiError.FollowItems.NOT_FOUND.withCause(e).exception();
        } catch (LockException e) {
            throw ApiError.FollowItems.BAD_REQUEST.withCause(e).exception();
        }
    }

    @Override
    @Authenticated(scopes = OAuth2AuthorizationScopeTypes.FOLLOW_WRITE)
    public void deleteFollowItem(DeleteFollowItemRequest deleteFollowItemRequest,
            StreamObserver<DeleteFollowItemResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();

        try {
            followServiceController.delete(authenticationContext.getUser(), deleteFollowItemRequest.getFollowItemId(),
                    authenticationContext.getRemoteAddress());
            streamObserver.onNext(DeleteFollowItemResponse.getDefaultInstance());
            streamObserver.onCompleted();
        } catch (NoSuchElementException e) {
            throw ApiError.FollowItems.NOT_FOUND.withCause(e).exception();
        }
    }

    @Override
    @Authenticated(scopes = OAuth2AuthorizationScopeTypes.FOLLOW_READ)
    public void listFollowItem(ListFollowItemRequest listFollowItemRequest,
            StreamObserver<ListFollowItemResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();
        CoreFollowItemToGrpcFollowItemConverter followItemConverter = new CoreFollowItemToGrpcFollowItemConverter(
                getUserPeriodsMap(user), user.getProfile().getCurrency(), categoryCodeById);

        List<FollowItem> followItems = followServiceController
                .list(user, false, listFollowItemRequest.getMonthPeriod());
        streamObserver.onNext(ListFollowItemResponse.newBuilder()
                .addAllFollowItems(followItemConverter.convertFrom(followItems))
                .build());
        streamObserver.onCompleted();
    }

    @Override
    @Authenticated(scopes = OAuth2AuthorizationScopeTypes.FOLLOW_READ)
    public void suggestFollowItem(SuggestFollowItemRequest suggestFollowItemRequest,
            StreamObserver<SuggestFollowItemResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();
        SuggestFollowItemRequestConverter requestConverter = new SuggestFollowItemRequestConverter(
                categoryCodeById.inverse());

        try {
            CoreFollowItemToGrpcFollowItemConverter followItemConverter = new CoreFollowItemToGrpcFollowItemConverter(
                    getUserPeriodsMap(user), user.getProfile().getCurrency(), categoryCodeById);

            FollowItem followItem = followServiceController
                    .suggestFollowItem(user, requestConverter.convertFrom(suggestFollowItemRequest));
            streamObserver.onNext(SuggestFollowItemResponse.newBuilder()
                    .setFollowItem(followItemConverter.convertFrom(followItem))
                    .build());
            streamObserver.onCompleted();
        } catch (DuplicateException e) {
            throw ApiError.FollowItems.CONFLICT.withCause(e).exception();
        }
    }

    private Map<String, Period> getUserPeriodsMap(User user) {
        List<se.tink.libraries.date.Period> periods = userServiceController.getPeriods(user);
        CorePeriodToGrpcPeriodConverter periodConverter = new CorePeriodToGrpcPeriodConverter();
        return periods.stream()
                .collect(Collectors.toMap(se.tink.libraries.date.Period::getName, periodConverter::convertFrom));
    }
}
