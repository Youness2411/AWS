import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { Api } from './service/api';
import { ThemeService } from './service/theme.service';
import { ChartModule } from 'primeng/chart';
import { ToastComponent } from './toast/toast.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, CommonModule, FormsModule, ChartModule, ToastComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App{

  protected readonly title = signal('One Piece Theory Evaluator');

  constructor(
    private apiService:Api, 
    public router:Router, 
    private cdr:ChangeDetectorRef,
    public themeService: ThemeService
  ){ }
  
  message: string = "Loading..."
  searchText: string = ''
  
  isAuth():boolean{
    return this.apiService.isAuthenticated();
  }

  isAdmin():boolean{
    return this.apiService.isAdmin();
  }
  isModerator():boolean{
    return this.apiService.isModerator();
  }

  logout():void{
    this.apiService.logout();
    this.router.navigate(["/login"]);
    this.cdr.detectChanges();
  }

  // Hamburger menu state
  menuOpen = false;
  toggleMenu(){ this.menuOpen = !this.menuOpen; }
  closeMenu(){ this.menuOpen = false; }

  // Theme methods
  toggleTheme() {
    this.themeService.toggleTheme();
  }

  // Get the appropriate logo based on current theme
  getLogoPath(): string {
    const isDark = this.themeService.currentTheme() === 'dark';
    return isDark ? 'site_logo_dark.png' : 'site_logo.png';
  }
}
