import { Directive, ElementRef, Input, OnInit, OnDestroy } from '@angular/core';

/**
 * Count-up animation directive.
 * Animates a number from 0 to [target] when the element scrolls into view.
 * Usage: <span appCountUp [target]="totalJobs()" [suffix]="'+'"></span>
 */
@Directive({
  selector: '[appCountUp]',
  standalone: true
})
export class CountUpDirective implements OnInit, OnDestroy {
  @Input() target = 0;
  @Input() duration = 1800;
  @Input() suffix = '';
  @Input() prefix = '';

  private observer!: IntersectionObserver;
  private rafId = 0;

  constructor(private el: ElementRef<HTMLElement>) {}

  ngOnInit(): void {
    this.observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting) {
          this.animate();
          this.observer.disconnect();
        }
      },
      { threshold: 0.3 }
    );
    this.observer.observe(this.el.nativeElement);
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
    cancelAnimationFrame(this.rafId);
  }

  private animate(): void {
    const start = performance.now();
    const target = this.target;
    const duration = this.duration;
    const el = this.el.nativeElement;

    const step = (now: number) => {
      const elapsed = now - start;
      const progress = Math.min(elapsed / duration, 1);
      // Ease-out cubic
      const eased = 1 - Math.pow(1 - progress, 3);
      const current = Math.round(eased * target);
      el.textContent = `${this.prefix}${current}${this.suffix}`;
      if (progress < 1) {
        this.rafId = requestAnimationFrame(step);
      } else {
        el.textContent = `${this.prefix}${target}${this.suffix}`;
      }
    };

    this.rafId = requestAnimationFrame(step);
  }
}
