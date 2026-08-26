import { useEffect, useState, useCallback } from "react";
import { LogOut, Users, Files, HardDrive, FolderTree, Trash2, ShieldCheck } from "lucide-react";
import { useAuth } from "../context/AuthContext";
import {
  fetchAdminDashboard,
  fetchAdminUsers,
  updateAdminUserStatus,
  fetchAdminFiles,
} from "../services/api";
import { formatBytes } from "../components/Sidebar";

function StatCard({ icon, label, value }) {
  return (
    <div className="bg-white border border-line rounded-lg p-4">
      <div className="flex items-center gap-2 text-muted mb-2">
        {icon}
        <span className="text-xs font-mono uppercase tracking-wide">{label}</span>
      </div>
      <p className="font-display text-2xl font-semibold text-ink">{value}</p>
    </div>
  );
}

export default function AdminDashboard() {
  const { user, logout } = useAuth();
  const [stats, setStats] = useState(null);
  const [users, setUsers] = useState([]);
  const [files, setFiles] = useState([]);
  const [tab, setTab] = useState("users"); // users | files
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const load = useCallback(() => {
    setLoading(true);
    setError("");
    Promise.all([fetchAdminDashboard(), fetchAdminUsers(), fetchAdminFiles()])
      .then(([s, u, f]) => {
        setStats(s.data);
        setUsers(u.data);
        setFiles(f.data);
      })
      .catch((err) => setError(err.response?.data?.message || "Could not load admin data."))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const toggleStatus = async (id, currentlyEnabled) => {
    try {
      await updateAdminUserStatus(id, !currentlyEnabled);
      load();
    } catch (err) {
      alert(err.response?.data?.message || "Could not update user status.");
    }
  };

  return (
    <div className="min-h-screen bg-paper">
      <header className="border-b border-line bg-white">
        <div className="max-w-6xl mx-auto px-8 py-4 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="w-7 h-7 bg-ink folder-tab" />
            <span className="font-display font-semibold text-lg text-ink">Vault</span>
            <span className="ml-2 flex items-center gap-1 text-xs font-mono bg-ink/5 text-ink border border-line rounded px-2 py-0.5">
              <ShieldCheck size={12} /> Admin
            </span>
          </div>
          <div className="flex items-center gap-4">
            <span className="text-sm text-muted">{user?.email}</span>
            <button onClick={logout} className="text-muted hover:text-ink" title="Log out">
              <LogOut size={16} />
            </button>
          </div>
        </div>
      </header>

      <main className="max-w-6xl mx-auto px-8 py-8">
        <h1 className="font-display text-xl font-semibold text-ink mb-6">Platform overview</h1>

        {error && <p className="text-sm text-red-600 mb-4">{error}</p>}

        {loading ? (
          <p className="text-sm text-muted">Loading…</p>
        ) : (
          <>
            <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-4 mb-8">
              <StatCard icon={<Users size={14} />} label="Users" value={stats.totalUsers} />
              <StatCard icon={<ShieldCheck size={14} />} label="Admins" value={stats.totalAdmins} />
              <StatCard icon={<Files size={14} />} label="Files" value={stats.totalFiles} />
              <StatCard icon={<FolderTree size={14} />} label="Folders" value={stats.totalFolders} />
              <StatCard icon={<HardDrive size={14} />} label="Storage used" value={formatBytes(stats.totalStorageUsedBytes)} />
            </div>

            <div className="flex gap-1 mb-4 border-b border-line">
              <button
                onClick={() => setTab("users")}
                className={`px-4 py-2 text-sm font-medium border-b-2 -mb-px ${
                  tab === "users" ? "border-ink text-ink" : "border-transparent text-muted"
                }`}
              >
                Users
              </button>
              <button
                onClick={() => setTab("files")}
                className={`px-4 py-2 text-sm font-medium border-b-2 -mb-px ${
                  tab === "files" ? "border-ink text-ink" : "border-transparent text-muted"
                }`}
              >
                Files
              </button>
            </div>

            {tab === "users" && (
              <div className="bg-white border border-line rounded-lg overflow-hidden">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="text-left text-xs font-mono uppercase tracking-wide text-muted border-b border-line">
                      <th className="px-4 py-3 font-medium">Name</th>
                      <th className="px-4 py-3 font-medium">Email</th>
                      <th className="px-4 py-3 font-medium">Role</th>
                      <th className="px-4 py-3 font-medium">Storage</th>
                      <th className="px-4 py-3 font-medium">Status</th>
                      <th className="px-4 py-3 font-medium text-right">Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {users.map((u) => (
                      <tr key={u.id} className="border-b border-line last:border-0">
                        <td className="px-4 py-3 text-ink font-medium">{u.name}</td>
                        <td className="px-4 py-3 text-muted">{u.email}</td>
                        <td className="px-4 py-3">
                          <span className="text-xs font-mono bg-black/5 rounded px-2 py-0.5">{u.role}</span>
                        </td>
                        <td className="px-4 py-3 font-mono text-xs text-muted">
                          {formatBytes(u.storageUsedBytes)} / {formatBytes(u.storageQuotaBytes)}
                        </td>
                        <td className="px-4 py-3">
                          <span
                            className={`text-xs font-medium rounded px-2 py-0.5 ${
                              u.enabled ? "bg-teal/10 text-teal" : "bg-red-50 text-red-600"
                            }`}
                          >
                            {u.enabled ? "Active" : "Disabled"}
                          </span>
                        </td>
                        <td className="px-4 py-3 text-right">
                          <button
                            onClick={() => toggleStatus(u.id, u.enabled)}
                            disabled={u.id === user?.id}
                            className="text-xs font-medium border border-line rounded px-3 py-1.5 text-ink hover:bg-black/5 disabled:opacity-40 disabled:cursor-not-allowed"
                            title={u.id === user?.id ? "You can't disable your own account" : ""}
                          >
                            {u.enabled ? "Disable" : "Enable"}
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}

            {tab === "files" && (
              <div className="bg-white border border-line rounded-lg overflow-hidden">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="text-left text-xs font-mono uppercase tracking-wide text-muted border-b border-line">
                      <th className="px-4 py-3 font-medium">Name</th>
                      <th className="px-4 py-3 font-medium">Owner</th>
                      <th className="px-4 py-3 font-medium">Size</th>
                      <th className="px-4 py-3 font-medium">Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {files.map((f) => (
                      <tr key={f.id} className="border-b border-line last:border-0">
                        <td className="px-4 py-3 text-ink font-medium">{f.name}</td>
                        <td className="px-4 py-3 text-muted">{f.ownerEmail}</td>
                        <td className="px-4 py-3 font-mono text-xs text-muted">{formatBytes(f.sizeBytes)}</td>
                        <td className="px-4 py-3">
                          {f.trashed ? (
                            <span className="text-xs font-medium rounded px-2 py-0.5 bg-red-50 text-red-600 flex items-center gap-1 w-fit">
                              <Trash2 size={11} /> Trashed
                            </span>
                          ) : (
                            <span className="text-xs font-medium rounded px-2 py-0.5 bg-teal/10 text-teal">Active</span>
                          )}
                        </td>
                      </tr>
                    ))}
                    {files.length === 0 && (
                      <tr>
                        <td colSpan={4} className="px-4 py-6 text-center text-muted text-sm">
                          No files yet.
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            )}
          </>
        )}
      </main>
    </div>
  );
}
