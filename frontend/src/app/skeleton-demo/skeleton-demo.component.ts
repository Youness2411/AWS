import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SkeletonComponent } from '../skeleton/skeleton.component';
import { TheoryCardSkeletonComponent } from '../theory-card-skeleton/theory-card-skeleton.component';
import { TheoryDetailsSkeletonComponent } from '../theory-details-skeleton/theory-details-skeleton.component';
import { ProfileSkeletonComponent } from '../profile-skeleton/profile-skeleton.component';
import { AuthSkeletonComponent } from '../auth-skeleton/auth-skeleton.component';

@Component({
  selector: 'app-skeleton-demo',
  standalone: true,
  imports: [
    CommonModule, 
    SkeletonComponent, 
    TheoryCardSkeletonComponent, 
    TheoryDetailsSkeletonComponent,
    ProfileSkeletonComponent,
    AuthSkeletonComponent
  ],
  template: `
    <div class="skeleton-demo">
      <h1>Skeleton Loading Components Demo</h1>
      
      <section class="demo-section">
        <h2>Basic Skeleton Components</h2>
        <div class="demo-grid">
          <div class="demo-item">
            <h3>Text Skeleton</h3>
            <app-skeleton type="text" width="200px" height="16px"></app-skeleton>
            <app-skeleton type="text" width="150px" height="16px" style="margin-top: 8px;"></app-skeleton>
            <app-skeleton type="text" width="180px" height="16px" style="margin-top: 8px;"></app-skeleton>
          </div>
          
          <div class="demo-item">
            <h3>Circle Skeleton</h3>
            <app-skeleton type="circle" width="60px" height="60px"></app-skeleton>
          </div>
          
          <div class="demo-item">
            <h3>Rectangle Skeleton</h3>
            <app-skeleton type="rect" width="200px" height="100px" borderRadius="8px"></app-skeleton>
          </div>
          
          <div class="demo-item">
            <h3>Card Skeleton</h3>
            <app-skeleton type="card" width="300px" height="200px"></app-skeleton>
          </div>
        </div>
      </section>

      <section class="demo-section">
        <h2>Theory Card Skeleton</h2>
        <div class="cards-demo">
          <app-theory-card-skeleton></app-theory-card-skeleton>
          <app-theory-card-skeleton></app-theory-card-skeleton>
          <app-theory-card-skeleton></app-theory-card-skeleton>
        </div>
      </section>

      <section class="demo-section">
        <h2>Theory Details Skeleton</h2>
        <app-theory-details-skeleton></app-theory-details-skeleton>
      </section>

      <section class="demo-section">
        <h2>Profile Skeleton</h2>
        <app-profile-skeleton></app-profile-skeleton>
      </section>

      <section class="demo-section">
        <h2>Auth Skeleton</h2>
        <div class="auth-demo">
          <div class="auth-demo-item">
            <h3>Login Form</h3>
            <app-auth-skeleton></app-auth-skeleton>
          </div>
          <div class="auth-demo-item">
            <h3>Register Form</h3>
            <app-auth-skeleton [showPasswordConfirm]="false"></app-auth-skeleton>
          </div>
        </div>
      </section>
    </div>
  `,
  styleUrls: ['./skeleton-demo.component.css']
})
export class SkeletonDemoComponent {
}
