import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Component, Input, OnInit, ElementRef, ViewChild, OnDestroy } from '@angular/core';
import { marked } from 'marked';
import DOMPurify from 'dompurify';
import { firstValueFrom } from 'rxjs';
import { Api } from '../service/api';
import { BookmarkService } from '../service/bookmark.service';
import { ToastService } from '../service/toast.service';

@Component({
  selector: 'app-theory-card',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './theory-card.html',
  styleUrl: './theory-card.css'
})
export class TheoryCard implements OnInit, OnDestroy{
  constructor(
    private apiService:Api, 
    private bookmarkService: BookmarkService,
    private toastService: ToastService
  ){}

  @Input() theory: any;
  @Input() userId: any;
  @Input() userVotes: any[] = [];
  @ViewChild('cardHost', { static: true }) cardHost!: ElementRef<HTMLDivElement>;

  upVotes: number = 0;
  downVotes: number = 0;
  totalComments: number = 0;
  theoryImageUrl = null;
  theoryAuthorUsername = null;
  showLoginPopup: boolean = false;
  myVote: 'up'|'down'|null = null;
  previewHtml: string = '';
  showPreview = false;
  previewLeft = 0;
  previewTop = 0;
  previewWidth = 320;
  hideTimer:any;
  showDelayTimer:any;
  private onWinScroll = () => { this.forceHide(); };
  isBookmarked: boolean = false;

  async ngOnInit(){
    if(!this.theory?.id) return;
    this.theoryImageUrl = this.theory?.imageUrl;
    this.theoryAuthorUsername = this.theory?.user?.username;
    this.upVotes = this.theory?.upVotesCount;
    this.downVotes = this.theory?.downVotesCount;
    this.totalComments = this.theory?.commentsCount;
    this.myVote = this.userVotes.find((v:any) => v.theoryId === this.theory.id)?.type.toLowerCase();
    const raw = (this.theory?.content || '').slice(0, 280);
    // Convert line breaks to proper markdown format
    const processedContent = raw.replace(/\n/g, '\n\n');
    const html = marked.parse(processedContent) as string;
    this.previewHtml = DOMPurify.sanitize(html);
    
    // Subscribe to bookmark status
    this.bookmarkService.bookmarkedTheoryIds$.subscribe(ids => {
      this.isBookmarked = ids.has(this.theory.id);
    });
  }

  onCardEnter(){
    clearTimeout(this.hideTimer);
    clearTimeout(this.showDelayTimer);
    const el = this.cardHost?.nativeElement;
    if(!el) { this.showPreview = true; return; }
    const r = el.getBoundingClientRect();
    const vw = window.innerWidth;
    const px = Math.min(Math.max(r.left, 8), vw - r.width - 8);
    this.previewLeft = px;
    this.previewTop = r.bottom + 8;
    this.previewWidth = r.width;
    this.showDelayTimer = setTimeout(() => { this.showPreview = true; this.attachScroll(); }, 1000);
  }

  onCardLeave(){
    clearTimeout(this.showDelayTimer);
    this.queueHide();
  }

  onPreviewEnter(){
    clearTimeout(this.hideTimer);
  }

  onPreviewLeave(){
    this.queueHide();
  }

  private queueHide(){
    clearTimeout(this.hideTimer);
    this.hideTimer = setTimeout(() => { this.forceHide(); }, 120);
  }

  private forceHide(){
    this.showPreview = false;
    this.detachScroll();
  }

  private attachScroll(){
    window.addEventListener('scroll', this.onWinScroll, { passive: true });
  }

  private detachScroll(){
    window.removeEventListener('scroll', this.onWinScroll as any);
  }

  ngOnDestroy(){
    clearTimeout(this.hideTimer);
    clearTimeout(this.showDelayTimer);
    this.detachScroll();
  }

  isAuth():boolean{ return this.apiService.isAuthenticated(); }

  async onVoteClick(type:'up'|'down', event:MouseEvent){
    event.preventDefault();
    event.stopPropagation();
    if(!this.isAuth()){
      this.showLoginPopup = true;
      return;
    }
    try{
      if(type==='up'){
        if(this.myVote === 'up'){
          await firstValueFrom(this.apiService.unVote({theoryId: this.theory.id}));
          this.myVote = null;
        }else{
          await firstValueFrom(this.apiService.upVote({theoryId: this.theory.id}));
          this.myVote = 'up';
        }
      }else{
        if(this.myVote === 'down'){
          await firstValueFrom(this.apiService.unVote({theoryId: this.theory.id}));
          this.myVote = null;
        }else{
          await firstValueFrom(this.apiService.downVote({theoryId: this.theory.id}));
          this.myVote = 'down';
        }
      }
      await this.refreshVotes();
    }catch{}
  }

  private async refreshVotes(){
    const [upRes, downRes]:any = await Promise.all([
      firstValueFrom(this.apiService.getAllTheoryUpVotes(String(this.theory.id))),
      firstValueFrom(this.apiService.getAllTheoryDownVotes(String(this.theory.id)))
    ]);
    this.upVotes = (upRes?.votes || []).length;
    this.downVotes = (downRes?.votes || []).length;
  }

  async onBookmarkClick(event: MouseEvent){
    event.preventDefault();
    event.stopPropagation();
    if(!this.isAuth()){
      this.showLoginPopup = true;
      return;
    }
    try{
      const wasBookmarked = this.isBookmarked;
      await firstValueFrom(this.bookmarkService.toggleBookmark(this.theory.id));
      
      // Show appropriate toast based on action
      if(wasBookmarked){
        this.toastService.bookmarkRemoved();
      } else {
        this.toastService.bookmarkAdded();
      }
    }catch(err){
      console.error('Error toggling bookmark:', err);
      this.toastService.bookmarkFailed();
    }
  }

  
}
