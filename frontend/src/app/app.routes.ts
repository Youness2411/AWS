import { Routes } from '@angular/router';
import { Login } from './login/login';
import { Register } from './register/register';
import { Theories } from './theories/theories';
import { TheoryDetails } from './theory-details/theory-details';
import { TheoryForm } from './theory-form/theory-form';
import { Profile } from './profile/profile';
import { Guard } from './service/guard';
import { AdminPanel } from './admin-panel/admin-panel';
import { FlaggedTheories } from './flagged-theories/flagged-theories';

export const routes: Routes = [
    {path: "login", component: Login},
    {path: "register", component: Register},
    
    {path: "", component: Theories},
    {path: "theories/:id", component: TheoryDetails},
    {path: "create-theory", component: TheoryForm, canActivate:[Guard]},
    {path: "admin", component: AdminPanel, canActivate:[Guard], data:{requiresAdmin:true}},
    {path: "flagged-theories", component: FlaggedTheories, canActivate:[Guard], data:{requiresAdmin:true}},
    {path: "edit-theory/:id", component: TheoryForm, canActivate:[Guard]},

    {path: "profile", component: Profile, canActivate:[Guard]},



    //WIDE CARD
    {path: "", redirectTo: "/", pathMatch: 'full'},
    {path: "**", redirectTo: "/"}
];
