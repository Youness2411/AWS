import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Api } from './api';

export interface BookmarkResponse {
  status: number;
  message: string;
  theories?: any[];
  timestamp: string;
}

@Injectable({
  providedIn: 'root'
})
export class BookmarkService {
  // Cache pour les bookmarks de l'utilisateur
  private bookmarkedTheoryIds = new BehaviorSubject<Set<number>>(new Set());
  public bookmarkedTheoryIds$ = this.bookmarkedTheoryIds.asObservable();

  constructor(private apiService: Api) {
    this.loadUserBookmarks();
  }

  /**
   * Ajoute une théorie aux favoris
   */
  addBookmark(theoryId: number): Observable<BookmarkResponse> {
    return this.apiService.addBookmark(theoryId)
      .pipe(
        tap(() => {
          // Mettre à jour le cache local
          const currentIds = this.bookmarkedTheoryIds.value;
          currentIds.add(theoryId);
          this.bookmarkedTheoryIds.next(currentIds);
        })
      );
  }

  /**
   * Retire une théorie des favoris
   */
  removeBookmark(theoryId: number): Observable<BookmarkResponse> {
    return this.apiService.removeBookmark(theoryId)
      .pipe(
        tap(() => {
          // Mettre à jour le cache local
          const currentIds = this.bookmarkedTheoryIds.value;
          currentIds.delete(theoryId);
          this.bookmarkedTheoryIds.next(currentIds);
        })
      );
  }

  /**
   * Toggle bookmark (ajouter ou retirer)
   */
  toggleBookmark(theoryId: number): Observable<BookmarkResponse> {
    const isBookmarked = this.bookmarkedTheoryIds.value.has(theoryId);
    return isBookmarked ? this.removeBookmark(theoryId) : this.addBookmark(theoryId);
  }

  /**
   * Récupère tous les favoris de l'utilisateur
   */
  getMyBookmarks(): Observable<BookmarkResponse> {
    return this.apiService.getMyBookmarks()
      .pipe(
        tap(response => {
          if (response.theories) {
            const ids = new Set(response.theories.map((t: any) => t.id));
            this.bookmarkedTheoryIds.next(ids);
          }
        })
      );
  }

  /**
   * Vérifie si une théorie est dans les favoris
   */
  isBookmarked(theoryId: number): Observable<BookmarkResponse> {
    return this.apiService.isBookmarked(theoryId);
  }

  /**
   * Vérifie localement si une théorie est bookmarkée (synchrone)
   */
  isBookmarkedLocally(theoryId: number): boolean {
    return this.bookmarkedTheoryIds.value.has(theoryId);
  }


  /**
   * Charge les bookmarks de l'utilisateur au démarrage
   */
  private loadUserBookmarks(): void {
    // Charger uniquement si l'utilisateur est connecté
    if (this.apiService.isAuthenticated()) {
      this.getMyBookmarks().subscribe({
        error: (err) => console.error('Error loading bookmarks:', err)
      });
    }
  }

  /**
   * Réinitialise le cache (utile lors de la déconnexion)
   */
  clearCache(): void {
    this.bookmarkedTheoryIds.next(new Set());
  }
}

