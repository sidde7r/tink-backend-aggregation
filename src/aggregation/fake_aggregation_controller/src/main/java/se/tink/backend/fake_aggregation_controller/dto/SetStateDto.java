package se.tink.backend.fake_aggregation_controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SetStateDto {
    private String credentialsId;
    private String state;
}
