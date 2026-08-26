import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Signup() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    setError("");
    setBusy(true);
    try {
      await register(name, email, password);
      navigate("/");
    } catch (err) {
      setError(err.response?.data?.message || "Could not create your account. Please try again.");
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-paper px-4">
      <div className="w-full max-w-sm">
        <div className="flex items-center gap-2 mb-8 justify-center">
          <div className="w-9 h-9 bg-ink folder-tab" />
          <span className="font-display font-semibold text-xl tracking-tight text-ink">Vault</span>
        </div>

        <div className="border border-line bg-white rounded-lg p-8">
          <h1 className="font-display text-2xl font-semibold text-ink mb-1">Create your account</h1>
          <p className="text-muted text-sm mb-6">5 GB of storage, free to start.</p>

          <form onSubmit={submit} className="space-y-4">
            <div>
              <label className="block text-xs font-mono uppercase tracking-wide text-muted mb-1.5">Name</label>
              <input
                required
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="w-full border border-line rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-teal/40 focus:border-teal"
                placeholder="Ada Lovelace"
              />
            </div>
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
            <div>
              <label className="block text-xs font-mono uppercase tracking-wide text-muted mb-1.5">Password</label>
              <input
                type="password"
                required
                minLength={8}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full border border-line rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-teal/40 focus:border-teal"
                placeholder="At least 8 characters"
              />
            </div>

            {error && <p className="text-sm text-red-600">{error}</p>}

            <button
              type="submit"
              disabled={busy}
              className="w-full bg-ink text-white rounded-md py-2.5 text-sm font-medium hover:bg-teal transition-colors disabled:opacity-60"
            >
              {busy ? "Creating account…" : "Create account"}
            </button>
          </form>
        </div>

        <p className="text-center text-sm text-muted mt-6">
          Already have an account?{" "}
          <Link to="/login" className="text-teal font-medium hover:underline">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  );
}
