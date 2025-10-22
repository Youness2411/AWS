import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-login-required-modal',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './login-required-modal.html',
  styleUrl: './login-required-modal.css'
})
export class LoginRequiredModal {
  @Input() visible: boolean = false;
  @Output() closed = new EventEmitter<void>();

  close(){
    this.closed.emit();
  }
}


