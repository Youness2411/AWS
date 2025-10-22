import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Api } from '../service/api';
import { ToastService } from '../service/toast.service';
import { AuthSkeletonComponent } from '../auth-skeleton/auth-skeleton.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CommonModule, RouterLink, AuthSkeletonComponent],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
  constructor(private apiService:Api, private router:Router, private toastService: ToastService){}

  formData: any = {
    email: '',
    password: ''
  };

  message:string | null = null;
  loading: boolean = false;

  async handleSubmit(){
    if( 
      !this.formData.email || 
      !this.formData.password 
    ){
      this.showMessage("All fields are required");
      return;
    }

    try {
      const response: any = await firstValueFrom(
        this.apiService.loginUser(this.formData)
      );
      if (response.status === 200) {
        this.apiService.encryptAndSaveToStorage('token', response.token);
        this.apiService.encryptAndSaveToStorage('role', response.role);
        this.toastService.loginSuccess();
        this.router.navigate(["/"]);
      }
    } catch (error:any) {
      console.log(error)
      this.toastService.loginFailed();
      this.showMessage(error?.error?.message || error?.message || "Unable to Login a user" + error)
      
    }
  }

  showMessage(message:string){
    this.message = message;
    setTimeout(() =>{
      this.message = null
    }, 4000)
  }

}