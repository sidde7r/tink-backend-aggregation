package se.tink.backend.grpc.v1.transports;

import com.google.inject.Inject;
import io.grpc.stub.StreamObserver;
import se.tink.backend.auth.Authenticated;
import se.tink.backend.auth.AuthenticationContext;
import se.tink.backend.core.User;
import se.tink.backend.grpc.v1.converter.calendar.BusinessDaysRequestConverter;
import se.tink.backend.grpc.v1.converter.calendar.BusinessDaysResponseConverter;
import se.tink.backend.grpc.v1.interceptors.AuthenticationInterceptor;
import se.tink.backend.main.controllers.CalendarServiceController;
import se.tink.grpc.v1.rpc.ListBusinessDaysRequest;
import se.tink.grpc.v1.rpc.ListBusinessDaysResponse;
import se.tink.grpc.v1.services.CalendarServiceGrpc;

import java.util.List;
import java.util.Map;

public class CalendarGrpcTransport extends CalendarServiceGrpc.CalendarServiceImplBase {

    private CalendarServiceController calendarServiceController;

    @Inject
    public CalendarGrpcTransport(CalendarServiceController calendarServiceController) {
        this.calendarServiceController = calendarServiceController;
    }

    @Override
    @Authenticated
    public void getBusinessDays(ListBusinessDaysRequest request, StreamObserver<ListBusinessDaysResponse> responseObserver) {
        AuthenticationContext authenticationContext = AuthenticationInterceptor.CONTEXT.get();
        User user = authenticationContext.getUser();

        BusinessDaysRequestConverter converter = new BusinessDaysRequestConverter();

        Map<String, Map<String, List<Integer>>> businessDays = calendarServiceController.getBusinessDays(converter.convertFrom(request));

        BusinessDaysResponseConverter responseConverter = new BusinessDaysResponseConverter();
        responseObserver.onNext(responseConverter.convertFrom(businessDays));
        responseObserver.onCompleted();

    }
}
