interface Props {
  value: number;
  min?: number;
  disabled?: boolean;
  onChange: (next: number) => void;
}

/** 수량 스테퍼 — 최종 수량(absolute)을 onChange로 전달 */
export default function QuantityStepper({ value, min = 1, disabled, onChange }: Props) {
  return (
    <div className="stepper">
      <button
        className="btn btn-sm"
        disabled={disabled || value <= min}
        onClick={() => onChange(value - 1)}
        aria-label="수량 감소"
      >
        −
      </button>
      <span className="stepper-value">{value}</span>
      <button
        className="btn btn-sm"
        disabled={disabled}
        onClick={() => onChange(value + 1)}
        aria-label="수량 증가"
      >
        +
      </button>
    </div>
  );
}
