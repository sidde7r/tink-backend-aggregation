package se.tink.backend.grpc.v1.transports;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.List;
import javax.inject.Inject;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticationContext;
import se.tink.backend.core.User;
import se.tink.backend.core.property.Property;
import se.tink.backend.grpc.v1.converter.properties.CorePropertyToGrpcPropertyConverter;
import se.tink.backend.grpc.v1.errors.ApiError;
import se.tink.backend.grpc.v1.interceptors.AuthenticationInterceptor;
import se.tink.backend.main.controllers.PropertyServiceController;
import se.tink.backend.main.controllers.exceptions.PropertyNotFoundException;
import se.tink.backend.rpc.properties.UpdatePropertyCommand;
import se.tink.grpc.v1.rpc.DeleteValuationRequest;
import se.tink.grpc.v1.rpc.DeleteValuationResponse;
import se.tink.grpc.v1.rpc.GetPropertyEventsRequest;
import se.tink.grpc.v1.rpc.GetPropertyEventsResponse;
import se.tink.grpc.v1.rpc.GetPropertyRequest;
import se.tink.grpc.v1.rpc.GetPropertyResponse;
import se.tink.grpc.v1.rpc.ListPropertiesRequest;
import se.tink.grpc.v1.rpc.ListPropertiesResponse;
import se.tink.grpc.v1.rpc.UpdatePropertyRequest;
import se.tink.grpc.v1.rpc.UpdatePropertyResponse;
import se.tink.grpc.v1.services.PropertyServiceGrpc;

public class PropertyGrpcTransport extends PropertyServiceGrpc.PropertyServiceImplBase {
    private PropertyServiceController propertyServiceController;

    @Inject
    public PropertyGrpcTransport(PropertyServiceController propertyServiceController) {
        this.propertyServiceController = propertyServiceController;
    }

    @Override
    @Authenticated
    public void listProperties(ListPropertiesRequest request, StreamObserver<ListPropertiesResponse> responseObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();

        List<Property> properties = propertyServiceController.list(user);

        CorePropertyToGrpcPropertyConverter converter = new CorePropertyToGrpcPropertyConverter();

        responseObserver.onNext(ListPropertiesResponse.newBuilder().addAllProperties(converter.convertFrom(properties))
                .build());
        responseObserver.onCompleted();
    }

    @Override
    @Authenticated
    public void getProperty(GetPropertyRequest request, StreamObserver<GetPropertyResponse> responseObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();

        try {
            Property property = propertyServiceController.get(user, request.getPropertyId());

            CorePropertyToGrpcPropertyConverter converter = new CorePropertyToGrpcPropertyConverter();

            responseObserver.onNext(GetPropertyResponse.newBuilder().setProperty(converter.convertFrom(property))
                    .build());
            responseObserver.onCompleted();
        } catch (PropertyNotFoundException e) {
            throw ApiError.Properties.NOT_FOUND.withCause(e).exception();
        }
    }

    @Override
    @Authenticated
    public void updateProperty(UpdatePropertyRequest request, StreamObserver<UpdatePropertyResponse> responseObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();

        try {
            UpdatePropertyCommand command = UpdatePropertyCommand.builder()
                    .withUser(user.getId())
                    .withPropertyId(request.getPropertyId())
                    .withNumberOfRooms(request.getNumberOfRooms())
                    .withNumberOfSquareMeters(request.getNumberOfSquareMeters())
                    .build();

            Property property = propertyServiceController.update(command);

            responseObserver.onNext(UpdatePropertyResponse.newBuilder()
                    .setProperty(new CorePropertyToGrpcPropertyConverter().convertFrom(property))
                    .build());
            responseObserver.onCompleted();
        } catch (PropertyNotFoundException e) {
            throw ApiError.Properties.NOT_FOUND.withCause(e).exception();
        }
    }

    @Override
    @Authenticated
    public void deleteValuation(DeleteValuationRequest request,
            StreamObserver<DeleteValuationResponse> responseObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();

        try {
            Property property = propertyServiceController.deleteValuation(user, request.getPropertyId());

            responseObserver.onNext(DeleteValuationResponse.newBuilder()
                    .setProperty(new CorePropertyToGrpcPropertyConverter().convertFrom(property))
                    .build());
            responseObserver.onCompleted();
        } catch (PropertyNotFoundException e) {
            throw ApiError.Properties.NOT_FOUND.withCause(e).exception();
        }
    }

    @Override
    @Authenticated
    public void getPropertyEvents(GetPropertyEventsRequest request,
            StreamObserver<GetPropertyEventsResponse> responseObserver) {
        responseObserver.onError(Status.UNIMPLEMENTED.asRuntimeException());
    }
}
