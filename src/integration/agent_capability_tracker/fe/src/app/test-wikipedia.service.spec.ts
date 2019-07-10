import { TestBed } from '@angular/core/testing';

import { TestWikipediaService } from './test-wikipedia.service';

describe('TestWikipediaService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: TestWikipediaService = TestBed.get(TestWikipediaService);
    expect(service).toBeTruthy();
  });
});
