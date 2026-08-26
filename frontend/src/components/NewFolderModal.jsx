import { useState } from "react";
import { X } from "lucide-react";

export default function NewFolderModal({ onClose, onCreate }) {
  const [name, setName] = useState("");
  const [busy, setBusy] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    if (!name.trim()) return;
    setBusy(true);
    try {
      await onCreate(name.trim());
      onClose();
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 px-4">
      <form onSubmit={submit} className="bg-white rounded-lg w-full max-w-sm border border-line">
        <div className="flex items-center justify-between px-5 py-4 border-b border-line">
          <h2 className="font-display font-semibold text-ink">New folder</h2>
          <button type="button" onClick={onClose} className="text-muted hover:text-ink">
            <X size={18} />
          </button>
        </div>
        <div className="p-5">
          <input
            autoFocus
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="Untitled folder"
            className="w-full border border-line rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-teal/40 focus:border-teal"
          />
        </div>
        <div className="px-5 py-4 border-t border-line flex justify-end gap-2">
          <button
            type="button"
            onClick={onClose}
            className="text-sm font-medium border border-line rounded-md px-4 py-2 text-ink hover:bg-black/5"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={busy}
            className="text-sm font-medium bg-ink text-white rounded-md px-4 py-2 hover:bg-teal transition-colors disabled:opacity-60"
          >
            Create
          </button>
        </div>
      </form>
    </div>
  );
}
