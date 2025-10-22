import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { trigger, state, style, transition, animate } from '@angular/animations';
import { ToastService, Toast } from '../service/toast.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  animations: [
    trigger('slideInOut', [
      transition(':enter', [
        style({ transform: 'translateX(100%)', opacity: 0 }),
        animate('300ms ease-out', style({ transform: 'translateX(0)', opacity: 1 }))
      ]),
      transition(':leave', [
        animate('300ms ease-in', style({ transform: 'translateX(100%)', opacity: 0 }))
      ])
    ])
  ],
  template: `
    <div class="toast-container">
      <div 
        *ngFor="let toast of toasts; trackBy: trackByToastId"
        class="toast"
        [ngClass]="'toast-' + toast.type"
        [@slideInOut]
      >
        <div class="toast-icon">
          <i [ngClass]="getIconClass(toast.type)"></i>
        </div>
        <div class="toast-content">
          <div class="toast-title">{{ toast.title }}</div>
          <div class="toast-message">{{ toast.message }}</div>
        </div>
        <button 
          class="toast-close" 
          (click)="removeToast(toast.id)"
          aria-label="Fermer la notification"
        >
          <i class="fas fa-times"></i>
        </button>
        <div class="toast-progress" [style.animation-duration]="getProgressDuration(toast) + 'ms'"></div>
      </div>
    </div>
  `,
  styleUrls: ['./toast.component.css']
})
export class ToastComponent implements OnInit, OnDestroy {
  toasts: Toast[] = [];
  private subscription: Subscription = new Subscription();

  constructor(private toastService: ToastService) {}

  ngOnInit(): void {
    this.subscription = this.toastService.toasts$.subscribe(
      toasts => this.toasts = toasts
    );
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  trackByToastId(index: number, toast: Toast): string {
    return toast.id;
  }

  removeToast(id: string): void {
    this.toastService.removeToast(id);
  }

  getIconClass(type: string): string {
    const iconMap: { [key: string]: string } = {
      'success': 'fas fa-check-circle',
      'error': 'fas fa-exclamation-circle',
      'warning': 'fas fa-exclamation-triangle',
      'info': 'fas fa-info-circle'
    };
    return iconMap[type] || 'fas fa-info-circle';
  }

  getProgressDuration(toast: Toast): number {
    return toast.duration || 5000;
  }
}
