/// <reference path="../../types/ambient.d.ts" />
import { CommonModule } from '@angular/common';
import { Component, ElementRef, OnInit, ViewChild, AfterViewInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { Api } from '../service/api';
import { ToastService } from '../service/toast.service';
import { FormsModule } from '@angular/forms';
import { LoginRequiredModal } from '../login-required-modal/login-required-modal';
import { ConfirmModal } from '../confirm-modal/confirm-modal';
import { TheoryDetailsSkeletonComponent } from '../theory-details-skeleton/theory-details-skeleton.component';
import { ChartModule } from 'primeng/chart';
import { Chart, registerables } from 'chart.js';
// @ts-ignore
import Viewer from '@toast-ui/editor/dist/toastui-editor-viewer';

Chart.register(...registerables);


type DailyPoint = { day: string; upRatio: number; upCount: number; downCount: number };

@Component({
  selector: 'app-theory-details',
  standalone: true,
  imports: [CommonModule, FormsModule, LoginRequiredModal, ConfirmModal, TheoryDetailsSkeletonComponent, ChartModule],
  templateUrl: './theory-details.html',
  styleUrls: ['./theory-details.css']
})
export class TheoryDetails implements OnInit, AfterViewInit, OnDestroy{
  constructor(private route:ActivatedRoute, private apiService:Api, private router:Router, private toastService: ToastService){}

  @ViewChild('viewerHost', { static: false }) viewerHost?: ElementRef<HTMLDivElement>;
  viewer!: Viewer;
  
  markdownContent: string = '';

  theory:any = null;
  loading:boolean = true;
  error:string | null = null;
  markdownReady: boolean = false;
  viewerError: boolean = false;

  upVotes:number = 0;
  downVotes:number = 0;
  publicPct:number = 0;

  theoryAuthorImageUrl = null;
  theoryAuthorUsername = null;

  comments:any[] = [];
  submitting:boolean = false;
  commentText:string = '';
  showLoginPopup:boolean = false;
  myVote: 'up'|'down'|null = null;
  currentUserId: string | null = null;

  versions: Array<{id:number; versionNumber:number; createdAt:string; content:string}> = [];
  selectedVersionNumber: number | null = null;
  confirmDeleteTheory: boolean = false;

  editingCommentId: number | null = null;
  editCommentText = '';

  replyingTo: Record<string, boolean> = {};
  replyText: Record<string, string> = {};
  confirmDeleteId: number | null = null;

  series: DailyPoint[] = [];

  chartW = 720;
  chartH = 300;

  publicLineD = '';
  publicAreaD = '';

  hoverActive = false;
  hoverX = 0;
  hoverY = 0;
  hoverPct = 0;
  hoverDate = '';
  tooltipLeft = 0;
  tooltipTop = 0;

  varGreen = '#25a153';

  padL = 8;          // graphe collé à gauche
  padR = 48;         // espace à droite pour les labels
  padT = 16;
  padB = 32;

  scaleMin = 0;
  scaleMax = 100;

  gridVals: number[] = [];
  gridYs: number[] = [];

  lineData: any = null;
  lineOptions: any = null;


  async ngOnInit(){
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.error = 'Missing theory id';
      this.loading = false;
      return;
    }
    try {
      await this.loadAll(id);
      // aiScore is now loaded from the database via loadAll()
      this.readCssVars();
      this.computeScaleAndGrid();
      this.buildPaths();
    }catch(error:any){
      this.error = error?.error?.message || 'Failed to load theory';
    }finally{
      this.loading = false;
    }
  }
  ngAfterViewInit(){     
    // Don't initialize viewer here - wait for data to be loaded
  }
  private initViewer(){
    console.log('initViewer called', {
      hasViewer: !!this.viewer,
      hasHost: !!this.viewerHost?.nativeElement,
      hasContent: !!this.markdownContent,
      contentLength: this.markdownContent?.length
    });
    
    if (this.viewer || !this.viewerHost?.nativeElement || !this.markdownContent) return;
    
    try {
      this.viewer = new Viewer({ el: this.viewerHost.nativeElement, initialValue: this.markdownContent });
      console.log('Viewer initialized successfully');
      this.viewerError = false;
    } catch (error) {
      console.error('Error initializing viewer:', error);
      this.viewerError = true;
    }
  }

  private async loadAll(id: string) {
    const [theoryRes, commentsRes]: any = await Promise.all([
      firstValueFrom(this.apiService.getTheoryById(id)),
      firstValueFrom(this.apiService.getAllTheoryComments(id))
    ]);

    if (this.apiService.isAuthenticated()) {
      const me: any = await firstValueFrom(this.apiService.getLoggedInUserInfo());
      const userId = '' + (me?.user?.id ?? me?.id);
      try {
        const voteRes: any = await firstValueFrom(this.apiService.getUserVote(userId, id));
        this.myVote = voteRes?.vote?.type?.toLowerCase();
      } catch (err: any) {
        if (err?.status !== 404) throw err;
      }
      this.currentUserId = userId;
    }

    this.theory = theoryRes?.theory || null;
    this.theoryAuthorImageUrl = this.theory?.user?.imageUrl ?? null;
    this.theoryAuthorUsername = this.theory?.user?.username ?? null;
    this.upVotes = Number(this.theory?.upVotesCount ?? 0);
    this.downVotes = Number(this.theory?.downVotesCount ?? 0);
    this.comments = commentsRes?.comments || [];
    this.updatePercents();

    const mdRaw = this.theory?.content || '';
    // Convert line breaks to proper markdown format for ToastUI Viewer
    this.markdownContent = mdRaw.replace(/\n/g, '\n\n');
    this.markdownReady = true;
    console.log('Theory loaded, markdown content length:', this.markdownContent.length);
    setTimeout(() => this.initViewer(), 100);

    // load versions list
    try{
      const vres:any = await firstValueFrom(this.apiService.getTheoryVersions(String(this.theory.id)));
      this.versions = vres?.versions || [];
      
      // Auto-select the first version if versions exist
      if (this.versions.length > 0) {
        this.selectedVersionNumber = this.versions[0].versionNumber;
        console.log('Auto-selected first version:', this.selectedVersionNumber);
        // Trigger version change to load the first version's content
        this.onVersionChange();
      } else {
        this.selectedVersionNumber = this.theory?.versionNumber || null;
        console.log('No versions found, using theory version:', this.selectedVersionNumber);
      }
    }catch{}

    // récupérer la série pour le graphe
    const raw:any[] = await firstValueFrom(this.apiService.getDailySeries(id, 30));
    this.series = (Array.isArray(raw) ? raw : []).map((r:any) => ({
      day: r.day ?? r.id?.day ?? '',
      upRatio: Number(r.upRatio),
      upCount: Number(r.upCount ?? 0),
      downCount: Number(r.downCount ?? 0)
    }));
    this.series.sort((a,b) => a.day.localeCompare(b.day));
    this.computeScaleAndGrid();
    this.buildPaths();
    this.buildChart();
  }

  private buildChart(): void {
    const labels = this.series.map(s => this.formatSeriesDate(s.day));
    const data = this.series.map(s => Number(s.upRatio));

    this.lineData = {
      labels,
      datasets: [
        {
          label: 'Public %',
          data,
          fill: false,
          borderColor: this.varGreen || '#25a153',
          tension: 0.35,
          pointRadius: 2,
          pointHoverRadius: 4
        }
      ]
    };

    this.lineOptions = {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: true }
      },
      scales: {
        x: {
          ticks: { maxRotation: 0, autoSkip: true }
        },
        y: {
          min: this.scaleMin,
          max: this.scaleMax,
          ticks: {
            callback: (v: any) => `${v}%`
          }
        }
      }
    };
  }


  private readCssVars() {
    const v = getComputedStyle(document.documentElement).getPropertyValue('--green').trim();
    if (v) this.varGreen = v;
  }

  private updatePercents() {
    const total = this.upVotes + this.downVotes;
    this.publicPct = total > 0 ? (this.upVotes / total) * 100 : 0;
  }

  gaugeColor(p: number): string {
    const clamped = Math.max(0, Math.min(100, p));
    const hue = Math.round((clamped / 100) * 120);
    return `hsl(${hue} 80% 45%)`;
  }

  private xScale(i: number, n: number): number {
    const w = this.chartW - this.padL - this.padR;
    return this.padL + (n <= 1 ? w / 2 : (i * w) / (n - 1));
  }

  private computeScaleAndGrid(): void {
    if (!this.series.length) {
      this.scaleMin = 0; this.scaleMax = 100;
      this.gridVals = [100, 80, 60, 40, 20, 0];
      this.gridYs = this.gridVals.map(v => this.yScale(v));
      return;
    }

    let dmin = Math.min(...this.series.map(s => Number(s.upRatio)));
    let dmax = Math.max(...this.series.map(s => Number(s.upRatio)));

    const span = Math.max(1, dmax - dmin);
    const pad = Math.max(2, span * 0.10);       // marge 10% mini 2 pts
    dmin -= pad;
    dmax += pad;

    dmin = Math.max(0, Math.floor(dmin));
    dmax = Math.min(100, Math.ceil(dmax));
    if (dmax - dmin < 10) dmax = Math.min(100, dmin + 10);  // garde une plage lisible

    const targetTicks = 5;                        // ~5 lignes
    const rawStep = (dmax - dmin) / targetTicks;
    const step = this.niceStep(rawStep);

    const niceMin = Math.max(0, Math.floor(dmin / step) * step);
    const niceMax = Math.min(100, Math.ceil(dmax / step) * step);

    this.scaleMin = niceMin;
    this.scaleMax = niceMax;

    const vals: number[] = [];
    for (let v = niceMax; v >= niceMin; v -= step) vals.push(v);
    if (vals[vals.length - 1] !== niceMin) vals.push(niceMin);

    this.gridVals = vals;
    this.gridYs = this.gridVals.map(v => this.yScale(v));
  }

  private niceStep(x: number): number {
    const p = Math.pow(10, Math.floor(Math.log10(x)));
    const n = x / p;
    if (n <= 1) return 1 * p;
    if (n <= 2) return 2 * p;
    if (n <= 5) return 5 * p;
    return 10 * p;
  }

  private yScale(pct: number): number {
    const h = this.chartH - this.padT - this.padB;
    const c = Math.max(this.scaleMin, Math.min(this.scaleMax, pct));
    const t = (c - this.scaleMin) / (this.scaleMax - this.scaleMin || 1);
    return this.chartH - this.padB - t * h;
  }

  private buildGrid(): void {
    // conservé pour compat, mais computeScaleAndGrid calcule déjà gridVals/gridYs
  }


  private smoothPath(points: { x: number; y: number }[]): string {
    if (points.length === 0) return '';
    if (points.length === 1) return `M ${points[0].x} ${points[0].y} L ${points[0].x + 0.001} ${points[0].y}`;
    const d: string[] = [];
    d.push(`M ${points[0].x} ${points[0].y}`);
    for (let i = 0; i < points.length - 1; i++) {
      const p0 = i > 0 ? points[i - 1] : points[0];
      const p1 = points[i];
      const p2 = points[i + 1];
      const p3 = i !== points.length - 2 ? points[i + 2] : p2;
      const t = 0.2;
      const c1x = p1.x + (p2.x - p0.x) * t;
      const c1y = p1.y + (p2.y - p0.y) * t;
      const c2x = p2.x - (p3.x - p1.x) * t;
      const c2y = p2.y - (p3.y - p1.y) * t;
      d.push(`C ${c1x} ${c1y} ${c2x} ${c2y} ${p2.x} ${p2.y}`);
    }
    return d.join(' ');
  }

  private buildPaths() {
    const n = this.series.length;
    if (!n) {
      this.publicLineD = '';
      this.publicAreaD = '';
      return;
    }
    const pts = this.series.map((s, i) => ({ x: this.xScale(i, n), y: this.yScale(s.upRatio) }));
    this.publicLineD = this.smoothPath(pts);
    if (pts.length > 1) {
      const baseY = this.yScale(this.scaleMin);
      this.publicAreaD = `${this.publicLineD} L ${pts[pts.length - 1].x} ${baseY} L ${pts[0].x} ${baseY} Z`;
    } else {
      this.publicAreaD = '';
    }
  }

  tipSide: 'left' | 'right' = 'right';

  onChartMove(e: MouseEvent): void {
    const host = e.currentTarget as HTMLElement;
    const svg = host.querySelector('svg') as SVGSVGElement;
    if (!svg || !this.series.length) return;

    const hostRect = host.getBoundingClientRect();
    const svgRect  = svg.getBoundingClientRect();

    const pxInSvg = e.clientX - svgRect.left;
    const scaledX = (pxInSvg / svgRect.width) * this.chartW;

    let idx = 0, best = Infinity;
    for (let i = 0; i < this.series.length; i++) {
      const xi = this.xScale(i, this.series.length);
      const d  = Math.abs(xi - scaledX);
      if (d < best) { best = d; idx = i; }
    }

    const x = this.xScale(idx, this.series.length);
    const y = this.yScale(this.series[idx].upRatio);

    this.hoverActive = true;
    this.hoverX = x;
    this.hoverY = y;
    this.hoverPct  = this.series[idx].upRatio;
    this.hoverDate = this.formatSeriesDate(this.series[idx].day);

    const leftWithinSvg = (x / this.chartW) * svgRect.width;
    const topWithinSvg  = (y / this.chartH) * svgRect.height;

    const absLeft = (svgRect.left - hostRect.left) + leftWithinSvg;
    const absTop  = (svgRect.top  - hostRect.top)  + topWithinSvg;

    const margin = 8;
    const tipW = 180; // largeur estimée du tooltip
    this.tipSide = (absLeft + tipW > hostRect.width - margin) ? 'left' : 'right';

    this.tooltipLeft = absLeft;
    this.tooltipTop  = absTop - 12;

    if (this.tipSide === 'left' && this.tooltipLeft < margin) this.tooltipLeft = margin;
    if (this.tipSide === 'right' && this.tooltipLeft > hostRect.width - margin) this.tooltipLeft = hostRect.width - margin;
  }


  onChartLeave() {
    this.hoverActive = false;
  }

  private formatSeriesDate(d: string | Date): string {
    const dt = typeof d === 'string' ? new Date(d) : d;
    return dt.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' });
  }


  isAuth():boolean{ return this.apiService.isAuthenticated(); }

  canEditTheory(): boolean {
    const uid = this.currentUserId;
    const isOwner = !!uid && (this.theory?.user?.id + '') === uid;
    return isOwner || this.apiService.isModerator() || this.apiService.isAdmin();
  }

  goToEditTheory(){
    if(!this.theory?.id) return;
    this.router.navigate(["/edit-theory", String(this.theory.id)]);
  }

  async confirmDeleteTheoryAction(){
    if(!this.theory?.id) return;
    try{
      await firstValueFrom(this.apiService.deleteTheory(String(this.theory.id)));
      this.toastService.theoryDeleted();
      this.confirmDeleteTheory = false;
      this.router.navigate(["/"]);
    }catch{
      this.toastService.theoryDeleteFailed();
      this.confirmDeleteTheory = false;
    }
  }

  onVersionChange(){
    console.log('Version change triggered, selectedVersionNumber:', this.selectedVersionNumber);
    const chosen = this.versions.find(v => v.versionNumber == this.selectedVersionNumber);
    console.log('Found version:', chosen);
    const raw = chosen ? chosen.content : (this.theory?.content || '');
    console.log('Content length:', raw.length);
    
    // Convert line breaks to proper markdown format for ToastUI Viewer
    this.markdownContent = raw.replace(/\n/g, '\n\n');
 
    // Update the viewer with new content
    if (this.viewer) {
      console.log('Updating existing viewer with new content');
      this.viewer.setMarkdown(this.markdownContent);
    } else {
      console.log('Viewer not initialized yet, will initialize with new content');
      // If viewer not initialized yet, initialize it
      setTimeout(() => this.initViewer(), 100);
    }
  }

  resetToLatest(){
    this.selectedVersionNumber = this.theory?.versionNumber || null;
    this.onVersionChange();
  }

  async voteUp() {
    if (!this.theory?.id) return;
    try {
      if (this.myVote === 'up') {
        await firstValueFrom(this.apiService.unVote({ theoryId: this.theory.id }));
        this.myVote = null;
        this.toastService.info('Vote retiré', 'Votre vote a été retiré.');
      } else {
        await firstValueFrom(this.apiService.upVote({ theoryId: this.theory.id }));
        this.myVote = 'up';
        this.toastService.voteSuccess('up');
      }
      await this.refreshVotes();
    } catch {
      this.toastService.voteFailed();
    }
  }

  async voteDown() {
    if (!this.theory?.id) return;
    try {
      if (this.myVote === 'down') {
        await firstValueFrom(this.apiService.unVote({ theoryId: this.theory.id }));
        this.myVote = null;
        this.toastService.info('Vote retiré', 'Votre vote a été retiré.');
      } else {
        await firstValueFrom(this.apiService.downVote({ theoryId: this.theory.id }));
        this.myVote = 'down';
        this.toastService.voteSuccess('down');
      }
      await this.refreshVotes();
    } catch {
      this.toastService.voteFailed();
    }
  }

  async unvote() {
    if (!this.theory?.id) return;
    try {
      await firstValueFrom(this.apiService.unVote({ theoryId: this.theory.id }));
      this.myVote = null;
      this.toastService.info('Vote retiré', 'Votre vote a été retiré.');
      await this.refreshVotes();
    } catch {
      this.toastService.voteFailed();
    }
  }

  onVoteClick(type: 'up' | 'down') {
    if (!this.isAuth()) {
      this.showLoginPopup = true;
      return;
    }
    if (type === 'up') this.voteUp();
    else this.voteDown();
  }

  closeLoginPopup() {
    this.showLoginPopup = false;
  }

  private async refreshVotes() {
    const id = String(this.theory.id);
    const [upRes, downRes]: any = await Promise.all([
      firstValueFrom(this.apiService.getAllTheoryUpVotes(id)),
      firstValueFrom(this.apiService.getAllTheoryDownVotes(id))
    ]);
    this.upVotes = (upRes?.votes || []).length;
    this.downVotes = (downRes?.votes || []).length;
    this.updatePercents();
  }

  async submitComment() {
    if (!this.isAuth() || !this.commentText.trim() || !this.theory?.id) return;
    this.submitting = true;
    try {
      await firstValueFrom(this.apiService.postComment({ content: this.commentText.trim(), theoryId: this.theory.id }));
      this.commentText = '';
      this.toastService.commentPosted();
      const id = String(this.theory.id);
      const commentsRes: any = await firstValueFrom(this.apiService.getAllTheoryComments(id));
      this.comments = commentsRes?.comments || [];
    } catch {
      this.toastService.commentFailed();
    } finally {
      this.submitting = false;
    }
  }

  canEditComment(c: any): boolean {
    const uid = this.currentUserId;
    const isOwner = !!uid && (c?.user?.id + '') === uid;
    return isOwner || this.apiService.isModerator() || this.apiService.isAdmin();
  }

  startEditComment(c: any) {
    if (!this.canEditComment(c)) return;
    this.editingCommentId = c?.id ?? null;
    this.editCommentText = c?.content ?? '';
  }

  cancelEditComment() {
    this.editingCommentId = null;
    this.editCommentText = '';
  }

  async saveEditComment(){
    if(!this.editingCommentId || !this.editCommentText.trim()) return;
    try{
      await firstValueFrom(this.apiService.updateComment(this.editingCommentId.toString(), {content: this.editCommentText.trim() }));
      this.toastService.success('Commentaire modifié !', 'Votre commentaire a été mis à jour.');
      // refresh comments
      const id = String(this.theory.id);
      const commentsRes:any = await firstValueFrom(this.apiService.getAllTheoryComments(id));
      this.comments = commentsRes?.comments || [];
      this.cancelEditComment();
    }catch{
      this.toastService.error('Erreur de modification', 'Impossible de modifier le commentaire. Veuillez réessayer.');
    }
  }

  async deleteComment(c:any){
    const cid = c?.id + ''
    try{
      await firstValueFrom(this.apiService.deleteComment(cid));
      this.toastService.commentDeleted();
      // refresh comments
      const id = String(this.theory.id);
      const commentsRes:any = await firstValueFrom(this.apiService.getAllTheoryComments(id));
      this.comments = commentsRes?.comments || [];
    }catch{
      this.toastService.commentDeleteFailed();
    }
  }

  toggleReply(c: any) {
    if (!this.isAuth()) {
      this.showLoginPopup = true;
      return;
    }
    const key = String(c?.id);
    this.replyingTo[key] = !this.replyingTo[key];
    if (this.replyingTo[key] && !this.replyText[key]) this.replyText[key] = '';
  }

  async submitReply(c: any) {
    if (!this.isAuth()) {
      this.showLoginPopup = true;
      return;
    }
    const key = String(c?.id);
    const text = (this.replyText[key] || '').trim();
    if (!text || !this.theory?.id) return;
    try {
      await firstValueFrom(
        this.apiService.postComment({ content: text, theoryId: this.theory.id, parentId: c?.id })
      );
      this.toastService.replyPosted();
      this.replyText[key] = '';
      this.replyingTo[key] = false;
      const id = String(this.theory.id);
      const commentsRes: any = await firstValueFrom(this.apiService.getAllTheoryComments(id));
      this.comments = commentsRes?.comments || [];
    } catch {
      this.toastService.replyFailed();
    }
  }
  
  ngOnDestroy() {
    // Clean up the viewer to prevent memory leaks
    if (this.viewer) {
      this.viewer.destroy();
    }
  }
}
