import { useEffect, useState, useCallback } from "react";
import Sidebar from "../components/Sidebar";
import ExplorerGrid from "../components/ExplorerGrid";
import { useAuth } from "../context/AuthContext";
import { listStarredFolders, listStarredFiles, starFolder, starFile, trashFolder, trashFile, downloadFile } from "../services/api";

export default function Starred() {
  const { user } = useAuth();
  const [folders, setFolders] = useState([]);
  const [files, setFiles] = useState([]);
  const [loading, setLoading] = useState(true);

  const load = useCallback(() => {
    setLoading(true);
    Promise.all([listStarredFolders(), listStarredFiles()])
      .then(([f1, f2]) => {
        setFolders(f1.data);
        setFiles(f2.data);
      })
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const handleDownload = async (file) => {
    const res = await downloadFile(file.id);
    const url = window.URL.createObjectURL(res.data);
    const a = document.createElement("a");
    a.href = url;
    a.download = file.name;
    a.click();
    window.URL.revokeObjectURL(url);
  };

  return (
    <div className="flex bg-paper min-h-screen">
      <Sidebar onNewFolder={() => {}} onUpload={() => {}} quotaUsed={user?.storageUsedBytes} quotaTotal={user?.storageQuotaBytes} />
      <main className="flex-1 min-w-0 w-full p-4 pt-16 sm:p-6 sm:pt-16 md:p-8 md:pt-8 max-w-6xl">
        <h1 className="font-display text-xl font-semibold text-ink mb-6">Starred</h1>
        {loading ? (
          <p className="text-sm text-muted">Loading…</p>
        ) : (
          <ExplorerGrid
            folders={folders}
            files={files}
            onOpenFolder={() => {}}
            onStarFolder={(id, starred) => starFolder(id, starred).then(load)}
            onStarFile={(id, starred) => starFile(id, starred).then(load)}
            onTrashFolder={(id) => trashFolder(id).then(load)}
            onTrashFile={(id) => trashFile(id).then(load)}
            onDownloadFile={handleDownload}
          />
        )}
      </main>
    </div>
  );
}
