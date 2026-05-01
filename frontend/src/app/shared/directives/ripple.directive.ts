import { Directive, ElementRef, HostListener, Renderer2 } from '@angular/core';

/**
 * Ripple click effect for buttons and interactive elements.
 * Usage: <button appRipple>Click me</button>
 */
@Directive({
  selector: '[appRipple]',
  standalone: true
})
export class RippleDirective {
  constructor(private el: ElementRef<HTMLElement>, private renderer: Renderer2) {
    // Ensure the host has position:relative for the ripple to be contained
    const host = this.el.nativeElement;
    if (getComputedStyle(host).position === 'static') {
      this.renderer.setStyle(host, 'position', 'relative');
    }
    this.renderer.setStyle(host, 'overflow', 'hidden');
  }

  @HostListener('click', ['$event'])
  onClick(e: MouseEvent): void {
    const el = this.el.nativeElement;
    const rect = el.getBoundingClientRect();
    const size = Math.max(rect.width, rect.height) * 2;
    const x = e.clientX - rect.left - size / 2;
    const y = e.clientY - rect.top - size / 2;

    const ripple = this.renderer.createElement('span') as HTMLSpanElement;
    this.renderer.setStyle(ripple, 'position', 'absolute');
    this.renderer.setStyle(ripple, 'width', `${size}px`);
    this.renderer.setStyle(ripple, 'height', `${size}px`);
    this.renderer.setStyle(ripple, 'left', `${x}px`);
    this.renderer.setStyle(ripple, 'top', `${y}px`);
    this.renderer.setStyle(ripple, 'border-radius', '50%');
    this.renderer.setStyle(ripple, 'background', 'rgba(255,255,255,0.25)');
    this.renderer.setStyle(ripple, 'transform', 'scale(0)');
    this.renderer.setStyle(ripple, 'animation', 'rippleEffect 0.55s ease-out forwards');
    this.renderer.setStyle(ripple, 'pointer-events', 'none');

    this.renderer.appendChild(el, ripple);
    setTimeout(() => this.renderer.removeChild(el, ripple), 600);
  }
}
