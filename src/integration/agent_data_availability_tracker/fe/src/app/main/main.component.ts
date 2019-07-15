import { Component, OnInit } from '@angular/core';
import { AgentCtService } from '../agent-ct.service-mock';
import { AgentFieldStats } from '../model/agent-field-stats';

@Component({
  selector: 'app-main',
  templateUrl: './main.component.html',
  styleUrls: ['./main.component.scss']
})
export class MainComponent implements OnInit {
  public agent: string = null;
  public field: string = null;
  public validAgentSelected = false;
  public validFieldSelected = false;
  public agentFieldsStatsToDisplay: AgentFieldStats[] = [];

  constructor(private agentCtService: AgentCtService) { }

  ngOnInit() { }

  updateAndValidateAgent(newAgent: string) {
    this.agentFieldsStatsToDisplay = [];
    this.validFieldSelected = false;
    this.agent = newAgent;
    this.agentCtService.isValidAgent(this.agent).subscribe(
      valid => {
        this.validAgentSelected = valid;
      }
    );
  }

  updateAndValidateField(newField: string) {
    this.agentFieldsStatsToDisplay = [];
    this.validFieldSelected = false;
    this.field = newField;
    this.agentCtService.isValidField(this.agent, this.field).subscribe(
      valid => {
        this.validFieldSelected = valid;
        if (valid) {
          this.agentCtService.getAgentFieldStats(this.agent, this.field).subscribe(
            agentFieldStats => {
              this.agentFieldsStatsToDisplay.push(agentFieldStats);
            }
          );
        }
      }
    );
  }

  displayAllFields() {
    if (this.validAgentSelected) {
      this.validFieldSelected = true;
      this.agentCtService.getAgentFieldsStats(this.agent).subscribe(
        agentFieldsStats => this.agentFieldsStatsToDisplay = [...agentFieldsStats]
      );
    }
  }
}
