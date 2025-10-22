import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { Api } from '../service/api';
import { TheoryCard } from '../theory-card/theory-card';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-flagged-theories',
  standalone: true,
  imports: [CommonModule, TheoryCard, FormsModule],
  templateUrl: './flagged-theories.html',
  styleUrl: './flagged-theories.css'
})
export class FlaggedTheories implements OnInit{
  constructor(private apiService:Api, private route:ActivatedRoute, private router:Router){}

  theories: any[] = [];
  userId: any;
  loading: boolean = true;
  error: string | null = null;
  votes: any[] = [];
  searchText: string = '';
  sortBy: 'date'|'mostLiked'|'mostComments'|'mostVotes' = 'date';
  
  async ngOnInit() {

    // Load user votes once
    try{
      if(this.apiService.isAuthenticated()){
        const me:any = await firstValueFrom(this.apiService.getLoggedInUserInfo());
        this.userId = '' + (me?.user?.id ?? me?.id);
        const votes:any = await firstValueFrom(this.apiService.getAllUserVotes(this.userId));
        this.votes = votes.votes || [];
      }
    }catch{}

    // Load flagged theories
    if(this.theories.length === 0){
      this.loading = true;
      try{
        const base:any = await firstValueFrom(this.apiService.getAllTheoriesFlagged());
        this.theories = base?.theories || [];
      }catch{}
      finally{
        this.loading = false;
        if (this.theories.length === 0){
          this.error = 'No flagged theories found';
        }
      }
    }
   
  }

}
