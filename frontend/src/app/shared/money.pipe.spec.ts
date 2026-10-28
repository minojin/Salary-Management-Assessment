import { MoneyPipe } from './money.pipe';

describe('MoneyPipe', () => {
  const pipe = new MoneyPipe();

  it('formats in the given currency without decimals', () => {
    expect(pipe.transform(124504, 'USD')).toBe('$124,504');
    expect(pipe.transform(64645, 'GBP')).toBe('£64,645');
  });

  it('uses Indian digit grouping for INR', () => {
    expect(pipe.transform(1800000, 'INR')).toBe('₹18,00,000');
  });

  it('returns an empty string for missing values', () => {
    expect(pipe.transform(null, 'USD')).toBe('');
    expect(pipe.transform(100, '')).toBe('');
  });
});
