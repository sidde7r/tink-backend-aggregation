import { TestBed } from '@angular/core/testing';

import { AgentCtService } from './agent-ct.service-mock';

describe('AgentCtService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: AgentCtService = TestBed.get(AgentCtService);
    expect(service).toBeTruthy();
  });
});
