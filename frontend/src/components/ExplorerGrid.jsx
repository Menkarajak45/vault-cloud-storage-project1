import { Star, MoreVertical, Trash2, RotateCcw, Download, Share2 } from "lucide-react";
import { useState } from "react";
import { formatBytes } from "./Sidebar";

function extensionOf(name) {
  const parts = name.split(".");
  return parts.length > 1 ? parts.pop().toUpperCase().slice(0, 4) : "FILE";
}

function ItemMenu({ actions }) {
  const [open, setOpen] = useState(false);
  return (
    <div className="relative">
      <button
        onClick={(e) => {
          e.stopPropagation();
          setOpen((o) => !o);
        }}
        className="p-1 rounded hover:bg-black/5 text-muted"
      >
        <MoreVertical size={16} />
      </button>
      {open && (
        <div
          onMouseLeave={() => setOpen(false)}
          className="absolute right-0 top-7 z-10 bg-white border border-line rounded-md shadow-lg py-1 w-40"
        >
          {actions.map((a) => (
            <button
              key={a.label}
              onClick={(e) => {
                e.stopPropagation();
                a.onClick();
                setOpen(false);
              }}
              className="w-full text-left px-3 py-1.5 text-sm text-ink hover:bg-black/5 flex items-center gap-2"
            >
              {a.icon} {a.label}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}

export default function ExplorerGrid({
  folders,
  files,
  onOpenFolder,
  onStarFolder,
  onStarFile,
  onTrashFolder,
  onTrashFile,
  onRestoreFolder,
  onRestoreFile,
  onDownloadFile,
  onShareFile,
  trashedView = false,
}) {
  const empty = folders.length === 0 && files.length === 0;

  if (empty) {
    return (
      <div className="border border-dashed border-line rounded-lg py-20 text-center">
        <p className="text-muted text-sm">
          {trashedView ? "Trash is empty." : "Nothing here yet. Upload a file or create a folder to get started."}
        </p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-3 sm:gap-4">
      {folders.map((folder) => (
        <div
          key={folder.id}
          onClick={() => !trashedView && onOpenFolder(folder.id)}
          className={`group border border-line bg-white rounded-lg p-4 ${
            !trashedView ? "cursor-pointer hover:border-teal" : ""
          } transition-colors`}
        >
          <div className="flex items-start justify-between mb-3">
            <div className="w-10 h-8 bg-brass/90 folder-tab" />
            <ItemMenu
              actions={
                trashedView
                  ? [{ label: "Restore", icon: <RotateCcw size={14} />, onClick: () => onRestoreFolder(folder.id) }]
                  : [
                      {
                        label: folder.starred ? "Unstar" : "Star",
                        icon: <Star size={14} />,
                        onClick: () => onStarFolder(folder.id, !folder.starred),
                      },
                      { label: "Move to Trash", icon: <Trash2 size={14} />, onClick: () => onTrashFolder(folder.id) },
                    ]
              }
            />
          </div>
          <p className="text-sm font-medium text-ink truncate">{folder.name}</p>
          <p className="text-xs font-mono text-muted mt-1">Folder</p>
        </div>
      ))}

      {files.map((file) => (
        <div
          key={file.id}
          className="group border border-line bg-white rounded-lg p-4 hover:border-teal transition-colors"
        >
          <div className="flex items-start justify-between mb-3">
            <span className="font-mono text-[10px] font-medium tracking-wide bg-ink text-white rounded px-2 py-1">
              {extensionOf(file.name)}
            </span>
            <ItemMenu
              actions={
                trashedView
                  ? [{ label: "Restore", icon: <RotateCcw size={14} />, onClick: () => onRestoreFile(file.id) }]
                  : [
                      { label: "Download", icon: <Download size={14} />, onClick: () => onDownloadFile(file) },
                      ...(onShareFile ? [{ label: "Share", icon: <Share2 size={14} />, onClick: () => onShareFile(file) }] : []),
                      {
                        label: file.starred ? "Unstar" : "Star",
                        icon: <Star size={14} />,
                        onClick: () => onStarFile(file.id, !file.starred),
                      },
                      { label: "Move to Trash", icon: <Trash2 size={14} />, onClick: () => onTrashFile(file.id) },
                    ]
              }
            />
          </div>
          <p className="text-sm font-medium text-ink truncate" title={file.name}>
            {file.name}
          </p>
          <p className="text-xs font-mono text-muted mt-1">{formatBytes(file.sizeBytes)}</p>
        </div>
      ))}
    </div>
  );
}
