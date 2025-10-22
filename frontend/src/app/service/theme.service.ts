import { Injectable, signal, effect } from '@angular/core';

export type Theme = 'light' | 'dark';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private readonly THEME_KEY = 'theme-preference';
  
  // Signal pour le thème actuel (light ou dark)
  public currentTheme = signal<Theme>('light');
  
  // Media query pour détecter les préférences système
  private darkModeQuery = window.matchMedia('(prefers-color-scheme: dark)');

  constructor() {
    // Charger la préférence sauvegardée ou utiliser la préférence système
    this.loadThemePreference();
    
    // Effect pour appliquer le thème quand il change
    effect(() => {
      this.applyTheme();
    });
  }

  private loadThemePreference(): void {
    const saved = localStorage.getItem(this.THEME_KEY) as Theme;
    if (saved && ['light', 'dark'].includes(saved)) {
      // Utiliser la préférence sauvegardée
      this.currentTheme.set(saved);
    } else {
      // Par défaut, utiliser la préférence système
      this.currentTheme.set(this.darkModeQuery.matches ? 'dark' : 'light');
    }
  }

  private applyTheme(): void {
    const theme = this.currentTheme();
    document.documentElement.setAttribute('data-theme', theme);
    
    // Sauvegarder la préférence
    localStorage.setItem(this.THEME_KEY, this.currentTheme());
  }

  public setTheme(theme: Theme): void {
    this.currentTheme.set(theme);
  }

  public toggleTheme(): void {
    const current = this.currentTheme();
    // Alterner simplement entre light et dark
    this.setTheme(current === 'light' ? 'dark' : 'light');
  }

  public getThemeIcon(): string {
    return this.currentTheme() === 'dark' ? '🌙' : '☀️';
  }

  public getThemeLabel(): string {
    return this.currentTheme() === 'dark' ? 'Dark' : 'Light';
  }
}
