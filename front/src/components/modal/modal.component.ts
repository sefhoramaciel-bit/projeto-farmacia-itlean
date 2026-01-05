import { Component, ChangeDetectionStrategy, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-modal',
  templateUrl: './modal.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
})
export class ModalComponent {
  isOpen = input.required<boolean>();
  title = input<string>('Modal Title');
  closeModal = output<void>();

  onClose() {
    this.closeModal.emit();
  }
}
