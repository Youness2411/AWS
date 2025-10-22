import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SkeletonComponent } from '../skeleton/skeleton.component';

@Component({
  selector: 'app-theory-details-skeleton',
  standalone: true,
  imports: [CommonModule, SkeletonComponent],
  template: `
    <div class="theory-details-skeleton">
      <!-- Header section -->
      <div class="header">
        <div class="title-section">
          <app-skeleton type="text" width="100%" height="32px" style="margin-bottom: 8px;"></app-skeleton>
          <app-skeleton type="text" width="60%" height="20px" style="margin-bottom: 16px;"></app-skeleton>
          <div class="meta-info">
            <app-skeleton type="circle" width="40px" height="40px"></app-skeleton>
            <div class="author-info">
              <app-skeleton type="text" width="120px" height="16px"></app-skeleton>
              <app-skeleton type="text" width="80px" height="14px" style="margin-top: 4px;"></app-skeleton>
            </div>
            <div class="scores">
              <app-skeleton type="text" width="60px" height="18px"></app-skeleton>
              <app-skeleton type="text" width="60px" height="18px"></app-skeleton>
            </div>
          </div>
        </div>
      </div>

      <!-- Content section -->
      <div class="content">
        <div class="content-header">
          <app-skeleton type="text" width="100%" height="24px" style="margin-bottom: 12px;"></app-skeleton>
          <app-skeleton type="text" width="90%" height="20px" style="margin-bottom: 8px;"></app-skeleton>
          <app-skeleton type="text" width="95%" height="20px" style="margin-bottom: 8px;"></app-skeleton>
          <app-skeleton type="text" width="85%" height="20px" style="margin-bottom: 8px;"></app-skeleton>
          <app-skeleton type="text" width="70%" height="20px" style="margin-bottom: 16px;"></app-skeleton>
        </div>
        
        <!-- Content paragraphs -->
        <div class="content-body">
          <app-skeleton type="text" width="100%" height="16px" style="margin-bottom: 8px;"></app-skeleton>
          <app-skeleton type="text" width="100%" height="16px" style="margin-bottom: 8px;"></app-skeleton>
          <app-skeleton type="text" width="100%" height="16px" style="margin-bottom: 8px;"></app-skeleton>
          <app-skeleton type="text" width="95%" height="16px" style="margin-bottom: 8px;"></app-skeleton>
          <app-skeleton type="text" width="100%" height="16px" style="margin-bottom: 8px;"></app-skeleton>
          <app-skeleton type="text" width="90%" height="16px" style="margin-bottom: 8px;"></app-skeleton>
          <app-skeleton type="text" width="100%" height="16px" style="margin-bottom: 8px;"></app-skeleton>
          <app-skeleton type="text" width="85%" height="16px" style="margin-bottom: 8px;"></app-skeleton>
          <app-skeleton type="text" width="100%" height="16px" style="margin-bottom: 8px;"></app-skeleton>
          <app-skeleton type="text" width="75%" height="16px" style="margin-bottom: 16px;"></app-skeleton>
        </div>
      </div>

      <!-- Actions section -->
      <div class="actions">
        <app-skeleton type="rect" width="120px" height="40px" borderRadius="8px"></app-skeleton>
        <app-skeleton type="rect" width="120px" height="40px" borderRadius="8px"></app-skeleton>
      </div>

      <!-- Comments section -->
      <div class="comments-section">
        <app-skeleton type="text" width="150px" height="24px" style="margin-bottom: 16px;"></app-skeleton>
        <div class="comment-skeletons">
          <div class="comment-skeleton" *ngFor="let i of [1,2,3]">
            <app-skeleton type="circle" width="32px" height="32px"></app-skeleton>
            <div class="comment-content">
              <app-skeleton type="text" width="80%" height="16px" style="margin-bottom: 4px;"></app-skeleton>
              <app-skeleton type="text" width="60%" height="14px"></app-skeleton>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styleUrls: ['./theory-details-skeleton.component.css']
})
export class TheoryDetailsSkeletonComponent {
}
