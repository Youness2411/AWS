/// <reference path="../../types/ambient.d.ts" />
import { CommonModule } from '@angular/common';
import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { first, firstValueFrom } from 'rxjs';
import { Api } from '../service/api';
import { ToastService } from '../service/toast.service';
// @ts-ignore
import Editor from '@toast-ui/editor';

@Component({
  selector: 'app-theory-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './theory-form.html',
  styleUrl: './theory-form.css'
})
export class TheoryForm implements OnInit, OnDestroy {
  constructor(private apiService:Api, private router:Router, private route:ActivatedRoute, private toastService: ToastService){}

  form = {
    title: '',
    content: '',
    isRelatedToLastChapter: false
  };
  @ViewChild('editorHost', { static: true }) editorHost!: ElementRef<HTMLDivElement>;
  editor!: Editor;

  imageFile: File | null = null;
  imagePreview: string | null = null;
  submitting = false;
  message: string | null = null;
  error: string | null = null;

  onImageChange(event: any){
    const file = event?.target?.files?.[0];
    this.imageFile = file || null;
    if (this.imageFile){
      const reader = new FileReader();
      reader.onload = () => this.imagePreview = String(reader.result);
      reader.readAsDataURL(this.imageFile);
    } else {
      this.imagePreview = null;
    }
  }

  ngOnInit(){
    const editId = this.route.snapshot.paramMap.get('id');
    this.editor = new Editor({
      el: this.editorHost.nativeElement,
      height: '420px',
      initialEditType: 'markdown',
      previewStyle: 'vertical',
      placeholder: 'Write your theory in Markdown...'
    });
    this.editor.on('change', () => {
      this.form.content = this.editor.getMarkdown();
    });
    // image upload hook
    this.editor.addHook('addImageBlobHook', async (blob: Blob, callback: (url: string, altText?: string) => void) => {
      try{
        const fd = new FormData();
        fd.append('image', blob);
        const resp = await firstValueFrom(this.apiService.uploadImage(fd)); 
        const url = resp?.url.replaceAll(" ","%20");
        if(url) callback(url, 'image');
      }catch{}
    });
    // If edit mode, prefill
    if(editId){
      (async () => {
        try{
          const res:any = await firstValueFrom(this.apiService.getTheoryById(editId));
          const t = res?.theory;
          if(t){
            this.form.title = t.title || '';
            this.form.content = t.content || '';
            this.form.isRelatedToLastChapter = t.isRelatedToLastChapter || false;
            this.editor.setMarkdown(this.form.content);
          }
        }catch{}
      })();
    }
  }

  ngOnDestroy(){
    try{ this.editor?.destroy?.(); }catch{}
  }

  async submit(){
    this.error = null; this.message = null;
    if(!this.form.title.trim() || !this.form.content.trim()){
      this.error = 'Title and content are required';
      return;
    }
    if(!this.apiService.isAuthenticated()){
      this.error = 'Please log in to create a theory';
      return;
    }
    this.submitting = true;
    try{
      const editId = this.route.snapshot.paramMap.get('id');
      const formData = new FormData();
      formData.append('title', this.form.title.trim());
      formData.append('content', this.form.content.trim());
      formData.append('isRelatedToLastChapter', String(this.form.isRelatedToLastChapter));
      if(this.imageFile){
        formData.append('image', this.imageFile);
      }
      let res:any;
      if(editId){
        formData.append('id', editId);
        res = await firstValueFrom(this.apiService.updateTheory(formData));
      }else{
        res = await firstValueFrom(this.apiService.postTheory(formData));
      }
      if(res?.status === 200){
        if(res?.message === "Theory is under review by the moderation team"){
          this.toastService.theoryCreationFailed("Theory is under review by the moderation team");
        }
        else {
          if(editId){
            this.toastService.theoryUpdated();
          }
          else {
            this.toastService.theoryCreated();
          }
        }
        this.message = editId ? 'Theory updated successfully' : 'Theory created successfully';
        this.form = { title:'', content:'', isRelatedToLastChapter: false };
        this.imageFile = null; this.imagePreview = null;
        // Optionally navigate to list
        await this.router.navigate(editId ? ["/theories", editId] : ['/']);
      }
    }catch(error:any){
      const editId = this.route.snapshot.paramMap.get('id');
      if(editId) {
        this.toastService.theoryUpdateFailed();
      } else {
        this.toastService.theoryCreationFailed(error?.error?.message || 'Failed to create theory');
      }
      this.error = error?.error?.message || 'Failed to create theory';
    }finally{
      this.submitting = false;
    }
  }
}
