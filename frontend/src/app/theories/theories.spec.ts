import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Theories } from './theories';

describe('Theories', () => {
  let component: Theories;
  let fixture: ComponentFixture<Theories>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Theories]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Theories);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
