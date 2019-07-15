import { Component, OnInit, Input, ViewEncapsulation } from '@angular/core';
import { AgentCtService } from '../agent-ct.service-mock';
import { FieldValue } from '../model/field-value';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-display-field-values',
  templateUrl: './display-field-values.component.html',
  encapsulation: ViewEncapsulation.None,
  styleUrls: ['./display-field-values.component.scss']
})
export class DisplayFieldValuesComponent implements OnInit {
  @Input() agent: string;
  @Input() field: string;
  public fieldValues: FieldValue[];

  constructor(
    private agentCtService: AgentCtService,
    private modalService: NgbModal
  ) { }

  ngOnInit() {
    this.agentCtService.getAgentFieldValues(this.agent, this.field).subscribe(
      fieldValues => this.fieldValues = fieldValues
    );
  }

  openVerticallyCentered(content) {
    this.modalService.open(content, { centered: true });
  }
}
