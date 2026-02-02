import './Skeleton.css';

export function SkeletonCard() {
  return (
    <div className="skeleton-card">
      <div className="skeleton-header">
        <div className="skeleton-circle"></div>
        <div className="skeleton-line small"></div>
      </div>
      <div className="skeleton-line large"></div>
      <div className="skeleton-number"></div>
      <div className="skeleton-line medium"></div>
    </div>
  );
}

export function SkeletonChart() {
  return (
    <div className="skeleton-chart">
      <div className="skeleton-line medium"></div>
      <div className="skeleton-chart-bars">
        {[...Array(12)].map((_, i) => (
          <div 
            key={i} 
            className="skeleton-bar" 
            style={{ height: `${20 + Math.random() * 60}%` }}
          ></div>
        ))}
      </div>
    </div>
  );
}

export function SkeletonList({ items = 5 }) {
  return (
    <div className="skeleton-list">
      {[...Array(items)].map((_, i) => (
        <div key={i} className="skeleton-list-item">
          <div className="skeleton-line flex-1"></div>
          <div className="skeleton-line small"></div>
        </div>
      ))}
    </div>
  );
}
