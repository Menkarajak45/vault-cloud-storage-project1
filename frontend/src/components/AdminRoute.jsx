import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

// Frontend gating only affects what's rendered — it is NOT the source of truth.
// Every /api/admin/** call is independently re-checked by Spring Security
// (hasRole("ADMIN")) on the backend, so this guard is purely a UX convenience.
export default function AdminRoute({ children }) {
  const { user, loading } = useAuth();

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-paper">
        <p className="text-sm text-muted">Loading…</p>
      </div>
    );
  }

  if (!user) return <Navigate to="/login" replace />;
  if (user.role !== "ADMIN") return <Navigate to="/" replace />;

  return children;
}
