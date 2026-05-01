import { Directive, ElementRef, Input, OnInit, OnDestroy } from '@angular/core';

/**
 * Scroll-reveal directive using IntersectionObserver.
 * Usage: <div appScrollReveal> or <div appScrollReveal animClass="animate-slide-up" delay="100">
 */
@Directive({
  selector: '[appScrollReveal]',
  standalone: true
})
export class ScrollRevealDirective implements OnInit, OnDestroy {
  /** CSS animation class to add when element enters viewport */
  @Input() animClass = 'sr-visible';
  /** Delay in ms before the animation fires */
  @Input() delay = 0;
  /** How much of the element must be visible before triggering (0–1) */
  @Input() threshold = 0.12;

  private observer!: IntersectionObserver;

  constructor(private el: ElementRef<HTMLElement>) {}

  ngOnInit(): void {
    const el = this.el.nativeElement;

    // Start invisible
    el.style.opacity = '0';
    el.style.transform = 'translateY(24px)';
    el.style.transition = 'opacity 0.55s cubic-bezier(0.4,0,0.2,1), transform 0.55s cubic-bezier(0.4,0,0.2,1)';
    if (this.delay) {
      el.style.transitionDelay = `${this.delay}ms`;
    }

    this.observer = new IntersectionObserver(
      (entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            el.style.opacity = '1';
            el.style.transform = 'translateY(0)';
            el.classList.add(this.animClass);
            this.observer.unobserve(el);
          }
        });
      },
      { threshold: this.threshold, rootMargin: '0px 0px -40px 0px' }
    );

    this.observer.observe(el);
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
  }
}
