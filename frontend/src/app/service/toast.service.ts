import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export interface Toast {
  id: string;
  type: 'success' | 'error' | 'warning' | 'info';
  title: string;
  message: string;
  duration?: number;
  timestamp: Date;
}

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  private toastsSubject = new BehaviorSubject<Toast[]>([]);
  public toasts$ = this.toastsSubject.asObservable();

  private defaultDuration = 5000; // 5 secondes par défaut

  constructor() {}

  private generateId(): string {
    return Math.random().toString(36).substr(2, 9);
  }

  private addToast(toast: Omit<Toast, 'id' | 'timestamp'>): void {
    const newToast: Toast = {
      ...toast,
      id: this.generateId(),
      timestamp: new Date()
    };

    const currentToasts = this.toastsSubject.value;
    this.toastsSubject.next([...currentToasts, newToast]);

    // Auto-remove après la durée spécifiée
    const duration = toast.duration || this.defaultDuration;
    setTimeout(() => {
      this.removeToast(newToast.id);
    }, duration);
  }

  success(title: string, message: string, duration?: number): void {
    this.addToast({
      type: 'success',
      title,
      message,
      duration
    });
  }

  error(title: string, message: string, duration?: number): void {
    this.addToast({
      type: 'error',
      title,
      message,
      duration: duration || 7000 // Les erreurs restent plus longtemps
    });
  }

  warning(title: string, message: string, duration?: number): void {
    this.addToast({
      type: 'warning',
      title,
      message,
      duration
    });
  }

  info(title: string, message: string, duration?: number): void {
    this.addToast({
      type: 'info',
      title,
      message,
      duration
    });
  }

  removeToast(id: string): void {
    const currentToasts = this.toastsSubject.value;
    this.toastsSubject.next(currentToasts.filter(toast => toast.id !== id));
  }

  clearAll(): void {
    this.toastsSubject.next([]);
  }

  // Méthodes de convenance pour les actions spécifiques
  theoryCreated(): void {
    this.success(
      'Théorie créée !',
      'Votre théorie a été publiée avec succès et sera évaluée par l\'IA.',
      4000
    );
  }

  theoryCreationFailed(error?: string): void {
    this.error(
      'Erreur de création',
      error || 'Impossible de créer la théorie. Veuillez réessayer.',
      6000
    );
  }

  loginSuccess(): void {
    this.success(
      'Connexion réussie !',
      'Bienvenue sur One Piece Theory Evaluator !',
      3000
    );
  }

  loginFailed(): void {
    this.error(
      'Échec de la connexion',
      'Email ou mot de passe incorrect.',
      5000
    );
  }

  registerSuccess(): void {
    this.success(
      'Compte créé !',
      'Votre compte a été créé avec succès. Vous pouvez maintenant vous connecter.',
      4000
    );
  }

  registerFailed(error?: string): void {
    this.error(
      'Erreur d\'inscription',
      error || 'Impossible de créer le compte. Veuillez réessayer.',
      6000
    );
  }

  commentPosted(): void {
    this.success(
      'Commentaire ajouté !',
      'Votre commentaire a été publié.',
      3000
    );
  }

  commentFailed(): void {
    this.error(
      'Erreur de commentaire',
      'Impossible de publier le commentaire. Veuillez réessayer.',
      5000
    );
  }

  voteSuccess(voteType: 'up' | 'down'): void {
    const action = voteType === 'up' ? 'aimé' : 'disliké';
    this.success(
      'Vote enregistré !',
      `Vous avez ${action} cette théorie.`,
      3000
    );
  }

  voteFailed(): void {
    this.error(
      'Erreur de vote',
      'Impossible d\'enregistrer votre vote. Veuillez réessayer.',
      5000
    );
  }

  profileUpdated(): void {
    this.success(
      'Profil mis à jour !',
      'Vos informations ont été sauvegardées.',
      3000
    );
  }

  profileUpdateFailed(): void {
    this.error(
      'Erreur de mise à jour',
      'Impossible de mettre à jour votre profil. Veuillez réessayer.',
      5000
    );
  }

  theoryDeleted(): void {
    this.success(
      'Théorie supprimée !',
      'Votre théorie a été supprimée avec succès.',
      4000
    );
  }

  theoryDeleteFailed(): void {
    this.error(
      'Erreur de suppression',
      'Impossible de supprimer la théorie. Veuillez réessayer.',
      6000
    );
  }

  theoryUpdated(): void {
    this.success(
      'Théorie mise à jour !',
      'Votre théorie a été modifiée avec succès.',
      4000
    );
  }

  theoryUpdateFailed(): void {
    this.error(
      'Erreur de modification',
      'Impossible de modifier la théorie. Veuillez réessayer.',
      6000
    );
  }

  replyPosted(): void {
    this.success(
      'Réponse ajoutée !',
      'Votre réponse au commentaire a été publiée.',
      3000
    );
  }

  replyFailed(): void {
    this.error(
      'Erreur de réponse',
      'Impossible de publier votre réponse. Veuillez réessayer.',
      5000
    );
  }

  commentDeleted(): void {
    this.info(
      'Commentaire supprimé',
      'Le commentaire a été supprimé.',
      3000
    );
  }

  commentDeleteFailed(): void {
    this.error(
      'Erreur de suppression',
      'Impossible de supprimer le commentaire. Veuillez réessayer.',
      5000
    );
  }

  passwordChanged(): void {
    this.success(
      'Mot de passe modifié !',
      'Votre mot de passe a été changé avec succès.',
      4000
    );
  }

  passwordChangeFailed(): void {
    this.error(
      'Erreur de changement',
      'Impossible de changer le mot de passe. Veuillez vérifier vos informations.',
      6000
    );
  }

  bookmarkAdded(): void {
    this.success(
      '★ Ajouté aux favoris !',
      'Cette théorie a été sauvegardée dans vos favoris.',
      3000
    );
  }

  bookmarkRemoved(): void {
    this.info(
      '☆ Retiré des favoris',
      'Cette théorie a été retirée de vos favoris.',
      3000
    );
  }

  bookmarkFailed(): void {
    this.error(
      'Erreur de favoris',
      'Impossible de modifier vos favoris. Veuillez réessayer.',
      5000
    );
  }
}
