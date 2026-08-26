import { useState } from "react";
import { NavLink } from "react-router-dom";
import { HardDrive, Star, Trash2, LogOut, FolderPlus, Users, Menu, X } from "lucide-react";
import { useAuth } from "../context/AuthContext";

const navItemClass = ({ isActive }) =>
  `flex items-center gap-2.5 px-3 py-2 rounded-md text-sm font-medium transition-colors ${
    isActive ? "bg-ink text-white" : "text-ink/80 hover:bg-black/5"
  }`;

export default function Sidebar({ onNewFolder, onUpload, quotaUsed, quotaTotal }) {
  const { user, logout } = useAuth();
  const [open, setOpen] = useState(false);
  const pct = quotaTotal ? Math.min(100, Math.round((quotaUsed / quotaTotal) * 100)) : 0;

  const closeOnNavigate = () => setOpen(false);

  const content = (
    <>
      <div className="p-5 flex items-center justify-between gap-2">
        <div className="flex items-center gap-2">
          <div className="w-7 h-7 bg-ink folder-tab" />
          <span className="font-display font-semibold text-lg text-ink">Vault</span>
        </div>
        <button className="md:hidden text-muted" onClick={() => setOpen(false)} aria-label="Close menu">
          <X size={20} />
        </button>
      </div>

      <div className="px-4 space-y-2 mb-4">
        <button
          onClick={() => { onUpload(); setOpen(false); }}
          className="w-full bg-brass text-white text-sm font-medium rounded-md py-2 hover:brightness-95 transition"
        >
          Upload file
        </button>
        <button
          onClick={() => { onNewFolder(); setOpen(false); }}
          className="w-full flex items-center justify-center gap-1.5 border border-line text-sm font-medium rounded-md py-2 text-ink hover:bg-black/5 transition"
        >
          <FolderPlus size={15} /> New folder
        </button>
      </div>

      <nav className="px-3 space-y-0.5 flex-1">
        <NavLink to="/" end className={navItemClass} onClick={closeOnNavigate}>
          <HardDrive size={16} /> My Drive
        </NavLink>
        <NavLink to="/shared-with-me" className={navItemClass} onClick={closeOnNavigate}>
          <Users size={16} /> Shared with me
        </NavLink>
        <NavLink to="/starred" className={navItemClass} onClick={closeOnNavigate}>
          <Star size={16} /> Starred
        </NavLink>
        <NavLink to="/trash" className={navItemClass} onClick={closeOnNavigate}>
          <Trash2 size={16} /> Trash
        </NavLink>
      </nav>

      <div className="p-4 border-t border-line">
        <div className="mb-3">
          <div className="h-1.5 rounded-full bg-line overflow-hidden">
            <div className="h-full bg-teal" style={{ width: `${pct}%` }} />
          </div>
          <p className="text-xs font-mono text-muted mt-1.5">
            {formatBytes(quotaUsed)} of {formatBytes(quotaTotal)} used
          </p>
        </div>
        <div className="flex items-center justify-between">
          <div className="min-w-0">
            <p className="text-sm font-medium text-ink truncate">{user?.name}</p>
            <p className="text-xs text-muted truncate">{user?.email}</p>
          </div>
          <button onClick={logout} className="text-muted hover:text-ink shrink-0 ml-2" title="Log out">
            <LogOut size={16} />
          </button>
        </div>
      </div>
    </>
  );

  return (
    <>
      <button
        onClick={() => setOpen(true)}
        className="md:hidden fixed top-3 left-3 z-40 p-2 rounded-md bg-white border border-line shadow-sm text-ink"
        aria-label="Open menu"
      >
        <Menu size={20} />
      </button>

      {open && (
        <button
          className="md:hidden fixed inset-0 z-40 bg-black/30"
          onClick={() => setOpen(false)}
          aria-label="Close navigation overlay"
        />
      )}

      <aside className={`z-50 border-r border-line bg-white h-screen flex-col shrink-0
        ${open ? "fixed inset-y-0 left-0 flex w-64 shadow-xl" : "hidden md:flex md:w-60 md:sticky md:top-0"}`}>
        {content}
      </aside>
    </>
  );
}

export function formatBytes(bytes) {
  if (!bytes) return "0 B";
  const units = ["B", "KB", "MB", "GB", "TB"];
  const i = Math.floor(Math.log(bytes) / Math.log(1024));
  return `${(bytes / Math.pow(1024, i)).toFixed(i === 0 ? 0 : 1)} ${units[i]}`;
}
