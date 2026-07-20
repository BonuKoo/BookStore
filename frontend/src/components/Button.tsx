import type { ButtonHTMLAttributes, ReactNode } from 'react';

type Variant = 'primary' | 'ghost' | 'danger' | 'default';
type Size = 'sm' | 'md' | 'lg';

interface Props extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant;
  size?: Size;
  full?: boolean;
  children: ReactNode;
}

const VARIANT_CLASS: Record<Variant, string> = {
  primary: 'btn-primary',
  ghost: 'btn-ghost',
  danger: 'btn-danger',
  default: '',
};

const SIZE_CLASS: Record<Size, string> = { sm: 'btn-sm', md: '', lg: 'btn-lg' };

/**
 * 공용 버튼 — variant/size/full로 표현을 정하고 나머지 button 속성(onClick/disabled/
 * type/aria-label)은 그대로 통과시킨다. 클래스는 index.css의 디자인 시스템과 연결된다.
 * (REACT_GUIDELINE §6 — variant prop 패턴)
 */
export default function Button({
  variant = 'default',
  size = 'md',
  full = false,
  className,
  children,
  ...rest
}: Props) {
  const classes = ['btn', VARIANT_CLASS[variant], SIZE_CLASS[size], full ? 'full' : '', className ?? '']
    .filter(Boolean)
    .join(' ');

  return (
    <button className={classes} {...rest}>
      {children}
    </button>
  );
}
