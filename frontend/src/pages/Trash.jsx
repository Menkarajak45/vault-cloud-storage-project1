import { useEffect, useState, useCallback } from "react";
import Sidebar from "../components/Sidebar";
import ExplorerGrid from "../components/ExplorerGrid";
import { useAuth } from "../context/AuthContext";
import { listTrashedFolders, listTrashedFiles, restoreFolder, restoreFile } from "../services/api";

export default function Trash() {
  const { user } = useAuth();
  const [folders, setFolders] = useState([]);
  const [files, setFiles] = useState([]);
  const [loading, setLoading] = useState(true);

  const load = useCallback(() => {
    setLoading(true);
    Promise.all([listTrashedFolders(), listTrashedFiles()])
      .then(([f1, f2]) => {
        setFolders(f1.data);
        setFiles(f2.data);
      })
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  return (
    <div className="flex bg-paper min-h-screen">
      <Sidebar onNewFolder={() => {}} onUpload={() => {}} quotaUsed={user?.storageUsedBytes} quotaTotal={user?.storageQuotaBytes} />
      <main className="flex-1 min-w-0 w-full p-4 pt-16 sm:p-6 sm:pt-16 md:p-8 md:pt-8 max-w-6xl">
        <div className="flex items-center justify-between mb-2">
          <h1 className="font-display text-xl font-semibold text-ink">Trash</h1>
        </div>
        <p className="text-sm text-muted mb-6">Items here can be restored. Permanent deletion isn't available in this MVP yet.</p>
        {loading ? (
          <p className="text-sm text-muted">Loading…</p>
        ) : (
          <ExplorerGrid
            folders={folders}
            files={files}
            trashedView
            onOpenFolder={() => {}}
            onRestoreFolder={(id) => restoreFolder(id).then(load)}
            onRestoreFile={(id) => restoreFile(id).then(load)}
          />
        )}
      </main>
    </div>
  );
}
