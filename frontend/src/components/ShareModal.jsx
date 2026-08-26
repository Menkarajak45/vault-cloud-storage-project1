import { useEffect, useState } from "react";
import { X, Link2, Trash2, Copy } from "lucide-react";
import {
  createShare,
  listSharesForFile,
  revokeShare,
  createPublicLink,
  listPublicLinksForFile,
  revokePublicLink,
  changePublicLinkPassword,
} from "../services/api";

export default function ShareModal({ file, onClose }) {
  const [email, setEmail] = useState("");
  const [permission, setPermission] = useState("VIEWER");
  const [shares, setShares] = useState([]);
  const [links, setLinks] = useState([]);
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const [linkPassword, setLinkPassword] = useState("");
  const [linkExpiry, setLinkExpiry] = useState("");
  const [editingLinkId, setEditingLinkId] = useState(null);
  const [newLinkPassword, setNewLinkPassword] = useState("");

  const load = () => {
    listSharesForFile(file.id).then((res) => setShares(res.data));
    listPublicLinksForFile(file.id).then((res) => setLinks(res.data));
  };

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [file.id]);

  const submitShare = async (e) => {
    e.preventDefault();
    setError("");
    setBusy(true);
    try {
      await createShare(file.id, email.trim(), permission);
      setEmail("");
      load();
    } catch (err) {
      setError(err.response?.data?.message || "Could not share this file.");
    } finally {
      setBusy(false);
    }
  };

  const doRevokeShare = async (shareId) => {
    await revokeShare(shareId);
    load();
  };

  const makeLink = async () => {
    setError("");
    try {
      await createPublicLink(file.id, linkPassword || null, linkExpiry ? Number(linkExpiry) : null);
      setLinkPassword("");
      setLinkExpiry("");
      load();
    } catch (err) {
      setError(err.response?.data?.message || "Could not create link.");
    }
  };

  const doRevokeLink = async (id) => {
    await revokePublicLink(id);
    setEditingLinkId(null);
    setNewLinkPassword("");
    load();
  };

  const saveLinkPassword = async (id) => {
    setError("");
    setBusy(true);
    try {
      await changePublicLinkPassword(id, newLinkPassword);
      setEditingLinkId(null);
      setNewLinkPassword("");
      load();
    } catch (err) {
      setError(err.response?.data?.message || "Could not update password.");
    } finally {
      setBusy(false);
    }
  };

  const linkUrl = (token) => `${window.location.origin}/shared/${token}`;

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 px-4">
      <div className="bg-white rounded-lg w-full max-w-md border border-line max-h-[85vh] overflow-y-auto">
        <div className="flex items-center justify-between px-5 py-4 border-b border-line sticky top-0 bg-white">
          <div>
            <h2 className="font-display font-semibold text-ink">Share</h2>
            <p className="text-xs text-muted truncate max-w-[280px]">{file.name}</p>
          </div>
          <button onClick={onClose} className="text-muted hover:text-ink">
            <X size={18} />
          </button>
        </div>

        <div className="p-5">
          <form onSubmit={submitShare} className="flex gap-2 mb-4">
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="Person's email"
              className="flex-1 border border-line rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-teal/40 focus:border-teal"
            />
            <select
              value={permission}
              onChange={(e) => setPermission(e.target.value)}
              className="border border-line rounded-md px-2 py-2 text-sm bg-white"
            >
              <option value="VIEWER">Viewer</option>
              <option value="EDITOR">Editor</option>
            </select>
            <button
              type="submit"
              disabled={busy}
              className="bg-ink text-white text-sm font-medium rounded-md px-3 py-2 hover:bg-teal transition-colors disabled:opacity-60"
            >
              Share
            </button>
          </form>

          {error && <p className="text-sm text-red-600 mb-3">{error}</p>}

          {shares.length > 0 && (
            <div className="mb-6">
              <p className="text-xs font-mono uppercase tracking-wide text-muted mb-2">People with access</p>
              <div className="space-y-1.5">
                {shares.map((s) => (
                  <div key={s.id} className="flex items-center justify-between text-sm bg-paper rounded-md px-3 py-2">
                    <div className="min-w-0">
                      <p className="text-ink font-medium truncate">{s.sharedWithName}</p>
                      <p className="text-xs text-muted truncate">{s.sharedWithEmail}</p>
                    </div>
                    <div className="flex items-center gap-2 shrink-0">
                      <span className="text-xs font-mono bg-white border border-line rounded px-2 py-0.5">
                        {s.permission}
                      </span>
                      <button onClick={() => doRevokeShare(s.id)} className="text-muted hover:text-red-600">
                        <Trash2 size={14} />
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          <div>
            <p className="text-xs font-mono uppercase tracking-wide text-muted mb-2">Public link</p>

            {links.length === 0 ? (
              <div className="space-y-2">
                <input
                  type="password"
                  value={linkPassword}
                  onChange={(e) => setLinkPassword(e.target.value)}
                  placeholder="Optional password"
                  className="w-full border border-line rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-teal/40 focus:border-teal"
                />
                <input
                  type="number"
                  min="1"
                  value={linkExpiry}
                  onChange={(e) => setLinkExpiry(e.target.value)}
                  placeholder="Expires in N hours (optional)"
                  className="w-full border border-line rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-teal/40 focus:border-teal"
                />
                <button
                  onClick={makeLink}
                  className="w-full flex items-center justify-center gap-1.5 border border-line rounded-md py-2 text-sm font-medium text-ink hover:bg-black/5 transition"
                >
                  <Link2 size={14} /> Create public link
                </button>
              </div>
            ) : (
              <div className="space-y-2">
                {links.map((l) => (
                  <div key={l.id} className="bg-paper rounded-md p-3">
                    <div className="flex items-center gap-2 mb-1">
                      <input
                        readOnly
                        value={linkUrl(l.token)}
                        className="flex-1 text-xs font-mono bg-white border border-line rounded px-2 py-1.5 truncate"
                      />
                      <button
                        onClick={() => navigator.clipboard.writeText(linkUrl(l.token))}
                        className="text-muted hover:text-ink"
                        title="Copy link"
                      >
                        <Copy size={14} />
                      </button>
                      <button onClick={() => doRevokeLink(l.id)} className="text-muted hover:text-red-600" title="Revoke">
                        <Trash2 size={14} />
                      </button>
                    </div>
                    <p className="text-xs text-muted">
                      {l.hasPassword ? "Password protected" : "No password"}
                      {l.expiresAt ? ` · Expires ${new Date(l.expiresAt).toLocaleString()}` : " · Never expires"}
                    </p>
                    {editingLinkId === l.id ? (
                      <div className="mt-2 space-y-2">
                        <input type="password" value={newLinkPassword} onChange={(e) => setNewLinkPassword(e.target.value)}
                          placeholder="New password (blank = remove)" className="w-full border border-line rounded-md px-3 py-2 text-sm" />
                        <div className="flex gap-2">
                          <button disabled={busy} onClick={() => saveLinkPassword(l.id)} className="flex-1 bg-ink text-white rounded-md py-2 text-xs font-medium disabled:opacity-50">Save</button>
                          <button onClick={() => { setEditingLinkId(null); setNewLinkPassword(""); }} className="px-3 border border-line rounded-md text-xs">Cancel</button>
                        </div>
                        <p className="text-[11px] text-muted">Leave blank to remove password protection.</p>
                      </div>
                    ) : (
                      <button onClick={() => { setEditingLinkId(l.id); setNewLinkPassword(""); }} className="mt-2 text-xs text-teal font-medium hover:underline">
                        {l.hasPassword ? "Change / remove password" : "Add password"}
                      </button>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
