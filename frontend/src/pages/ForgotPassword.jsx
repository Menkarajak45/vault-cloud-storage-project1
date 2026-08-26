import { useState } from "react";
import { Link } from "react-router-dom";
import { forgotPassword } from "../services/api";

export default function ForgotPassword() {
  const [email, setEmail] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    setMessage("");
    setError("");
    setBusy(true);
    try {
      await forgotPassword({ email });
      setMessage("If an account exists for this email, a password reset link has been sent.");
    } catch (err) {
      setError(err.response?.data?.message || "Could not process your request.");
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
          <h1 className="font-display text-2xl font-semibold text-ink mb-1">Forgot password?</h1>
          <p className="text-muted text-sm mb-6">Enter your email and we'll send a reset link.</p>

          <form onSubmit={submit} className="space-y-4">
            <div>
              <label className="block text-xs font-mono uppercase tracking-wide text-muted mb-1.5">Email</label>
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full border border-line rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-teal/40 focus:border-teal"
                placeholder="you@example.com"
              />
            </div>
            {message && <p className="text-sm text-teal">{message}</p>}
            {error && <p className="text-sm text-red-600">{error}</p>}
            <button
              type="submit"
              disabled={busy}
              className="w-full bg-ink text-white rounded-md py-2.5 text-sm font-medium hover:bg-teal transition-colors disabled:opacity-60"
            >
              {busy ? "Sending…" : "Send reset link"}
            </button>
          </form>
        </div>
        <p className="text-center text-sm text-muted mt-6">
          <Link to="/login" className="text-teal font-medium hover:underline">Back to sign in</Link>
        </p>
      </div>
    </div>
  );
}
