import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AdminPlaceholderComponent } from './admin-placeholder.component';

describe('AdminPlaceholderComponent', () => {
  let fixture: ComponentFixture<AdminPlaceholderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminPlaceholderComponent],
      providers: [provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminPlaceholderComponent);
  });

  it('should create and show admin title', () => {
    fixture.detectChanges();
    const element: HTMLElement = fixture.nativeElement;

    expect(fixture.componentInstance).toBeTruthy();
    expect(element.textContent).toContain('Ruta protegida (demo RBAC)');
  });
});
