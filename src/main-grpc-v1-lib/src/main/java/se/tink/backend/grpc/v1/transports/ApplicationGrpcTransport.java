package se.tink.backend.grpc.v1.transports;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.grpc.stub.StreamObserver;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticationContext;
import se.tink.backend.common.application.ApplicationAlreadySignedException;
import se.tink.backend.common.application.ApplicationCannotBeDeletedException;
import se.tink.backend.common.application.ApplicationNotCompleteException;
import se.tink.backend.common.application.ApplicationNotFoundException;
import se.tink.backend.common.application.ApplicationNotModifiableException;
import se.tink.backend.common.application.ApplicationNotValidException;
import se.tink.backend.common.application.ApplicationSigningNotInvokableException;
import se.tink.backend.common.exceptions.FeatureFlagNotEnabledException;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationSummary;
import se.tink.backend.core.User;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.grpc.v1.converter.EnumMappers;
import se.tink.backend.grpc.v1.converter.application.ApplicationConverter;
import se.tink.backend.grpc.v1.converter.application.ApplicationSummaryConverter;
import se.tink.backend.grpc.v1.converter.application.GrpcApplicationFormConverter;
import se.tink.backend.grpc.v1.converter.transfer.CoreSignableOperationToGrpcSignableOperationConverter;
import se.tink.backend.grpc.v1.errors.ApiError;
import se.tink.backend.grpc.v1.interceptors.AuthenticationInterceptor;
import se.tink.backend.main.controllers.ApplicationServiceController;
import se.tink.backend.rpc.ApplicationSummaryListResponse;
import se.tink.backend.rpc.application.ApplicationListCommand;
import se.tink.backend.rpc.application.CreateApplicationCommand;
import se.tink.backend.rpc.application.DeleteApplicationCommand;
import se.tink.backend.rpc.application.GetApplicationCommand;
import se.tink.backend.rpc.application.GetEligibleApplicationTypesCommand;
import se.tink.backend.rpc.application.GetSummaryCommand;
import se.tink.backend.rpc.application.SubmitApplicationCommand;
import se.tink.backend.rpc.application.SubmitApplicationFormCommand;
import se.tink.grpc.v1.rpc.CreateApplicationRequest;
import se.tink.grpc.v1.rpc.CreateApplicationResponse;
import se.tink.grpc.v1.rpc.DeleteApplicationRequest;
import se.tink.grpc.v1.rpc.DeleteApplicationResponse;
import se.tink.grpc.v1.rpc.GetApplicationRequest;
import se.tink.grpc.v1.rpc.GetApplicationResponse;
import se.tink.grpc.v1.rpc.GetApplicationSummaryListRequest;
import se.tink.grpc.v1.rpc.GetApplicationSummaryListResponse;
import se.tink.grpc.v1.rpc.GetApplicationSummaryRequest;
import se.tink.grpc.v1.rpc.GetApplicationSummaryResponse;
import se.tink.grpc.v1.rpc.GetEligibleApplicationTypesRequest;
import se.tink.grpc.v1.rpc.GetEligibleApplicationTypesResponse;
import se.tink.grpc.v1.rpc.SubmitApplicationFormRequest;
import se.tink.grpc.v1.rpc.SubmitApplicationFormResponse;
import se.tink.grpc.v1.rpc.SubmitApplicationRequest;
import se.tink.grpc.v1.rpc.SubmitApplicationResponse;
import se.tink.grpc.v1.services.ApplicationServiceGrpc;
import se.tink.libraries.application.ApplicationType;

import javax.inject.Inject;
import java.util.Set;
import java.util.stream.Collectors;

public class ApplicationGrpcTransport extends ApplicationServiceGrpc.ApplicationServiceImplBase {

    private ApplicationServiceController applicationServiceController;

    @Inject
    public ApplicationGrpcTransport(ApplicationServiceController applicationServiceController) {
        this.applicationServiceController = applicationServiceController;
    }

    @Override
    @Authenticated
    public void createApplication(CreateApplicationRequest request, StreamObserver<CreateApplicationResponse> responseObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();

        try {
            CreateApplicationCommand command = new CreateApplicationCommand(
                    authenticationContext.getUser(),
                    authenticationContext.getUserAgent(),
                    EnumMappers.APPLICATION_TYPE_TO_GRPC.inverse().getOrDefault(request.getType(), null));
            Application application = applicationServiceController.createApplication(command);
            ApplicationConverter converter = new ApplicationConverter();
            responseObserver.onNext(
                    CreateApplicationResponse
                            .newBuilder()
                            .setApplication(converter.convertFrom(application))
                            .build());
            responseObserver.onCompleted();
        } catch (FeatureFlagNotEnabledException e) {
            throw ApiError.Applications.PERMISSION_DENIED.withCause(e).exception();
        } catch (ApplicationNotValidException e) {
            throw ApiError.Applications.INTERNAL_ERROR.withCause(e).exception();
        } catch (IllegalArgumentException e) {
            throw ApiError.Applications.INTERNAL_ERROR.withCause(e).exception();
        }
    }

    @Override
    @Authenticated
    public void getApplication(GetApplicationRequest request, StreamObserver<GetApplicationResponse> responseObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();

        try {
            GetApplicationCommand command = new GetApplicationCommand(request.getApplicationId(), user,
                    authenticationContext.getUserAgent());
            Application application = applicationServiceController.getApplication(command);
            ApplicationConverter converter = new ApplicationConverter();

            responseObserver.onNext(GetApplicationResponse.newBuilder().setApplication(converter.convertFrom(application)).build());
            responseObserver.onCompleted();
        } catch (ApplicationNotValidException e) {
            throw ApiError.Applications.APPLICATION_NOT_VALID.withCause(e).exception();
        } catch (ApplicationNotFoundException e) {
            throw ApiError.Applications.NOT_FOUND.withCause(e).exception();
        } catch (IllegalArgumentException e) {
            throw ApiError.Applications.INTERNAL_ERROR.withCause(e).exception();
        }
    }

    @Override
    @Authenticated
    public void deleteApplication(DeleteApplicationRequest request, StreamObserver<DeleteApplicationResponse> responseObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();

        try {
            DeleteApplicationCommand command = new DeleteApplicationCommand(user.getId(), request.getApplicationId());
            applicationServiceController.delete(command);

            responseObserver.onNext(DeleteApplicationResponse.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (ApplicationNotFoundException e) {
            throw ApiError.Applications.NOT_FOUND.withCause(e).exception();
        } catch (ApplicationCannotBeDeletedException e) {
            throw ApiError.Applications.FORBIDDEN.withCause(e).exception();
        } catch (IllegalArgumentException e) {
            throw ApiError.Applications.INTERNAL_ERROR.withCause(e).exception();
        }
    }

    @Override
    @Authenticated
    public void getApplicationSummary(GetApplicationSummaryRequest request, StreamObserver<GetApplicationSummaryResponse> responseObserver) {

        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();
        try {
            GetSummaryCommand command = new GetSummaryCommand(request.getApplicationId(), user,
                    authenticationContext.getUserAgent());
            ApplicationSummary applicationSummary = applicationServiceController.getSummary(command);
            ApplicationSummaryConverter converter = new ApplicationSummaryConverter();

            responseObserver.onNext(GetApplicationSummaryResponse
                    .newBuilder().setSummary(converter.convertFrom(applicationSummary)).build());
            responseObserver.onCompleted();
        } catch (ApplicationNotFoundException e) {
            throw ApiError.Applications.NOT_FOUND.withCause(e).exception();
        } catch (IllegalArgumentException e) {
            throw ApiError.Applications.INTERNAL_ERROR.withCause(e).exception();
        }
    }

    @Override
    @Authenticated
    public void getApplicationSummaryList(GetApplicationSummaryListRequest request, StreamObserver<GetApplicationSummaryListResponse> responseObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();

        try {
            ApplicationListCommand command = new ApplicationListCommand(user, authenticationContext.getUserAgent());
            ApplicationSummaryListResponse controllerResponse = applicationServiceController.list(command);
            ApplicationSummaryConverter converter = new ApplicationSummaryConverter();

            responseObserver.onNext(GetApplicationSummaryListResponse.newBuilder()
                    .addAllSummary(converter.convertFrom(controllerResponse.getSummaries())).build());
            responseObserver.onCompleted();
        } catch (ApplicationNotValidException e) {
            throw ApiError.Applications.COULD_NOT_PROCESS.withCause(e).exception();
        } catch (IllegalArgumentException e) {
            throw ApiError.Applications.INTERNAL_ERROR.withCause(e).exception();
        }

    }

    @Override
    @Authenticated
    public void submitApplicationForm(SubmitApplicationFormRequest request, StreamObserver<SubmitApplicationFormResponse> responseObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();

        try {
            SubmitApplicationFormCommand command = new SubmitApplicationFormCommand(request.getApplicationId(), user,
                    authenticationContext.getUserAgent());
            GrpcApplicationFormConverter requestConverter = new GrpcApplicationFormConverter();
            Application application = applicationServiceController.submitForm(command, requestConverter.convertFrom(request));
            ApplicationConverter converter = new ApplicationConverter();

            responseObserver.onNext(SubmitApplicationFormResponse.newBuilder().setApplication(converter.convertFrom(application)).build());
            responseObserver.onCompleted();
        } catch (ApplicationNotModifiableException e) {
            throw ApiError.Applications.FORBIDDEN.withCause(e).exception();
        } catch (ApplicationNotValidException e) {
            throw ApiError.Applications.FAILED_TO_SUBMIT_FORM.withCause(e).exception();
        } catch (ApplicationNotFoundException e) {
            throw ApiError.Applications.NOT_FOUND.withCause(e).exception();
        } catch (IllegalArgumentException e) {
            throw ApiError.Applications.INTERNAL_ERROR.withCause(e).exception();
        } catch (Exception e) {
            throw ApiError.Applications.INTERNAL_ERROR.withCause(e).exception();
        }
    }

    @Override
    @Authenticated
    public void submitApplication(SubmitApplicationRequest request, StreamObserver<SubmitApplicationResponse> responseObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();

        try {
            SubmitApplicationCommand command = new SubmitApplicationCommand(request.getApplicationId(), user,
                    authenticationContext.getUserAgent(), authenticationContext.getRemoteAddress());
            CoreSignableOperationToGrpcSignableOperationConverter converter = new CoreSignableOperationToGrpcSignableOperationConverter();
            SignableOperation operation = applicationServiceController.submit(command);

            responseObserver.onNext(SubmitApplicationResponse.newBuilder().setSignableOperation(converter.convertFrom(operation)).build());
            responseObserver.onCompleted();
        } catch (ApplicationAlreadySignedException e) {
            throw ApiError.Applications.FORBIDDEN.withCause(e).exception();
        } catch (ApplicationNotCompleteException e) {
            throw ApiError.Applications.APPLICATION_NOT_COMPLETE.withCause(e).exception();
        } catch (JsonProcessingException e) {
            throw ApiError.Applications.INTERNAL_ERROR.withCause(e).exception();
        } catch (ApplicationSigningNotInvokableException e) {
            throw ApiError.Applications.SIGNING_NOT_INVOKABLE.withCause(e).exception();
        } catch (ApplicationNotFoundException e) {
            throw ApiError.Applications.NOT_FOUND.withCause(e).exception();
        } catch (IllegalArgumentException e) {
            throw ApiError.Applications.INTERNAL_ERROR.withCause(e).exception();
        }
    }

    @Override
    @Authenticated
    public void getEligibleApplicationTypes(GetEligibleApplicationTypesRequest request, StreamObserver<GetEligibleApplicationTypesResponse> responseObserver) {

        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();
        Set<ApplicationType> types = applicationServiceController.getEligibleApplicationTypes(
                new GetEligibleApplicationTypesCommand(user.getId()));
        responseObserver.onNext(GetEligibleApplicationTypesResponse.newBuilder()
                .addAllTypes(
                    types.stream()
                        .map(t -> EnumMappers.APPLICATION_TYPE_TO_GRPC.getOrDefault(t,
                                se.tink.grpc.v1.models.ApplicationType.APPLICATION_TYPE_UNKNOWN))
                        .collect(Collectors.toList()))
                .build());
        responseObserver.onCompleted();
    }
}
