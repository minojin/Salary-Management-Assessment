import { Pipe, PipeTransform } from '@angular/core';

const formatters = new Map<string, Intl.NumberFormat>();

/** Formats a whole-unit amount in its own currency, e.g. `1800000 | money:'INR'` -> "₹18,00,000". */
@Pipe({ name: 'money' })
export class MoneyPipe implements PipeTransform {
  transform(amount: number | null | undefined, currency: string): string {
    if (amount == null || !currency) {
      return '';
    }
    return formatter(currency).format(amount);
  }
}

function formatter(currency: string): Intl.NumberFormat {
  let format = formatters.get(currency);
  if (!format) {
    // en-IN gives Indian lakh/crore grouping for INR, which is what an Indian HR team expects.
    const locale = currency === 'INR' ? 'en-IN' : 'en';
    format = new Intl.NumberFormat(locale, { style: 'currency', currency, maximumFractionDigits: 0 });
    formatters.set(currency, format);
  }
  return format;
}
