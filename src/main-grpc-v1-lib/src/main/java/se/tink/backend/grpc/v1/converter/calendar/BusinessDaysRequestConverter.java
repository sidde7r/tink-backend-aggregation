package se.tink.backend.grpc.v1.converter.calendar;

import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.main.rpc.calendar.GetBusinessDaysCommand;
import se.tink.grpc.v1.rpc.ListBusinessDaysRequest;

public class BusinessDaysRequestConverter implements Converter<ListBusinessDaysRequest, GetBusinessDaysCommand> {

    @Override
    public GetBusinessDaysCommand convertFrom(ListBusinessDaysRequest input) {
        return new GetBusinessDaysCommand(input.getStartYear(), input.getStartMonth(), input.getMonths());
    }
}
