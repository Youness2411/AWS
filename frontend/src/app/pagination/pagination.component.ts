import { CommonModule } from '@angular/common';
import { Component, Input, Output, EventEmitter } from '@angular/core';

export interface PaginationInfo {
  currentPage: number;
  totalPages: number;
  totalElements: number;
  pageSize: number;
}

@Component({
  selector: 'app-pagination',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './pagination.component.html',
  styleUrl: './pagination.component.css'
})
export class PaginationComponent {
  @Input() paginationInfo: PaginationInfo | null = null;
  @Input() showInfo: boolean = true;
  @Input() maxVisiblePages: number = 5;
  @Output() pageChange = new EventEmitter<number>();

  get pages(): number[] {
    if (!this.paginationInfo) return [];
    
    const { currentPage, totalPages } = this.paginationInfo;
    const pages: number[] = [];
    
    if (totalPages <= this.maxVisiblePages) {
      // Show all pages if total is less than max visible
      for (let i = 0; i < totalPages; i++) {
        pages.push(i);
      }
    } else {
      // Calculate start and end pages
      let startPage = Math.max(0, currentPage - Math.floor(this.maxVisiblePages / 2));
      let endPage = Math.min(totalPages - 1, startPage + this.maxVisiblePages - 1);
      
      // Adjust start page if we're near the end
      if (endPage - startPage < this.maxVisiblePages - 1) {
        startPage = Math.max(0, endPage - this.maxVisiblePages + 1);
      }
      
      for (let i = startPage; i <= endPage; i++) {
        pages.push(i);
      }
    }
    
    return pages;
  }

  get hasPreviousPage(): boolean {
    return this.paginationInfo ? this.paginationInfo.currentPage > 0 : false;
  }

  get hasNextPage(): boolean {
    return this.paginationInfo ? this.paginationInfo.currentPage < this.paginationInfo.totalPages - 1 : false;
  }

  get startElement(): number {
    if (!this.paginationInfo) return 0;
    return this.paginationInfo.currentPage * this.paginationInfo.pageSize + 1;
  }

  get endElement(): number {
    if (!this.paginationInfo) return 0;
    const { currentPage, pageSize, totalElements } = this.paginationInfo;
    return Math.min((currentPage + 1) * pageSize, totalElements);
  }

  goToPage(page: number): void {
    if (this.paginationInfo && page >= 0 && page < this.paginationInfo.totalPages) {
      this.pageChange.emit(page);
    }
  }

  goToFirstPage(): void {
    this.goToPage(0);
  }

  goToLastPage(): void {
    if (this.paginationInfo) {
      this.goToPage(this.paginationInfo.totalPages - 1);
    }
  }

  goToPreviousPage(): void {
    if (this.hasPreviousPage) {
      this.goToPage(this.paginationInfo!.currentPage - 1);
    }
  }

  goToNextPage(): void {
    if (this.hasNextPage) {
      this.goToPage(this.paginationInfo!.currentPage + 1);
    }
  }

  trackByPage(index: number, page: number): number {
    return page;
  }
}
