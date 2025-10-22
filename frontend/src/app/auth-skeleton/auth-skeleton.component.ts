import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SkeletonComponent } from '../skeleton/skeleton.component';

@Component({
  selector: 'app-auth-skeleton',
  standalone: true,
  imports: [CommonModule, SkeletonComponent],
  template: `
    <div class="auth-skeleton">
      <div class="auth-card">
        <div class="header">
          <app-skeleton type="text" width="200px" height="32px" style="margin-bottom: 8px;"></app-skeleton>
          <app-skeleton type="text" width="150px" height="18px"></app-skeleton>
        </div>
        
        <div class="form">
          <div class="field">
            <app-skeleton type="text" width="80px" height="16px" style="margin-bottom: 8px;"></app-skeleton>
            <app-skeleton type="rect" width="100%" height="40px" borderRadius="8px"></app-skeleton>
          </div>
          
          <div class="field">
            <app-skeleton type="text" width="80px" height="16px" style="margin-bottom: 8px;"></app-skeleton>
            <app-skeleton type="rect" width="100%" height="40px" borderRadius="8px"></app-skeleton>
          </div>
          
          <div class="field" *ngIf="showPasswordConfirm">
            <app-skeleton type="text" width="120px" height="16px" style="margin-bottom: 8px;"></app-skeleton>
            <app-skeleton type="rect" width="100%" height="40px" borderRadius="8px"></app-skeleton>
          </div>
          
          <div class="actions">
            <app-skeleton type="rect" width="100%" height="44px" borderRadius="8px"></app-skeleton>
          </div>
        </div>
        
        <div class="footer">
          <app-skeleton type="text" width="200px" height="16px"></app-skeleton>
        </div>
      </div>
    </div>
  `,
  styleUrls: ['./auth-skeleton.component.css']
})
export class AuthSkeletonComponent {
  @Input() showPasswordConfirm: boolean = false;
}
