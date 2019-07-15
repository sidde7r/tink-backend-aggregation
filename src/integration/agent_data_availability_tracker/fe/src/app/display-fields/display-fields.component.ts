import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { AgentCtService } from '../agent-ct.service-mock';
import { Observable, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, tap, switchMap, map, catchError, flatMap } from 'rxjs/operators';

@Component({
  selector: 'app-display-fields',
  templateUrl: './display-fields.component.html',
  styleUrls: ['./display-fields.component.scss']
})
export class DisplayFieldsComponent implements OnInit {
  searching = false;
  searchFailed = false;
  inputId = 'fields';
  @Input() agent: string;
  @Input() field: string;
  @Output() fieldChange = new EventEmitter<string>();

  constructor(private service: AgentCtService) { }

  ngOnInit() {
    this.field = null;
  }

  public modelChange(newValue: string) {
    this.field = newValue;
    this.fieldChange.emit(newValue);
  }

  search = (text$: Observable<string>) =>
    text$.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      tap(() => this.searching = true),
      switchMap(term =>
        this.service.getAgentFields(this.agent, term).pipe(
          tap(() => this.searchFailed = false),
          catchError(() => {
            this.searchFailed = true;
            return of([]);
          }))
      ),
      tap(() => this.searching = false)
    )
}
