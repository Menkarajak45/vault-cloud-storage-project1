import axios from "axios";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "http://localhost:8080/api",
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("vault_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem("vault_token");
      localStorage.removeItem("vault_user");
      if (window.location.pathname !== "/login") {
        window.location.href = "/login";
      }
    }
    return Promise.reject(err);
  }
);

// ---- Auth ----
export const registerUser = (data) => api.post("/auth/register", data);
export const loginUser = (data) => api.post("/auth/login", data);
export const forgotPassword = (data) => api.post("/auth/forgot-password", data);
export const resetPassword = (data) => api.post("/auth/reset-password", data);
export const fetchMe = () => api.get("/auth/me");


// ---- Folders ----
export const browseFolder = (folderId) =>
  api.get("/folders", { params: folderId ? { folderId } : {} });
export const createFolder = (name, parentId) =>
  api.post("/folders", { name, parentId });
export const renameFolder = (id, name) =>
  api.patch(`/folders/${id}/rename`, { name });
export const moveFolder = (id, targetFolderId) =>
  api.patch(`/folders/${id}/move`, { targetFolderId });
export const starFolder = (id, starred) =>
  api.patch(`/folders/${id}/star`, { starred });
export const trashFolder = (id) => api.post(`/folders/${id}/trash`);
export const restoreFolder = (id) => api.post(`/folders/${id}/restore`);
export const listTrashedFolders = () => api.get("/folders/trash");
export const listStarredFolders = () => api.get("/folders/starred");
export const searchFolders = (q) => api.get("/folders/search", { params: { q } });

// ---- Files ----
export const uploadFile = (file, folderId, onProgress) => {
  const formData = new FormData();
  formData.append("file", file);
  if (folderId) formData.append("folderId", folderId);
  return api.post("/files/upload", formData, {
    headers: { "Content-Type": "multipart/form-data" },
    onUploadProgress: onProgress,
  });
};
export const downloadFile = (id) =>
  api.get(`/files/${id}/download`, { responseType: "blob" });
export const renameFile = (id, name) => api.patch(`/files/${id}/rename`, { name });
export const moveFile = (id, targetFolderId) =>
  api.patch(`/files/${id}/move`, { targetFolderId });
export const starFile = (id, starred) => api.patch(`/files/${id}/star`, { starred });
export const trashFile = (id) => api.post(`/files/${id}/trash`);
export const restoreFile = (id) => api.post(`/files/${id}/restore`);
export const deleteFileForever = (id) => api.delete(`/files/${id}`);
export const listTrashedFiles = () => api.get("/files/trash");
export const listStarredFiles = () => api.get("/files/starred");
export const searchFiles = (q, filters = {}) => api.get("/files/search", { params: { q, ...filters } });

// ---- Admin ----
export const fetchAdminDashboard = () => api.get("/admin/dashboard");
export const fetchAdminUsers = () => api.get("/admin/users");
export const fetchAdminUser = (id) => api.get(`/admin/users/${id}`);
export const updateAdminUserStatus = (id, enabled) =>
  api.put(`/admin/users/${id}/status`, { enabled });
export const fetchAdminFiles = (trashed) =>
  api.get("/admin/files", { params: trashed === undefined ? {} : { trashed } });
export const fetchAdminActivities = () => api.get("/admin/activities");

// ---- Sharing ----
export const createShare = (fileId, email, permission) =>
  api.post("/shares", { fileId, email, permission });
export const listSharesForFile = (fileId) => api.get(`/shares/file/${fileId}`);
export const revokeShare = (shareId) => api.delete(`/shares/${shareId}`);
export const listSharedWithMe = () => api.get("/shares/shared-with-me");

// ---- Public links ----
export const createPublicLink = (fileId, password, expiresInHours) =>
  api.post("/public-links", { fileId, password: password || null, expiresInHours: expiresInHours || null });
export const listPublicLinksForFile = (fileId) => api.get(`/public-links/file/${fileId}`);
export const revokePublicLink = (id) => api.delete(`/public-links/${id}`);

export default api;

export const changePublicLinkPassword = (id, password) => api.patch(`/public-links/${id}/password`, { password: password || null });
