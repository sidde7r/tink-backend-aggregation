package se.tink.backend.grpc.v1.transports;

import com.google.common.collect.BiMap;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticationContext;
import se.tink.backend.common.providers.CurrenciesByCodeProvider;
import se.tink.backend.core.User;
import se.tink.libraries.identity.model.Identity;
import se.tink.libraries.identity.model.IdentityEvent;
import se.tink.libraries.identity.model.IdentityEventSummary;
import se.tink.backend.grpc.v1.converter.identity.AnswerIdentityEventCommandConverter;
import se.tink.backend.grpc.v1.converter.identity.GetIdentityEventSummaryCommandConverter;
import se.tink.backend.grpc.v1.converter.identity.GetIdentityEventToCommandConverter;
import se.tink.backend.grpc.v1.converter.identity.IdentityEventConverter;
import se.tink.backend.grpc.v1.converter.identity.IdentityEventSummaryToResponseConverter;
import se.tink.backend.grpc.v1.converter.identity.IdentityToGrpcConverter;
import se.tink.backend.grpc.v1.converter.identity.SeenIdentityEventCommandConverter;
import se.tink.backend.grpc.v1.errors.ApiError;
import se.tink.backend.grpc.v1.interceptors.AuthenticationInterceptor;
import se.tink.backend.main.controllers.IdentityServiceController;
import se.tink.libraries.identity.commands.GetIdentityStateCommand;
import se.tink.grpc.v1.rpc.AnswerIdentityEventRequest;
import se.tink.grpc.v1.rpc.AnswerIdentityEventResponse;
import se.tink.grpc.v1.rpc.GetIdentityEventRequest;
import se.tink.grpc.v1.rpc.IdentityEventListRequest;
import se.tink.grpc.v1.rpc.IdentityEventListResponse;
import se.tink.grpc.v1.rpc.IdentityEventResponse;
import se.tink.grpc.v1.rpc.IdentityStateRequest;
import se.tink.grpc.v1.rpc.IdentityStateResponse;
import se.tink.grpc.v1.rpc.SeenIdentityEventRequest;
import se.tink.grpc.v1.rpc.SeenIdentityEventResponse;
import se.tink.grpc.v1.services.IdentityServiceGrpc;

public class IdentityGrpcTransport extends IdentityServiceGrpc.IdentityServiceImplBase {
    private final IdentityServiceController identityServiceController;
    private final CurrenciesByCodeProvider currenciesByCodeProvider;
    private final BiMap<String, String> categoryCodeById;

    @Inject
    public IdentityGrpcTransport(IdentityServiceController identityServiceController,
            CurrenciesByCodeProvider currenciesByCodeProvider,
            @Named("categoryCodeById") BiMap<String, String> categoryCodeById) {
        this.identityServiceController = identityServiceController;
        this.currenciesByCodeProvider = currenciesByCodeProvider;
        this.categoryCodeById = categoryCodeById;
    }

    @Override
    @Authenticated
    public void getIdentityEvent(GetIdentityEventRequest request,
            StreamObserver<IdentityEventResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();

        try {
            GetIdentityEventToCommandConverter converter = new GetIdentityEventToCommandConverter(user,
                    currenciesByCodeProvider);
            IdentityEvent identityEvent = identityServiceController.getIdentityEvent(converter.convertFrom(request));
            IdentityEventConverter responseConverter = new IdentityEventConverter(user.getProfile().getCurrency(),
                    categoryCodeById);
            streamObserver.onNext(IdentityEventResponse.newBuilder().setEvent(responseConverter.convertFrom(identityEvent)).build());
            streamObserver.onCompleted();
        } catch (NoSuchElementException e) {
            throw ApiError.Identity.NOT_FOUND.exception();
        } catch (IllegalArgumentException e) {
            throw ApiError.Identity.INTERNAL_ERROR.withCause(e).exception();
        }
    }

    @Override
    @Authenticated
    public void getIdentityEventSummaryList(IdentityEventListRequest request, StreamObserver<IdentityEventListResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();
        GetIdentityEventSummaryCommandConverter converter = new GetIdentityEventSummaryCommandConverter(
                currenciesByCodeProvider);
        List<IdentityEventSummary> identityEventList = identityServiceController
                .getIdentityEventSummaryList(converter.convertFrom(user));
        IdentityEventSummaryToResponseConverter responseConverter = new IdentityEventSummaryToResponseConverter();
        streamObserver.onNext(IdentityEventListResponse.newBuilder()
                .addAllEvents(responseConverter.convertFrom(identityEventList)).build());
        streamObserver.onCompleted();
    }

    @Override
    @Authenticated
    public void seenIdentityEvents(SeenIdentityEventRequest request,
            StreamObserver<SeenIdentityEventResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();

        SeenIdentityEventCommandConverter converter = new SeenIdentityEventCommandConverter(user,
                currenciesByCodeProvider);
        try {
            List<IdentityEventSummary> identityEventList = identityServiceController.seenIdentityEvents(converter.convertFrom(request));
            IdentityEventSummaryToResponseConverter responseConverter = new IdentityEventSummaryToResponseConverter();
            streamObserver.onNext(SeenIdentityEventResponse.newBuilder()
                    .addAllEvents(responseConverter.convertFrom(identityEventList)).build());
            streamObserver.onCompleted();
        } catch(IllegalArgumentException e){
            throw ApiError.Identity.INTERNAL_ERROR.withCause(e).exception();
        }
    }

    @Override
    @Authenticated
    public void answerIdentityEvent(AnswerIdentityEventRequest request,
            StreamObserver<AnswerIdentityEventResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();

        AnswerIdentityEventCommandConverter converter = new AnswerIdentityEventCommandConverter(user,
                currenciesByCodeProvider);
        try {
            IdentityEvent identityEvent = identityServiceController.answerIdentityEvent(converter.convertFrom(request));
            IdentityEventConverter responseConverter = new IdentityEventConverter(user.getProfile().getCurrency(),
                    categoryCodeById);
            streamObserver.onNext(AnswerIdentityEventResponse.newBuilder()
                    .setEvent(responseConverter.convertFrom(identityEvent)).build());
            streamObserver.onCompleted();
        } catch(IllegalArgumentException e){
            throw ApiError.Identity.INTERNAL_ERROR.withCause(e).exception();
        }
    }

    @Override
    @Authenticated
    public void getIdentityState(IdentityStateRequest request, StreamObserver<IdentityStateResponse> streamObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();

        try {
            GetIdentityStateCommand cmd = new GetIdentityStateCommand(user.getId());
            Optional<Identity> identity = identityServiceController.getIdentityState(cmd);
            IdentityToGrpcConverter responseConverter = new IdentityToGrpcConverter(user.getProfile().getCurrency());

            if (!identity.isPresent()) {
                throw ApiError.Identity.NOT_FOUND.exception();
            }
            streamObserver.onNext(IdentityStateResponse.newBuilder()
                    .setState(responseConverter.convertFrom(identity.get())).build());
            streamObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            throw ApiError.Identity.INTERNAL_ERROR.withCause(e).exception();
        }
    }

}
