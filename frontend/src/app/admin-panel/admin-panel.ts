import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { Api } from '../service/api';

@Component({
  selector: 'app-admin-panel',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
  <div class="admin">
    <h2>Admin panel</h2>
    <div *ngIf="loading" class="status">Loading…</div>
    <div *ngIf="error" class="status error">{{error}}</div>
    <div *ngIf="success" class="status success">{{success}}</div>
    <table *ngIf="!loading && !error" class="users">
      <thead>
        <tr><th>ID</th><th>Username</th><th>Email</th><th>Role</th><th>Actions</th></tr>
      </thead>
      <tbody>
        <tr *ngFor="let u of users">
          <td>{{u.id}}</td>
          <td>{{u.username}}</td>
          <td>{{u.email}}</td>
          <td>
            <select [(ngModel)]="u.role">
              <option value="USER">USER</option>
              <option value="MODERATOR">MODERATOR</option>
              <option value="ADMIN">ADMIN</option>
            </select>
          </td>
          <td>
            <button (click)="saveRole(u)">Save</button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
  `,
  styles: [`
  .admin{ padding:16px }
  .status{ color:#444 }
  .status.error{ color:#b00020 }
  .status.success{ color:#1ea95e}

  table{ width:100%; border-collapse:collapse }
  th,td{ border:1px solid #eee; padding:8px; text-align:left }
  `]
})
export class AdminPanel implements OnInit{
  constructor(private api:Api){}
  users:any[] = [];
  loading = true;
  success:string|null = null;
  error:string|null = null;
  async ngOnInit(){
    

    try{
      const res:any = await firstValueFrom(this.api.getAllUsers());
      this.users = res?.users || [];
    }catch(e:any){ 
      const errorMessage = e?.error?.message || 'Failed to load users'; 
      this.updateErrorMessage(errorMessage);
    }
    finally{ this.loading = false; }
  }
  async saveRole(u:any){
    try{ 
      const res = await firstValueFrom(this.api.updateUserRole(String(u.id), u.role)); 
      const succesUpdate = res?.status === 200 ? true : false;
      if (succesUpdate){
        this.updateSuccessMessage("User role updated successfully");
      }
      else {
        this.updateErrorMessage("Error when updating user role"); 
      }
    }
    catch(e:any){ 
      const errorMessage = e?.error?.message || 'Failed to load users'; 
      this.updateErrorMessage(errorMessage); 
    }
  }

  private updateSuccessMessage(message: string){
    this.success = message;
    setTimeout(() => { this.success=null; }, 5000);
  }

  private updateErrorMessage(message: string){
    this.error = message;
    setTimeout(() => { this.error=null; }, 5000);
  }
}


