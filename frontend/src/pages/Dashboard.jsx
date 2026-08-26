import { useEffect, useState, useCallback } from "react";
import { useSearchParams } from "react-router-dom";
import { Search } from "lucide-react";
import Sidebar from "../components/Sidebar";
import Breadcrumbs from "../components/Breadcrumbs";
import ExplorerGrid from "../components/ExplorerGrid";
import UploadModal from "../components/UploadModal";
import NewFolderModal from "../components/NewFolderModal";
import ShareModal from "../components/ShareModal";
import { useAuth } from "../context/AuthContext";
import {
  browseFolder,
  createFolder,
  starFolder,
  starFile,
  trashFolder,
  trashFile,
  downloadFile,
  searchFiles,
  searchFolders,
} from "../services/api";

export default function Dashboard() {
  const { user } = useAuth();
  const [params, setParams] = useSearchParams();
  const folderId = params.get("folderId");

  const [data, setData] = useState({ folders: [], files: [], breadcrumbs: [{ id: null, name: "My Drive" }] });
  const [loading, setLoading] = useState(true);
  const [showUpload, setShowUpload] = useState(false);
  const [showNewFolder, setShowNewFolder] = useState(false);
  const [shareTarget, setShareTarget] = useState(null);
  const [query, setQuery] = useState("");
  const [searchResults, setSearchResults] = useState(null);

  const load = useCallback(() => {
    setLoading(true);
    browseFolder(folderId)
      .then((res) => setData(res.data))
      .finally(() => setLoading(false));
  }, [folderId]);

  useEffect(() => {
    load();
  }, [load]);

  const navigate = (id) => setParams(id ? { folderId: id } : {});

  const runSearch = async (q) => {
    setQuery(q);
    if (!q.trim()) {
      setSearchResults(null);
      return;
    }
    const [f1, f2] = await Promise.all([searchFolders(q), searchFiles(q)]);
    setSearchResults({ folders: f1.data, files: f2.data });
  };

  const handleDownload = async (file) => {
    const res = await downloadFile(file.id);
    const url = window.URL.createObjectURL(res.data);
    const a = document.createElement("a");
    a.href = url;
    a.download = file.name;
    a.click();
    window.URL.revokeObjectURL(url);
  };

  const view = searchResults ?? data;

  return (
    <div className="flex bg-paper min-h-screen">
      <Sidebar
        onNewFolder={() => setShowNewFolder(true)}
        onUpload={() => setShowUpload(true)}
        quotaUsed={user?.storageUsedBytes}
        quotaTotal={user?.storageQuotaBytes}
      />

      <main className="flex-1 min-w-0 w-full p-4 pt-16 sm:p-6 sm:pt-16 md:p-8 md:pt-8 max-w-6xl">
        <div className="relative mb-6 w-full max-w-md">
          <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-muted" />
          <input
            value={query}
            onChange={(e) => runSearch(e.target.value)}
            placeholder="Search your drive"
            className="w-full border border-line bg-white rounded-md pl-9 pr-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-teal/40 focus:border-teal"
          />
        </div>

        {!searchResults && <Breadcrumbs trail={data.breadcrumbs} onNavigate={navigate} />}
        {searchResults && <p className="text-sm text-muted mb-5">Search results for "{query}"</p>}

        {loading ? (
          <p className="text-sm text-muted">Loading…</p>
        ) : (
          <ExplorerGrid
            folders={view.folders}
            files={view.files}
            onOpenFolder={navigate}
            onStarFolder={(id, starred) => starFolder(id, starred).then(load)}
            onStarFile={(id, starred) => starFile(id, starred).then(load)}
            onTrashFolder={(id) => trashFolder(id).then(load)}
            onTrashFile={(id) => trashFile(id).then(load)}
            onDownloadFile={handleDownload}
            onShareFile={(file) => setShareTarget(file)}
          />
        )}
      </main>

      {showUpload && (
        <UploadModal folderId={folderId} onClose={() => setShowUpload(false)} onUploaded={load} />
      )}
      {showNewFolder && (
        <NewFolderModal
          onClose={() => setShowNewFolder(false)}
          onCreate={(name) => createFolder(name, folderId).then(load)}
        />
      )}
      {shareTarget && <ShareModal file={shareTarget} onClose={() => setShareTarget(null)} />}
    </div>
  );
}
