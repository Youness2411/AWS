import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TheoryForm } from './theory-form';

describe('TheoryForm', () => {
  let component: TheoryForm;
  let fixture: ComponentFixture<TheoryForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TheoryForm]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TheoryForm);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
