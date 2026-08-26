package com.cloudstorage.model;

// Resource-level permission for a single shared file. Deliberately separate
// from Role (ADMIN/USER), which is platform-level. Ownership itself isn't a
// value here — a file's owner is tracked via FileItem.ownerId, not a Share row.
public enum Permission {
    EDITOR,
    VIEWER
}
