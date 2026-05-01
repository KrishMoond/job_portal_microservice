import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-skeleton',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div [class]="'skeleton ' + variant" [style.width]="width" [style.height]="height"></div>
  `,
  styles: []
})
export class SkeletonComponent {
  @Input() variant: 'text' | 'title' | 'card' | 'circle' | 'rectangle' = 'text';
  @Input() width: string = '100%';
  @Input() height: string = 'auto';
}
