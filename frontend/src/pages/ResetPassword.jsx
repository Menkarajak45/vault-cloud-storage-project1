import { useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { resetPassword } from "../services/api";

export default function ResetPassword() {
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const token = params.get("token") || "";
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [error, setError] = useState("");
  const [done, setDone] = useState(false);
  const [busy, setBusy] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    setError("");
    if (password !== confirm) {
      setError("Passwords do not match.");
      return;
    }
    setBusy(true);
    try {
      await resetPassword({ token, password });
      setDone(true);
      setTimeout(() => navigate("/login"), 1200);
    } catch (err) {
      setError(err.response?.data?.message || "This reset link is invalid or expired.");
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-paper px-4 py-8">
      <div className="w-full max-w-sm">
        <div className="flex items-center gap-2 mb-8 justify-center">
          <div className="w-9 h-9 bg-ink folder-tab" />
          <span className="font-display font-semibold text-xl tracking-tight text-ink">Vault</span>
        </div>
        <div className="border border-line bg-white rounded-lg p-8">
          <h1 className="font-display text-2xl font-semibold text-ink mb-1">Set a new password</h1>
          <p className="text-muted text-sm mb-6">Choose a password with at least 8 characters.</p>

          {!token ? (
            <p className="text-sm text-red-600">This reset link is missing a token.</p>
          ) : (
            <form onSubmit={submit} className="space-y-4">
              <input
                type="password"
                required
                minLength={8}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full border border-line rounded-md px-3 py-2 text-sm"
                placeholder="New password"
              />
              <input
                type="password"
                required
                minLength={8}
                value={confirm}
                onChange={(e) => setConfirm(e.target.value)}
                className="w-full border border-line rounded-md px-3 py-2 text-sm"
                placeholder="Confirm password"
              />
              {error && <p className="text-sm text-red-600">{error}</p>}
              {done && <p className="text-sm text-teal">Password changed. Redirecting to sign in…</p>}
              <button
                type="submit"
                disabled={busy || done}
                className="w-full bg-ink text-white rounded-md py-2.5 text-sm font-medium hover:bg-teal transition-colors disabled:opacity-60"
              >
                {busy ? "Updating…" : "Update password"}
              </button>
            </form>
          )}
        </div>
        <p className="text-center text-sm text-muted mt-6">
          <Link to="/login" className="text-teal font-medium hover:underline">Back to sign in</Link>
        </p>
      </div>
    </div>
  );
}
