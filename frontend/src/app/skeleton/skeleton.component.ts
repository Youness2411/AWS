import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-skeleton',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="skeleton" [class]="type" [style.width]="width" [style.height]="height" [style.border-radius]="borderRadius">
      <div class="skeleton-shimmer"></div>
    </div>
  `,
  styleUrls: ['./skeleton.component.css']
})
export class SkeletonComponent {
  @Input() type: 'text' | 'rect' | 'circle' | 'card' = 'rect';
  @Input() width: string = '100%';
  @Input() height: string = '20px';
  @Input() borderRadius: string = '4px';
}
