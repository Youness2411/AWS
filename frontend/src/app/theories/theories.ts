import { CommonModule } from '@angular/common';
import { Component, OnInit, ElementRef, ViewChild, ChangeDetectorRef } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { Api } from '../service/api';
import { TheoryCard } from '../theory-card/theory-card';
import { TheoryCardSkeletonComponent } from '../theory-card-skeleton/theory-card-skeleton.component';
import { SpoilerModal } from '../spoiler-modal/spoiler-modal';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-theories',
  standalone: true,
  imports: [CommonModule, TheoryCard, TheoryCardSkeletonComponent, FormsModule, SpoilerModal],
  templateUrl: './theories.html',
  styleUrl: './theories.css'
})
export class Theories implements OnInit{
  constructor(
    private apiService:Api, 
    private route:ActivatedRoute, 
    private router:Router,
    private cdr: ChangeDetectorRef
  ){}

  theories: any[] = [];
  baseTheories: any[] = [];
  trending: any[] = [];
  latestChapter: any[] = [];
  userId: any;
  loading: boolean = true;
  loadingTrending: boolean = false;
  loadingLatestChapter: boolean = false;
  error: string | null = null;
  votes: any[] = [];
  votesLoaded: boolean = false;
  searchText: string = '';
  sortBy: 'date'|'mostLiked'|'mostComments'|'mostVotes' = 'date';
  
  // Load More Logic
  totalNumberOfRecordsToShow: number = 20;
  readonly numberOfRecordsOffset: number = 20;
  isLoadMoreVisible: boolean = false;
  
  // Cache with different TTLs for different content types
  private cache: Map<string, any> = new Map();
  private readonly CACHE_TTL = {
    TRENDING: 2 * 60 * 1000,      // 2 minutes (changes frequently)
    LATEST_CHAPTER: 5 * 60 * 1000, // 5 minutes
    THEORIES: 10 * 60 * 1000,     // 10 minutes (more stable)
    USER_VOTES: 15 * 60 * 1000    // 15 minutes (very stable)
  };
  @ViewChild('trendTrack') trendTrack!: ElementRef<HTMLDivElement>;
  @ViewChild('theoriesContainer') theoriesContainer!: ElementRef<HTMLDivElement>;
  activeSlide = 0;
  autoTimer: any;
  showSpoiler = false;
  
  
  // Simple loading - no pagination needed
  
  async ngOnInit() {
    // Spoiler gate (localStorage)
    try{
      const seen = localStorage.getItem('spoiler_ack');
      if (seen == null || seen != "1"){
        this.showSpoiler = true;
      }
      else{
        this.showSpoiler = false;
      }
    }catch{}

    // Load all data in parallel for better performance
    await this.loadInitialData();

    // React to search param changes and fetch theories (only bottom list)
    this.route.queryParamMap.subscribe(async params => {
      const q = params.get('q') || '';
      this.searchText = q;
      await this.applyFilters();
    });
  }

  private async loadInitialData(): Promise<void> {
    // Step 1: Load critical content first (trending + latest chapter)
    const criticalPromises = [
      this.loadTrendingTheories(),
      this.loadLastChapterTheories()
    ];

    // Step 2: Load user data and main theories in parallel (non-blocking)
    const backgroundPromises = [];
    
    if (this.apiService.isAuthenticated()) {
      backgroundPromises.push(
        this.loadUserData().catch(error => {
          console.warn('Failed to load user data:', error);
        })
      );
    }
    
    backgroundPromises.push(
      this.applyFilters().catch(error => {
        console.warn('Failed to load main theories:', error);
      })
    );

    // Wait for critical content first
    await Promise.allSettled(criticalPromises);
    
    // Start carousel immediately after critical content
    this.startCarousel();

    // Load background content without blocking
    Promise.allSettled(backgroundPromises);
  }

  private async loadUserData(): Promise<void> {
    const me: any = await firstValueFrom(this.apiService.getLoggedInUserInfo());
    this.userId = '' + (me?.user?.id ?? me?.id);
    
    // Load votes in background (non-critical)
    this.loadUserVotesInBackground();
  }

  private loadUserVotesInBackground(): void {
    if (this.userId && this.votes.length === 0) {
      // Load votes after a short delay to not block initial render
      setTimeout(() => {
        firstValueFrom(this.apiService.getAllUserVotes(this.userId))
          .then(votes => {
            this.votes = votes.votes || [];
            this.votesLoaded = true;
            console.log('User votes loaded:', this.votes.length, 'votes');
            // Trigger change detection to update theory cards
            this.updateTheoryCardsVotes();
          })
          .catch(error => {
            console.warn('Failed to load user votes:', error);
            this.votes = [];
          });
      }, 500);
    }
  }

  private updateTheoryCardsVotes(): void {
    // Force change detection to update all theory cards with new vote data
    // This will trigger the theory cards to re-evaluate their myVote property
    console.log('Updating theory cards with vote data');
    this.cdr.detectChanges();
  }

  private async loadTrendingTheories(): Promise<void> {
    this.loadingTrending = true;
    try {
      const cacheKey = 'trending_theories';
      let trendingRes = this.getFromCache(cacheKey, 'TRENDING');
      
      if (!trendingRes) {
        trendingRes = await firstValueFrom(this.apiService.getTrendingTheories(10));
        this.setCache(cacheKey, trendingRes, 'TRENDING');
      }
      
      this.trending = trendingRes?.theories || [];
    } finally {
      this.loadingTrending = false;
    }
  }

  private async loadLastChapterTheories(): Promise<void> {
    this.loadingLatestChapter = true;
    try {
      const cacheKey = 'last_chapter_theories';
      let lastChapterRes = this.getFromCache(cacheKey, 'LATEST_CHAPTER');
      
      if (!lastChapterRes) {
        lastChapterRes = await firstValueFrom(this.apiService.getLastChapterTheories(6));
        this.setCache(cacheKey, lastChapterRes, 'LATEST_CHAPTER');
      }
      
      this.latestChapter = lastChapterRes?.theories || [];
      
      // Fallback to latest theories if no last chapter theories
      if(this.latestChapter.length === 0){
        const base: any = await firstValueFrom(this.apiService.queryTheories({ q: '', sort: 'date' }));
        this.baseTheories = base?.theories || [];
        this.latestChapter = [...this.baseTheories].slice(0, 6);
      }
    } finally {
      this.loadingLatestChapter = false;
    }
  }

  async applyFilters(){
    this.loading = true;
    
    try{
      const params: any = { 
        q: this.searchText, 
        sort: this.sortBy
      };
      
      // Create cache key
      const cacheKey = `theories_${JSON.stringify(params)}`;
      
      // Check cache first
      let response = this.getFromCache(cacheKey, 'THEORIES');
      
      if (!response) {
        response = await firstValueFrom(this.apiService.queryTheories(params));
        // Cache the response
        this.setCache(cacheKey, response, 'THEORIES');
      }
      
      if(response?.status === 200){
        this.theories = response.theories || [];
        this.isLoadMoreVisible = this.theories.length > this.numberOfRecordsOffset;
        this.totalNumberOfRecordsToShow = this.numberOfRecordsOffset;
      }
    }catch(error:any){
      this.error = error?.error?.message || 'Failed to load theories';
    }finally{
      this.loading = false;
    }
  }

  private getFromCache(key: string, cacheType: keyof typeof this.CACHE_TTL = 'THEORIES'): any {
    const cached = this.cache.get(key);
    if (cached && (Date.now() - cached.timestamp) < this.CACHE_TTL[cacheType]) {
      return cached.data;
    }
    // Clean up expired cache
    if (cached) {
      this.cache.delete(key);
    }
    return null;
  }

  private setCache(key: string, data: any, cacheType: keyof typeof this.CACHE_TTL = 'THEORIES'): void {
    this.cache.set(key, {
      data: data,
      timestamp: Date.now(),
      type: cacheType
    });
  }

  loadMoreButtonClicked(): void {
    this.totalNumberOfRecordsToShow += this.numberOfRecordsOffset;
    this.isLoadMoreVisible = this.totalNumberOfRecordsToShow < this.theories.length;
  }
  

  private startCarousel(){
    try{
      const el = this.trendTrack?.nativeElement;
      if(!el) return;
      const goTo = (idx:number) => {
        const child = el.children.item(idx) as HTMLElement | null;
        if(!child) return;
        el.scrollTo({ left: child.offsetLeft - 8, behavior: 'smooth' });
        this.activeSlide = idx;
      };
      const next = () => goTo((this.activeSlide + 1) % Math.max(1, this.trending.length));
      clearInterval(this.autoTimer);
      this.autoTimer = setInterval(next, 2000);
      // pause on hover
      el.addEventListener('mouseenter', () => clearInterval(this.autoTimer));
      el.addEventListener('mouseleave', () => { clearInterval(this.autoTimer); this.autoTimer = setInterval(next, 3500); });
    }catch{}
  }

  prevSlide(){ if(!this.trending.length) return; const n=this.trending.length; this.activeSlide=(this.activeSlide-1+n)%n; this.scrollToActive(); }
  nextSlide(){ if(!this.trending.length) return; const n=this.trending.length; this.activeSlide=(this.activeSlide+1)%n; this.scrollToActive(); }
  setSlide(i:number){ this.activeSlide=i; this.scrollToActive(); }
  private scrollToActive(){ const el=this.trendTrack?.nativeElement; const child=el?.children.item(this.activeSlide) as HTMLElement|null; if(child) el.scrollTo({left: child.offsetLeft-8, behavior:'smooth'}); }

  onSpoilerEnter(){
    this.showSpoiler = false;
    try{ localStorage.setItem('spoiler_ack', '1'); }catch{}
  }
  onSpoilerLeave(){
    try{ localStorage.setItem('spoiler_ack', '0'); }catch{}
    // Navigate away or just hide content; here we redirect to an external page
    location.href='https://google.com';
    // this.showSpoiler = false;
  }

  // TrackBy function to prevent unnecessary re-renders
  trackByTheoryId(index: number, theory: any): any {
    return theory?.id || index;
  }

  // Get votes for a specific theory
  getVotesForTheory(theoryId: any): any[] {
    return this.votes.filter((vote: any) => vote.theoryId === theoryId);
  }


}
