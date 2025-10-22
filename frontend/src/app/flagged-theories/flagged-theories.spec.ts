import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FlaggedTheories } from './flagged-theories';

describe('FlaggedTheories', () => {
  let component: FlaggedTheories;
  let fixture: ComponentFixture<FlaggedTheories>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FlaggedTheories]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FlaggedTheories);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
