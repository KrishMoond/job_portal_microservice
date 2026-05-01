import { Injectable, ComponentRef, ViewContainerRef, Type, inject, ApplicationRef, createComponent, EnvironmentInjector } from '@angular/core';
import { Subject } from 'rxjs';

export interface ModalConfig {
  closeOnBackdropClick?: boolean;
  closeOnEscape?: boolean;
  size?: 'sm' | 'md' | 'lg' | 'xl' | 'full';
}

@Injectable({
  providedIn: 'root'
})
export class ModalService {
  private appRef = inject(ApplicationRef);
  private injector = inject(EnvironmentInjector);
  private activeModals: ComponentRef<any>[] = [];
  private backdropElement: HTMLElement | null = null;

  open<T>(component: Type<T>, config: ModalConfig = {}): { close: () => void; onClose: Subject<any> } {
    const closeSubject = new Subject<any>();
    
    // Create backdrop
    this.createBackdrop(config);

    // Create modal component
    const componentRef = createComponent(component, {
      environmentInjector: this.injector
    });

    // Create modal wrapper
    const modalWrapper = document.createElement('div');
    modalWrapper.className = `modal modal-${config.size || 'md'}`;
    modalWrapper.appendChild(componentRef.location.nativeElement);
    document.body.appendChild(modalWrapper);

    // Attach to app
    this.appRef.attachView(componentRef.hostView);
    this.activeModals.push(componentRef);

    // Handle escape key
    if (config.closeOnEscape !== false) {
      const escapeHandler = (e: KeyboardEvent) => {
        if (e.key === 'Escape') {
          close();
        }
      };
      document.addEventListener('keydown', escapeHandler);
      
      closeSubject.subscribe(() => {
        document.removeEventListener('keydown', escapeHandler);
      });
    }

    const close = (result?: any) => {
      const index = this.activeModals.indexOf(componentRef);
      if (index > -1) {
        this.activeModals.splice(index, 1);
      }

      this.appRef.detachView(componentRef.hostView);
      componentRef.destroy();
      modalWrapper.remove();

      if (this.activeModals.length === 0) {
        this.removeBackdrop();
      }

      closeSubject.next(result);
      closeSubject.complete();
    };

    return { close, onClose: closeSubject };
  }

  closeAll(): void {
    this.activeModals.forEach(modal => {
      this.appRef.detachView(modal.hostView);
      modal.destroy();
    });
    this.activeModals = [];
    this.removeBackdrop();
    document.querySelectorAll('.modal').forEach(el => el.remove());
  }

  private createBackdrop(config: ModalConfig): void {
    if (this.backdropElement) return;

    this.backdropElement = document.createElement('div');
    this.backdropElement.className = 'modal-backdrop';
    
    if (config.closeOnBackdropClick !== false) {
      this.backdropElement.addEventListener('click', () => {
        if (this.activeModals.length > 0) {
          const lastModal = this.activeModals[this.activeModals.length - 1];
          this.appRef.detachView(lastModal.hostView);
          lastModal.destroy();
          this.activeModals.pop();
          
          if (this.activeModals.length === 0) {
            this.removeBackdrop();
          }
        }
      });
    }

    document.body.appendChild(this.backdropElement);
    document.body.style.overflow = 'hidden';
  }

  private removeBackdrop(): void {
    if (this.backdropElement) {
      this.backdropElement.remove();
      this.backdropElement = null;
      document.body.style.overflow = '';
    }
  }
}
