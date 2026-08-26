import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { Lock, Download, FileWarning } from "lucide-react";
import api from "../services/api";
import { formatBytes } from "../components/Sidebar";

export default function PublicLinkAccess() {
  const { token } = useParams();
  const [preview, setPreview] = useState(null);
  const [error, setError] = useState("");
  const [password, setPassword] = useState("");
  const [unlocked, setUnlocked] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Public endpoint — deliberately uses the raw api client without an auth token.
    api
      .get(`/public-links/${token}/preview`)
      .then((res) => {
        setPreview(res.data);
        if (!res.data.requiresPassword) setUnlocked(true);
      })
      .catch((err) => setError(err.response?.data?.message || "This link isn't available."))
      .finally(() => setLoading(false));
  }, [token]);

  const tryUnlock = async (e) => {
    e.preventDefault();
    setError("");
    try {
      const res = await api.post(`/public-links/${token}/unlock`, { password });
      if (res.data.unlocked) {
        setUnlocked(true);
      } else {
        setError("Incorrect password.");
      }
    } catch (err) {
      setError(err.response?.data?.message || "Could not verify password.");
    }
  };

  const download = () => {
    const url = `${api.defaults.baseURL}/public-links/${token}/download${
      password ? `?password=${encodeURIComponent(password)}` : ""
    }`;
    window.location.href = url;
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-paper px-4">
      <div className="w-full max-w-sm">
        <div className="flex items-center gap-2 mb-8 justify-center">
          <div className="w-9 h-9 bg-ink folder-tab" />
          <span className="font-display font-semibold text-xl tracking-tight text-ink">Vault</span>
        </div>

        <div className="border border-line bg-white rounded-lg p-8 text-center">
          {loading && <p className="text-sm text-muted">Loading…</p>}

          {!loading && error && !preview && (
            <>
              <FileWarning className="mx-auto mb-3 text-muted" size={28} />
              <p className="text-sm text-ink font-medium">{error}</p>
            </>
          )}

          {!loading && preview && (
            <>
              <p className="font-display text-lg font-semibold text-ink truncate mb-1">{preview.fileName}</p>
              <p className="text-xs font-mono text-muted mb-6">{formatBytes(preview.sizeBytes)}</p>

              {preview.expired ? (
                <p className="text-sm text-red-600">This link has expired.</p>
              ) : !unlocked ? (
                <form onSubmit={tryUnlock} className="space-y-3">
                  <div className="flex items-center gap-2 justify-center text-muted mb-1">
                    <Lock size={14} />
                    <span className="text-xs">Password required</span>
                  </div>
                  <input
                    type="password"
                    autoFocus
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    className="w-full border border-line rounded-md px-3 py-2 text-sm text-center focus:outline-none focus:ring-2 focus:ring-teal/40 focus:border-teal"
                    placeholder="Enter password"
                  />
                  {error && <p className="text-sm text-red-600">{error}</p>}
                  <button
                    type="submit"
                    className="w-full bg-ink text-white rounded-md py-2.5 text-sm font-medium hover:bg-teal transition-colors"
                  >
                    Unlock
                  </button>
                </form>
              ) : (
                <button
                  onClick={download}
                  className="w-full flex items-center justify-center gap-2 bg-brass text-white rounded-md py-2.5 text-sm font-medium hover:brightness-95 transition"
                >
                  <Download size={16} /> Download
                </button>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
}
