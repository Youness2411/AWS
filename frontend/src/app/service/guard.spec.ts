import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';

import { Guard } from './guard';

describe('Guard', () => {
  let service: Guard;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });
    service = TestBed.inject(Guard);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
