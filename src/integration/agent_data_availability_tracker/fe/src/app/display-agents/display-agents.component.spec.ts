import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DisplayAgentsComponent } from './display-agents.component';

describe('DisplayAgentsComponent', () => {
  let component: DisplayAgentsComponent;
  let fixture: ComponentFixture<DisplayAgentsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DisplayAgentsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DisplayAgentsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
