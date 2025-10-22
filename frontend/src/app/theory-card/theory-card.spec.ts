import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TheoryCard } from './theory-card';

describe('TheoryCard', () => {
  let component: TheoryCard;
  let fixture: ComponentFixture<TheoryCard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TheoryCard]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TheoryCard);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
