import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-drop-down-typeahead',
  templateUrl: './drop-down-typeahead.component.html',
  styleUrls: ['./drop-down-typeahead.component.scss']
})
export class DropDownTypeaheadComponent implements OnInit {

  @Input() search: (text: Observable<string>) => Observable<any[]>;
  @Input() inputId: string;
  @Input() searching: boolean;
  @Input() searchFailed: boolean;
  @Input() placeholder: string;
  @Input() modelValue: string;
  @Output() modelValueChange = new EventEmitter<string>();

  constructor() { }

  ngOnInit() { }

  public onFocus(e: Event): void {
    e.stopPropagation();
    setTimeout(() => {
      const inputEvent: Event = new Event('input');
      e.target.dispatchEvent(inputEvent);
    }, 0);
    this.modelValue = null;
    this.modelValueChange.emit(null);
  }

  public modelChange(newValue: string) {
    this.modelValue = newValue;
    this.modelValueChange.emit(newValue);
  }
}
