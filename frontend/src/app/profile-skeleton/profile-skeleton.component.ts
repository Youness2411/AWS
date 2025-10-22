import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SkeletonComponent } from '../skeleton/skeleton.component';

@Component({
  selector: 'app-profile-skeleton',
  standalone: true,
  imports: [CommonModule, SkeletonComponent],
  template: `
    <div class="profile-skeleton">
      <!-- Profile header -->
      <div class="profile-header">
        <app-skeleton type="circle" width="120px" height="120px"></app-skeleton>
        <div class="profile-info">
          <app-skeleton type="text" width="200px" height="28px" style="margin-bottom: 8px;"></app-skeleton>
          <app-skeleton type="text" width="150px" height="18px" style="margin-bottom: 16px;"></app-skeleton>
          <div class="stats">
            <div class="stat">
              <app-skeleton type="text" width="60px" height="20px"></app-skeleton>
              <app-skeleton type="text" width="40px" height="14px" style="margin-top: 4px;"></app-skeleton>
            </div>
            <div class="stat">
              <app-skeleton type="text" width="60px" height="20px"></app-skeleton>
              <app-skeleton type="text" width="40px" height="14px" style="margin-top: 4px;"></app-skeleton>
            </div>
            <div class="stat">
              <app-skeleton type="text" width="60px" height="20px"></app-skeleton>
              <app-skeleton type="text" width="40px" height="14px" style="margin-top: 4px;"></app-skeleton>
            </div>
          </div>
        </div>
      </div>

      <!-- Profile content -->
      <div class="profile-content">
        <div class="section">
          <app-skeleton type="text" width="120px" height="24px" style="margin-bottom: 16px;"></app-skeleton>
          <div class="theories-grid">
            <div class="theory-card-skeleton" *ngFor="let i of [1,2,3,4,5,6]">
              <app-skeleton type="rect" width="100%" height="180px" borderRadius="14px"></app-skeleton>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styleUrls: ['./profile-skeleton.component.css']
})
export class ProfileSkeletonComponent {
}
