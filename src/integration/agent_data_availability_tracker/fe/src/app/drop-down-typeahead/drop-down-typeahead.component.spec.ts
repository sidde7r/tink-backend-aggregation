import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DropDownTypeaheadComponent } from './drop-down-typeahead.component';

describe('DropDownTypeaheadComponent', () => {
  let component: DropDownTypeaheadComponent;
  let fixture: ComponentFixture<DropDownTypeaheadComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DropDownTypeaheadComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DropDownTypeaheadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
