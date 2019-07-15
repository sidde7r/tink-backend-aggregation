import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { Observable, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, tap, switchMap, catchError, map } from 'rxjs/operators';
import { AgentCtService } from '../agent-ct.service-mock';

@Component({
  selector: 'app-display-agents',
  templateUrl: './display-agents.component.html',
  styleUrls: ['./display-agents.component.scss']
})
export class DisplayAgentsComponent implements OnInit {
  searching = false;
  searchFailed = false;
  inputId = 'agents';
  @Input() agent: string;
  @Output() agentChange = new EventEmitter<string>();

  constructor(private service: AgentCtService) { }

  ngOnInit() { }

  public modelChange(newValue: string) {
    this.agent = newValue;
    this.agentChange.emit(newValue);
  }

  search = (text$: Observable<string>) =>
    text$.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      tap(() => this.searching = true),
      switchMap(term =>
        this.service.getAgents(term).pipe(
          map(agents => agents.map(agent => agent.agent)),
          tap(() => this.searchFailed = false),
          catchError(() => {
            this.searchFailed = true;
            return of([]);
          }))
      ),
      tap(() => this.searching = false)
    )
}
