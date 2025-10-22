import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { Api } from '../service/api';
import { ToastService } from '../service/toast.service';
import { TheoryCard } from '../theory-card/theory-card';
import { ProfileSkeletonComponent } from '../profile-skeleton/profile-skeleton.component';
import { BookmarkService } from '../service/bookmark.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, TheoryCard, ProfileSkeletonComponent],
  templateUrl: './profile.html',
  styleUrl: './profile.css'
})
export class Profile implements OnInit {
  constructor(
    private apiService: Api, 
    private toastService: ToastService,
    private bookmarkService: BookmarkService
  ){}
  user: any = null;
  userId: any;
  message: string = "";
  error: string | null = null;
  submitting: boolean = false;
  loading: boolean = true;
  userTheories: any[] = [];
  userVotes: any[] = [];
  bookmarkedTheories: any[] = [];

  form = {
    username: '',
    email: ''
  };

  passwordForm = {
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  };

  imageFile: File | null = null;
  imagePreview: string | null = null;
  async ngOnInit() {
    this.loading = true;
    try{
      const me:any = await firstValueFrom(this.apiService.getLoggedInUserInfo());
      this.userId = '' + (me?.user?.id ?? me?.id);
      this.form.username = me?.username || me?.name || '';
      this.form.email = me?.email || '';
      this.imagePreview = me?.imageUrl || null;
      const votes:any = await firstValueFrom(this.apiService.getAllUserVotes(this.userId));
      this.userVotes = votes.votes || [];
      const theories:any = await firstValueFrom(this.apiService.getTheoriesByUserId(this.userId))
      this.userTheories = theories.theories || [];
      // Load bookmarked theories
      const bookmarks:any = await firstValueFrom(this.bookmarkService.getMyBookmarks());
      this.bookmarkedTheories = bookmarks.theories || [];
    }catch(error){
      this.error = 'Failed to load profile data';
    }finally{
      this.loading = false;
    }
  }

  // fetchUserInfo():void{
  //   this.apiService.getLoggedInUserInfo().subscribe({
  //     next:(res)=>{
  //       this.user = res;
  //       // backend may return user or {user: {...}}
  //       const u:any = (res?.user) ? res.user : res;
  //       this.userId = u?.id;
  //       this.form.username = u?.username || u?.name || '';
  //       this.form.email = u?.email || '';
  //       this.imagePreview = u?.imageUrl || null;
  //     },
  //     error: (error) => {
  //       this.showMessage(
  //         error?.error?.message ||
  //           error?.message ||
  //           'Unable to Get Profile Info' + error
  //       );
  //     }
  //   })
  // }

  // async fetchUserTheoriesAndVotes(){
  //   console.log(this.userId);
  //   const responseTheories = await firstValueFrom(this.apiService.getTheoriesByUserId(this.userId))
  //   if(responseTheories?.status === 200){
  //     this.userTheories = responseTheories.theories || [];
  //   }
  //   const responseVotes = await firstValueFrom(this.apiService.getAllUserVotes(this.userId));
  //   if(responseVotes?.status === 200){
  //     this.userVotes = responseVotes.votes || [];
  //   }
  // }

  onImageChange(event:any){
    const file = event?.target?.files?.[0];
    this.imageFile = file || null;
    if(this.imageFile){
      const reader = new FileReader();
      reader.onload = () => this.imagePreview = String(reader.result);
      reader.readAsDataURL(this.imageFile);
    }
  }

  async save(){
    this.error = null; this.message = '';
    this.submitting = true;
    try{
      const formData = new FormData();
      formData.append('username', this.form.username.trim());
      formData.append('email', this.form.email.trim());
      if(this.imageFile){ formData.append('image', this.imageFile); }
      const res:any = await firstValueFrom(this.apiService.updateUser(this.userId, formData));
      this.toastService.profileUpdated();
      this.message = res?.message || 'Profile updated';
      await firstValueFrom(this.apiService.getLoggedInUserInfo());
    }catch(error:any){
      this.toastService.profileUpdateFailed();
      this.error = error?.error?.message || 'Failed to update profile';
    }finally{
      this.submitting = false;
    }
  }

  async changePassword(){
    this.error = null; this.message = '';
    this.submitting = true;
    try{
      // Validate passwords match
      if(this.passwordForm.newPassword !== this.passwordForm.confirmPassword){
        this.error = 'New password and confirmation do not match';
        return;
      }

      // Validate password length
      if(this.passwordForm.newPassword.length < 6){
        this.error = 'New password must be at least 6 characters long';
        return;
      }

      const res:any = await firstValueFrom(this.apiService.changePassword(
        this.passwordForm.currentPassword,
        this.passwordForm.newPassword,
        this.passwordForm.confirmPassword
      ));
      
      this.message = res?.message || 'Password changed successfully';
      this.passwordForm = { currentPassword: '', newPassword: '', confirmPassword: '' };
      this.toastService.passwordChanged();
    }catch(error:any){
      this.toastService.passwordChangeFailed();
      this.error = error?.error?.message || 'Failed to change password';
    }finally{
      this.submitting = false;
    }
  }



  //SHOW ERROR
  showMessage(message: string) {
    this.message = message;
    setTimeout(() => {
      this.message = '';
    }, 4000);
  }
}