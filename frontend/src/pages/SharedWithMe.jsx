import { useEffect, useState, useCallback } from "react";
import Sidebar from "../components/Sidebar";
import { useAuth } from "../context/AuthContext";
import { listSharedWithMe, downloadFile } from "../services/api";
import { formatBytes } from "../components/Sidebar";

function extensionOf(name) {
  const parts = name.split(".");
  return parts.length > 1 ? parts.pop().toUpperCase().slice(0, 4) : "FILE";
}

export default function SharedWithMe() {
  const { user } = useAuth();
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);

  const load = useCallback(() => {
    setLoading(true);
    listSharedWithMe()
      .then((res) => setItems(res.data))
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
        <h1 className="font-display text-xl font-semibold text-ink mb-6">Shared with me</h1>

        {loading ? (
          <p className="text-sm text-muted">Loading…</p>
        ) : items.length === 0 ? (
          <div className="border border-dashed border-line rounded-lg py-20 text-center">
            <p className="text-muted text-sm">Nothing has been shared with you yet.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-3 sm:gap-4">
            {items.map((item) => (
              <div
                key={item.shareId}
                onClick={() => handleDownload(item.file)}
                className="cursor-pointer border border-line bg-white rounded-lg p-4 hover:border-teal transition-colors"
              >
                <div className="flex items-start justify-between mb-3">
                  <span className="font-mono text-[10px] font-medium tracking-wide bg-ink text-white rounded px-2 py-1">
                    {extensionOf(item.file.name)}
                  </span>
                  <span className="text-xs font-mono bg-black/5 rounded px-2 py-0.5">{item.permission}</span>
                </div>
                <p className="text-sm font-medium text-ink truncate" title={item.file.name}>
                  {item.file.name}
                </p>
                <p className="text-xs font-mono text-muted mt-1">{formatBytes(item.file.sizeBytes)}</p>
                <p className="text-xs text-muted mt-1">Shared by {item.ownerEmail}</p>
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
