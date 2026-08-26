import { useCallback, useState } from "react";
import { useDropzone } from "react-dropzone";
import { UploadCloud, X } from "lucide-react";
import { uploadFile } from "../services/api";

export default function UploadModal({ folderId, onClose, onUploaded }) {
  const [items, setItems] = useState([]); // { file, progress, done, error }

  const onDrop = useCallback(
    (accepted) => {
      const newItems = accepted.map((file) => ({ file, progress: 0, done: false, error: null }));
      setItems((prev) => [...prev, ...newItems]);

      newItems.forEach((item, localIdx) => {
        uploadFile(item.file, folderId, (evt) => {
          const progress = Math.round((evt.loaded * 100) / evt.total);
          setItems((prev) =>
            prev.map((it) => (it.file === item.file ? { ...it, progress } : it))
          );
        })
          .then(() => {
            setItems((prev) => prev.map((it) => (it.file === item.file ? { ...it, done: true } : it)));
            onUploaded();
          })
          .catch((err) => {
            setItems((prev) =>
              prev.map((it) =>
                it.file === item.file
                  ? { ...it, error: err.response?.data?.message || "Upload failed" }
                  : it
              )
            );
          });
      });
    },
    [folderId, onUploaded]
  );

  const { getRootProps, getInputProps, isDragActive } = useDropzone({ onDrop });

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 px-4">
      <div className="bg-white rounded-lg w-full max-w-md border border-line">
        <div className="flex items-center justify-between px-5 py-4 border-b border-line">
          <h2 className="font-display font-semibold text-ink">Upload files</h2>
          <button onClick={onClose} className="text-muted hover:text-ink">
            <X size={18} />
          </button>
        </div>

        <div className="p-5">
          <div
            {...getRootProps()}
            className={`border-2 border-dashed rounded-lg py-10 text-center cursor-pointer transition-colors ${
              isDragActive ? "border-teal bg-teal/5" : "border-line hover:border-teal/60"
            }`}
          >
            <input {...getInputProps()} />
            <UploadCloud className="mx-auto mb-2 text-muted" size={28} />
            <p className="text-sm text-ink font-medium">Drop files here, or click to browse</p>
            <p className="text-xs text-muted mt-1">Up to 200MB per file</p>
          </div>

          {items.length > 0 && (
            <div className="mt-4 space-y-2 max-h-48 overflow-y-auto">
              {items.map((it, i) => (
                <div key={i} className="text-sm">
                  <div className="flex justify-between mb-1">
                    <span className="truncate text-ink">{it.file.name}</span>
                    <span className="text-xs font-mono text-muted ml-2 shrink-0">
                      {it.error ? "Failed" : it.done ? "Done" : `${it.progress}%`}
                    </span>
                  </div>
                  <div className="h-1.5 bg-line rounded-full overflow-hidden">
                    <div
                      className={`h-full transition-all ${it.error ? "bg-red-500" : "bg-brass"}`}
                      style={{ width: `${it.error ? 100 : it.progress}%` }}
                    />
                  </div>
                  {it.error && <p className="text-xs text-red-600 mt-1">{it.error}</p>}
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="px-5 py-4 border-t border-line flex justify-end">
          <button
            onClick={onClose}
            className="text-sm font-medium bg-ink text-white rounded-md px-4 py-2 hover:bg-teal transition-colors"
          >
            Done
          </button>
        </div>
      </div>
    </div>
  );
}
