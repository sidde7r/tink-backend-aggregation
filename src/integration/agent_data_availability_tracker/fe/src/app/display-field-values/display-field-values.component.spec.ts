import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DisplayFieldValuesComponent } from './display-field-values.component';

describe('DisplayFieldValuesComponent', () => {
  let component: DisplayFieldValuesComponent;
  let fixture: ComponentFixture<DisplayFieldValuesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DisplayFieldValuesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DisplayFieldValuesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
