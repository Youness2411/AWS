import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SkeletonComponent } from '../skeleton/skeleton.component';

@Component({
  selector: 'app-theory-card-skeleton',
  standalone: true,
  imports: [CommonModule, SkeletonComponent],
  template: `
    <div class="card-skeleton">
      <div class="header">
        <app-skeleton type="circle" width="63px" height="63px"></app-skeleton>
        <div class="meta">
          <app-skeleton type="text" width="80%" height="16px"></app-skeleton>
          <app-skeleton type="text" width="60%" height="12px" style="margin-top: 4px;"></app-skeleton>
        </div>
        <div class="scores">
          <app-skeleton type="text" width="40px" height="14px"></app-skeleton>
          <app-skeleton type="text" width="40px" height="14px"></app-skeleton>
        </div>
      </div>
      
      <div class="choices">
        <app-skeleton type="rect" width="100%" height="40px" borderRadius="10px"></app-skeleton>
        <app-skeleton type="rect" width="100%" height="40px" borderRadius="10px"></app-skeleton>
      </div>
      
      <div class="footer">
        <app-skeleton type="text" width="80px" height="12px"></app-skeleton>
        <app-skeleton type="text" width="100px" height="12px"></app-skeleton>
      </div>
    </div>
  `,
  styleUrls: ['./theory-card-skeleton.component.css']
})
export class TheoryCardSkeletonComponent {
}
