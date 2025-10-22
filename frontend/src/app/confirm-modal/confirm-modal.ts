import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-confirm-modal',
  standalone: true,
  imports: [CommonModule],
  template: `
  <div class="modal-backdrop" *ngIf="visible">
    <div class="modal" role="dialog" aria-modal="true">
      <div class="modal-header">
        <div class="modal-title">{{ title || 'Confirm' }}</div>
        <button class="close" (click)="onCancel()">✕</button>
      </div>
      <div class="modal-body">
        <p>{{ message || 'Are you sure?' }}</p>
      </div>
      <div class="modal-actions">
        <button class="secondary" (click)="onCancel()">Cancel</button>
        <button class="danger" (click)="onConfirm()">Confirm</button>
      </div>
    </div>
  </div>
  `,
  styleUrls: ['./confirm-modal.css']
})
export class ConfirmModal {
  @Input() visible = false;
  @Input() title: string = 'Confirm';
  @Input() message: string = 'Are you sure?';
  @Output() confirm = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();

  onConfirm(){ this.confirm.emit(); }
  onCancel(){ this.cancel.emit(); }
}


