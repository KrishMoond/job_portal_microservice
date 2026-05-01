import { Directive, ElementRef, HostListener, Input, OnDestroy } from '@angular/core';

/**
 * Subtle 3-D tilt effect on hover.
 * Usage: <div appTilt [maxTilt]="8">
 */
@Directive({
  selector: '[appTilt]',
  standalone: true
})
export class TiltDirective implements OnDestroy {
  @Input() maxTilt = 6;
  @Input() perspective = 800;
  @Input() scale = 1.02;

  private rafId = 0;

  constructor(private el: ElementRef<HTMLElement>) {}

  @HostListener('mousemove', ['$event'])
  onMouseMove(e: MouseEvent): void {
    cancelAnimationFrame(this.rafId);
    this.rafId = requestAnimationFrame(() => {
      const el = this.el.nativeElement;
      const rect = el.getBoundingClientRect();
      const cx = rect.left + rect.width / 2;
      const cy = rect.top + rect.height / 2;
      const dx = (e.clientX - cx) / (rect.width / 2);
      const dy = (e.clientY - cy) / (rect.height / 2);
      const rx = dy * this.maxTilt;
      const ry = -dx * this.maxTilt;
      el.style.transform = `perspective(${this.perspective}px) rotateX(${rx}deg) rotateY(${ry}deg) scale(${this.scale})`;
      el.style.transition = 'transform 0.1s ease';
    });
  }

  @HostListener('mouseleave')
  onMouseLeave(): void {
    cancelAnimationFrame(this.rafId);
    const el = this.el.nativeElement;
    el.style.transform = `perspective(${this.perspective}px) rotateX(0deg) rotateY(0deg) scale(1)`;
    el.style.transition = 'transform 0.4s ease';
  }

  ngOnDestroy(): void {
    cancelAnimationFrame(this.rafId);
  }
}
