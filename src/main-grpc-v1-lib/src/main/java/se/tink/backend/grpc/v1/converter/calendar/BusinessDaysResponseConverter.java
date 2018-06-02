package se.tink.backend.grpc.v1.converter.calendar;

import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.grpc.v1.models.BusinessDays;
import se.tink.grpc.v1.rpc.ListBusinessDaysResponse;

import java.util.List;
import java.util.Map;

public class BusinessDaysResponseConverter implements Converter<Map<String, Map<String, List<Integer>>>, ListBusinessDaysResponse> {

    // The structure of the input object is
    // Map<Year,  Map<Month, List<Days>>>
    @Override
    public ListBusinessDaysResponse convertFrom(Map<String, Map<String, List<Integer>>> input) {
        ListBusinessDaysResponse.Builder builder = ListBusinessDaysResponse.newBuilder();
        input.forEach((yearStr, monthMap) -> {
            int year = Integer.parseInt(yearStr);
            monthMap.forEach((monthStr, daysList) -> {
                int month = Integer.parseInt(monthStr);
                builder.addBusinessDays(BusinessDays.newBuilder()
                        .setYear(year)
                        .setMonth(month)
                        .addAllDays(daysList).build());
            });
        });
        return builder.build();
    }
}
