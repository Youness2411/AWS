import { Injectable } from '@angular/core';
import { Api } from './api';
import { ActivatedRouteSnapshot, CanActivate, GuardResult, MaybeAsync, Router, RouterStateSnapshot } from '@angular/router';


@Injectable({
  providedIn: 'root'
})
export class Guard implements CanActivate {
  constructor(private apiService: Api, private router: Router) { }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    const requiresAdmin = route.data['requiresAdmin'] || false;
    const requiresModerator = route.data['requiresModerator'] || false;

    if (requiresAdmin) {
      if (this.apiService.isAdmin()) {
        return true;
      }
      else{
        this.router.navigate(['/login'], {
          queryParams:{returnUrl: state.url}
        });
        return false;
      }
    }else if (requiresModerator){
      if (this.apiService.isModerator()){
        return true;
      }else{
        this.router.navigate(['/login'], { queryParams:{returnUrl: state.url} });
        return false;
      }
    }else{
      if (this.apiService.isAuthenticated()) {
        return true;
      }
      else{
        this.router.navigate(['/login'], {
          queryParams:{returnUrl: state.url}
        });
        return false;
      }
    }
  }
}