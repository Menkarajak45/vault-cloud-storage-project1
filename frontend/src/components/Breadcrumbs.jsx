import { ChevronRight } from "lucide-react";

export default function Breadcrumbs({ trail, onNavigate }) {
  return (
    <div className="flex items-center gap-1.5 text-sm mb-5 flex-wrap">
      {trail.map((crumb, i) => (
        <span key={crumb.id ?? "root"} className="flex items-center gap-1.5">
          <button
            onClick={() => onNavigate(crumb.id)}
            className={`font-medium ${
              i === trail.length - 1 ? "text-ink" : "text-muted hover:text-teal"
            } transition-colors`}
          >
            {crumb.name}
          </button>
          {i < trail.length - 1 && <ChevronRight size={14} className="text-muted" />}
        </span>
      ))}
    </div>
  );
}
