import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Api } from '../service/api';
import { ToastService } from '../service/toast.service';
import { AuthSkeletonComponent } from '../auth-skeleton/auth-skeleton.component';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-register',
  imports: [FormsModule, CommonModule, RouterLink, AuthSkeletonComponent],
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class Register {
  constructor(private apiService:Api, private router:Router, private toastService: ToastService){}

  formData: any = {
    email: '',
    username: '',
    password: '',
  };
  message:string | null = null;
  loading: boolean = false;

  async handleSubmit(){
    if (
      !this.formData.email ||
      !this.formData.username ||
      !this.formData.password
    ) {
      this.showMessage("All fields are required")
      return;
    }

    try {
      const response: any = await firstValueFrom(
        this.apiService.registerUser(this.formData)
      );
      if (response.status === 200) {
        this.toastService.registerSuccess();
        this.showMessage(response.message)
        this.router.navigate(["/login"]);
      }
    } catch (error: any) {
      console.log(error)
      this.toastService.registerFailed(error?.error?.message || error?.message);
      this.showMessage(error?.error?.message || error?.message || "Unable to register a user" + error)
      
    }
  }

  showMessage(message:string){
    this.message = message;
    setTimeout(() => {
      this.message == null
    }, 4000)
  }
}
