import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    setError("");
    setBusy(true);

    try {
      const loggedInUser = await login(email, password);
      navigate(
        loggedInUser.role === "ADMIN"
          ? "/admin/dashboard"
          : "/"
      );
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "Could not sign in. Check your details and try again."
      );
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-paper px-4">
      <div className="w-full max-w-sm">

        {/* Logo */}
        <div className="flex items-center gap-2 mb-8 justify-center">
          <div className="w-9 h-9 bg-ink folder-tab" />
          <span className="font-display font-semibold text-xl tracking-tight text-ink">
            Vault
          </span>
        </div>

        {/* Login Card */}
        <div className="border border-line bg-white rounded-lg p-8">

          <h1 className="font-display text-2xl font-semibold text-ink mb-1">
            Welcome back
          </h1>

          <p className="text-muted text-sm mb-6">
            Sign in to reach your files.
          </p>

          <form onSubmit={submit} className="space-y-4">

            {/* Email */}
            <div>
              <label className="block text-xs font-mono uppercase tracking-wide text-muted mb-1.5">
                Email
              </label>

              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full border border-line rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-teal/40 focus:border-teal"
                placeholder="you@example.com"
              />
            </div>

            {/* Password */}
            <div>
              <label className="block text-xs font-mono uppercase tracking-wide text-muted mb-1.5">
                Password
              </label>

              <input
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full border border-line rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-teal/40 focus:border-teal"
                placeholder="••••••••"
              />
            </div>

            {/* Error */}
            {error && (
              <p className="text-sm text-red-600">
                {error}
              </p>
            )}

            {/* Sign In */}
            <button
              type="submit"
              disabled={busy}
              className="w-full bg-ink text-white rounded-md py-2.5 text-sm font-medium hover:bg-teal transition-colors disabled:opacity-60"
            >
              {busy ? "Signing in…" : "Sign in"}
            </button>

            {/* Forgot Password */}
            <Link
              to="/forgot-password"
              className="block text-center text-xs text-teal font-medium hover:underline"
            >
              Forgot password?
            </Link>

          </form>
        </div>

        {/* Signup */}
        <p className="text-center text-sm text-muted mt-6">
          New here?{" "}
          <Link
            to="/signup"
            className="text-teal font-medium hover:underline"
          >
            Create an account
          </Link>
        </p>

      </div>
    </div>
  );
}