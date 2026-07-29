import { Directive, ElementRef, Input, OnInit, OnDestroy } from '@angular/core';

type RevealDirection = 'up' | 'down' | 'left' | 'right';

/**
 * Scroll-reveal directive using IntersectionObserver.
 * Usage: <div appScrollReveal> or <div appScrollReveal [delay]="100" [direction]="'left'">
 */
@Directive({
  selector: '[appScrollReveal]',
  standalone: true
})
export class ScrollRevealDirective implements OnInit, OnDestroy {
  /** CSS animation class to add when the element enters the viewport */
  @Input() animClass = 'sr-visible';
  /** Delay in ms before the animation fires */
  @Input() delay = 0;
  /** How much of the element must be visible before triggering (0–1) */
  @Input() threshold = 0.12;
  /** Initial reveal direction */
  @Input() direction: RevealDirection = 'up';
  /** Distance to move before the animation */
  @Input() distance = 24;
  /** Duration of the reveal transition in seconds */
  @Input() duration = 0.6;
  /** Easing function for the transition */
  @Input() easing = 'cubic-bezier(0.22, 1, 0.36, 1)';

  private observer?: IntersectionObserver;

  constructor(private el: ElementRef<HTMLElement>) {}

  ngOnInit(): void {
    const el = this.el.nativeElement;
    const initialTransform = this.getInitialTransform(this.direction, this.distance);

    el.style.opacity = '0';
    el.style.transform = initialTransform;
    el.style.transition = `opacity ${this.duration}s ${this.easing}, transform ${this.duration}s ${this.easing}`;
    el.style.transitionDelay = this.delay ? `${this.delay}ms` : '0ms';
    el.style.willChange = 'opacity, transform';
    el.style.backfaceVisibility = 'hidden';
    el.style.transformOrigin = 'center center';

    this.observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            el.style.opacity = '1';
            el.style.transform = 'translate3d(0, 0, 0) scale(1)';
            el.classList.add(this.animClass);
            this.observer?.unobserve(el);
          }
        });
      },
      { threshold: this.threshold, rootMargin: '0px 0px -8% 0px' }
    );

    this.observer.observe(el);
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
  }

  private getInitialTransform(direction: RevealDirection, distance: number): string {
    switch (direction) {
      case 'left':
        return `translate3d(-${distance}px, 0, 0)`;
      case 'right':
        return `translate3d(${distance}px, 0, 0)`;
      case 'down':
        return `translate3d(0, ${distance}px, 0)`;
      case 'up':
      default:
        return `translate3d(0, -${distance}px, 0)`;
    }
  }
}
