import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../service/toast.service';

@Component({
  selector: 'app-toast-demo',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toast-demo">
      <h2>Toast Demo</h2>
      <p>Testez les différents types de toasts :</p>
      
      <div class="demo-buttons">
        <button (click)="showSuccess()" class="btn btn-success">Success Toast</button>
        <button (click)="showError()" class="btn btn-error">Error Toast</button>
        <button (click)="showWarning()" class="btn btn-warning">Warning Toast</button>
        <button (click)="showInfo()" class="btn btn-info">Info Toast</button>
      </div>
      
      <div class="demo-buttons">
        <button (click)="showTheoryCreated()" class="btn btn-primary">Theory Created</button>
        <button (click)="showTheoryUpdated()" class="btn btn-primary">Theory Updated</button>
        <button (click)="showTheoryDeleted()" class="btn btn-primary">Theory Deleted</button>
        <button (click)="showLoginSuccess()" class="btn btn-primary">Login Success</button>
        <button (click)="showCommentPosted()" class="btn btn-primary">Comment Posted</button>
        <button (click)="showReplyPosted()" class="btn btn-primary">Reply Posted</button>
        <button (click)="showVoteSuccess()" class="btn btn-primary">Vote Success</button>
      </div>
      
      <div class="demo-buttons">
        <button (click)="clearAll()" class="btn btn-secondary">Clear All Toasts</button>
      </div>
    </div>
  `,
  styles: [`
    .toast-demo {
      padding: 20px;
      max-width: 600px;
      margin: 0 auto;
    }
    
    .demo-buttons {
      display: flex;
      gap: 10px;
      margin: 15px 0;
      flex-wrap: wrap;
    }
    
    .btn {
      padding: 10px 20px;
      border: none;
      border-radius: 5px;
      cursor: pointer;
      font-weight: 500;
      transition: all 0.2s ease;
    }
    
    .btn:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 8px rgba(0,0,0,0.2);
    }
    
    .btn-success { background: #10b981; color: white; }
    .btn-error { background: #ef4444; color: white; }
    .btn-warning { background: #f59e0b; color: white; }
    .btn-info { background: #3b82f6; color: white; }
    .btn-primary { background: #8b5cf6; color: white; }
    .btn-secondary { background: #6b7280; color: white; }
  `]
})
export class ToastDemoComponent {
  constructor(private toastService: ToastService) {}

  showSuccess() {
    this.toastService.success('Succès !', 'Opération réussie avec succès.');
  }

  showError() {
    this.toastService.error('Erreur !', 'Une erreur est survenue lors de l\'opération.');
  }

  showWarning() {
    this.toastService.warning('Attention !', 'Cette action peut avoir des conséquences.');
  }

  showInfo() {
    this.toastService.info('Information', 'Voici une information importante.');
  }

  showTheoryCreated() {
    this.toastService.theoryCreated();
  }

  showTheoryUpdated() {
    this.toastService.theoryUpdated();
  }

  showTheoryDeleted() {
    this.toastService.theoryDeleted();
  }

  showLoginSuccess() {
    this.toastService.loginSuccess();
  }

  showCommentPosted() {
    this.toastService.commentPosted();
  }

  showReplyPosted() {
    this.toastService.replyPosted();
  }

  showVoteSuccess() {
    this.toastService.voteSuccess('up');
  }

  clearAll() {
    this.toastService.clearAll();
  }
}
