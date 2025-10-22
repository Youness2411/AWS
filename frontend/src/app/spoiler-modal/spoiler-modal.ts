import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-spoiler-modal',
  standalone: true,
  imports: [CommonModule],
  template: `
  <div class="backdrop" *ngIf="visible">
    <div class="panel" role="dialog" aria-modal="true">
      <div class="header">
        <div class="title">⚠️ Spoiler warning</div>
      </div>
      <div class="body">
        <p>This website discusses the latest One Piece theories and may contain spoilers.</p>
        <p>Do you wish to continue?</p>
      </div>
      <div class="actions">
        <button class="leave" (click)="onLeave()">Leave</button>
        <button class="enter" (click)="onEnter()">Enter</button>
      </div>
    </div>
  </div>
  `,
  styleUrls: ['./spoiler-modal.css']
})
export class SpoilerModal {
  @Input() visible = false;
  @Output() enter = new EventEmitter<void>();
  @Output() leave = new EventEmitter<void>();

  onEnter(){ this.enter.emit(); }
  onLeave(){ this.leave.emit(); }
}


