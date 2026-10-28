import { Component, computed, input } from '@angular/core';

/**
 * Shows where a group's min–max range and average sit within the country's overall range.
 * Makes "which roles are paid most, and how wide is the spread" visible at a glance.
 */
@Component({
  selector: 'app-range-bar',
  template: `
    <div class="track" role="img" [attr.aria-label]="label()">
      <div class="range" [style.left.%]="pos().min" [style.width.%]="pos().max - pos().min"></div>
      <div class="avg" [style.left.%]="pos().avg"></div>
    </div>
  `,
  styles: `
    :host { display: block; min-width: 160px; }
    .track {
      position: relative;
      height: 10px;
      border-radius: 5px;
      background: var(--mat-sys-surface-container-high);
    }
    .range {
      position: absolute;
      top: 0;
      bottom: 0;
      border-radius: 5px;
      background: color-mix(in srgb, var(--mat-sys-primary) 45%, transparent);
    }
    .avg {
      position: absolute;
      top: -3px;
      width: 4px;
      height: 16px;
      margin-left: -2px;
      border-radius: 2px;
      background: var(--mat-sys-primary);
    }
  `,
})
export class RangeBar {
  readonly min = input.required<number>();
  readonly avg = input.required<number>();
  readonly max = input.required<number>();
  /** The scale: usually the whole country's min and max. */
  readonly scaleMin = input.required<number>();
  readonly scaleMax = input.required<number>();
  readonly label = input('');

  readonly pos = computed(() => {
    const span = this.scaleMax() - this.scaleMin();
    const percent = (value: number) => (span > 0 ? ((value - this.scaleMin()) / span) * 100 : 0);
    return { min: percent(this.min()), avg: percent(this.avg()), max: percent(this.max()) };
  });
}
