package se.tink.backend.aggregation.agents.agentplatform.authentication;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.AgentFieldDefinition;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.label.AgentFieldLabel;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.label.ConstantFieldLabel;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.label.I18NFieldLabel;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;

public class AgentInteractionDataToSupplementalInformationMapperTest {

    private SupplementalInformationFormer supplementalInformationFormer;
    private AgentInteractionDataToSupplementalInformationMapper objectUnderTest;

    @Before
    public void init() {
        supplementalInformationFormer = Mockito.mock(SupplementalInformationFormer.class);
        objectUnderTest =
                new AgentInteractionDataToSupplementalInformationMapper(
                        supplementalInformationFormer);
    }

    @Test
    public void shouldMapFieldDefinitionToAggregationField() {
        // given
        AgentFieldDefinition agentFieldDefinition = Mockito.mock(AgentFieldDefinition.class);
        AgentFieldLabel fieldLabel = Mockito.mock(I18NFieldLabel.class);
        Mockito.when(agentFieldDefinition.getFieldIdentifier()).thenReturn("username");
        Mockito.when(agentFieldDefinition.getFieldLabel()).thenReturn(fieldLabel);
        Field usernameField = Mockito.mock(Field.class);
        Mockito.when(supplementalInformationFormer.getField("username")).thenReturn(usernameField);
        // when
        Field result = objectUnderTest.toField(agentFieldDefinition);
        // then
        Assertions.assertThat(result).isEqualTo(usernameField);
    }

    @Test
    public void shouldMapFieldDefinitionToAggregationFieldWithValue() {
        // given
        AgentFieldDefinition agentFieldDefinition = Mockito.mock(AgentFieldDefinition.class);
        AgentFieldLabel fieldLabel = Mockito.mock(ConstantFieldLabel.class);
        Mockito.when(fieldLabel.getLabel()).thenReturn("123456789");
        Mockito.when(agentFieldDefinition.getFieldIdentifier()).thenReturn("code");
        Mockito.when(agentFieldDefinition.getFieldLabel()).thenReturn(fieldLabel);
        Field codeField = Mockito.mock(Field.class);
        Mockito.when(supplementalInformationFormer.getField("code")).thenReturn(codeField);
        // when
        Field result = objectUnderTest.toField(agentFieldDefinition);
        // then
        Assertions.assertThat(result).isEqualTo(codeField);
        Mockito.verify(codeField).setValue("123456789");
    }
}
