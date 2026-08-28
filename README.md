# ☁️ Vault — Cloud File Storage

A secure cloud-based file storage and sharing web application built with **Java Spring Boot** and **React**. Vault provides users with authentication, file and folder management, search, sharing, public links, trash, starred files, and an administrator dashboard.

## 🚀 Features

### 🔐 Authentication & Security

* User registration and login
* JWT-based authentication
* BCrypt password hashing
* Forgot password and password reset
* Role-based authorization
* Separate `ADMIN` and `USER` platform roles

### 📁 File Management

* Upload files
* Download files
* Rename files
* Move files
* Delete files permanently
* Trash and restore files
* Star files
* Search files
* Storage quota tracking
* 5 GB default storage quota

### 📂 Folder Management

* Create folders
* Create nested folders
* Rename folders
* Move folders
* Star folders
* Trash and restore folders
* Search folders
* Protection against moving folders into their own descendants

### 👥 File Sharing

* Share files with registered users
* Viewer permission
* Editor permission
* Owner permission
* Revoke file access
* Shared with Me section

### 🔗 Public File Links

* Generate public sharing links
* Optional password protection
* Optional link expiration
* Preview shared file information
* Password-protected downloads
* Revoke public links
* Expired/revoked links return appropriate errors

### 👨‍💼 Admin Dashboard

* Admin and User roles
* Platform-wide statistics
* View total users
* View total admins
* View total files
* View total folders
* View storage usage
* View registered users
* Enable/disable user accounts
* View files across users
* Secure admin-only API endpoints

### 🎨 User Interface

* Responsive web interface
* Grid-based file layout
* My Drive
* Starred
* Trash
* Shared with Me
* Search and filtering
* File upload with progress indication

---

## 🛠️ Technologies Used

### Frontend

* React
* Vite
* JavaScript
* React Router
* Tailwind CSS
* HTML5
* CSS3

### Backend

* Java 17
* Spring Boot
* Spring Security
* JWT
* REST APIs
* Maven
* BCrypt

### Database

* H2 Database for local development
* PostgreSQL support for deployment

### Tools

* Git
* GitHub
* VS Code
* IntelliJ IDEA
* Postman

---

## 🏗️ Project Structure

```text
vault-cloud-storage-project/
│
├── backend/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       └── resources/
│   ├── .env.example
│   └── pom.xml
│
├── frontend/
│   ├── src/
│   ├── public/
│   ├── .env.example
│   ├── package.json
│   └── vite.config.js
│
├── .gitignore
└── README.md
```

---

## ⚙️ Requirements

Make sure you have installed:

* Java 17 or later
* Maven
* Node.js 18 or later
* npm
* Git

---

## 🔧 Backend Setup

Navigate to the backend folder:

```bash
cd backend
```

Create a `.env` file using `.env.example` as a reference.

Example:

```env
FRONTEND_URL=http://localhost:5173

MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-gmail@gmail.com
MAIL_PASSWORD=your-gmail-app-password

ADMIN_EMAIL=your-admin-email@gmail.com
ADMIN_PASSWORD=your-admin-password
```

**Never upload the real `.env` file to GitHub.**

Start the backend:

```bash
mvn spring-boot:run
```

The backend runs on:

```text
http://localhost:8080
```

---

## 💻 Frontend Setup

Open another terminal and navigate to the frontend:

```bash
cd frontend
```

Install dependencies:

```bash
npm install
```

Create a `.env` file using `.env.example`:

```env
VITE_API_URL=http://localhost:8080/api
```

Start the development server:

```bash
npm run dev
```

The frontend runs on:

```text
http://localhost:5173
```

---

## 👨‍💼 Admin Account

The first administrator is created using environment variables.

Set:

```env
ADMIN_EMAIL=your-admin-email@gmail.com
ADMIN_PASSWORD=your-admin-password
```

When the backend starts, the application checks whether an administrator already exists.

If no administrator exists, it creates the initial admin account with a BCrypt-hashed password.

The admin can then log in through the normal login page and access:

```text
/admin/dashboard
```

---

## 🔐 File Permissions

Vault uses three file-level permissions:

| Permission | Access                                    |
| ---------- | ----------------------------------------- |
| OWNER      | Full control                              |
| EDITOR     | View, download, rename, trash and restore |
| VIEWER     | View and download                         |

Platform roles are separate from file permissions:

| Platform Role | Purpose                                                   |
| ------------- | --------------------------------------------------------- |
| ADMIN         | Manage platform users and view platform-level information |
| USER          | Normal application user                                   |

An administrator does not automatically receive access to another user's files.

---

## 🔗 Public Sharing

File owners can generate public links with optional:

* Password protection
* Expiration time

A public link can be accessed without logging in.

If password protection is enabled, the recipient must provide the correct password before downloading the file.

Revoked or expired links are rejected by the backend.

---

## 🔒 Security

The application implements several security measures:

* JWT authentication
* BCrypt password hashing
* Spring Security authorization
* Role-based access control
* Protected API endpoints
* Admin-only API routes
* File-level access control
* Password-protected public links
* Environment variables for sensitive configuration

Sensitive configuration such as passwords, email credentials, and secrets should never be committed to GitHub.

---

## 📡 API Overview

### Authentication

```text
POST /api/auth/register
POST /api/auth/login
GET  /api/auth/me
```

### Files

```text
POST   /api/files/upload
GET    /api/files/{id}/download
PATCH  /api/files/{id}/rename
PATCH  /api/files/{id}/move
PATCH  /api/files/{id}/star
POST   /api/files/{id}/trash
POST   /api/files/{id}/restore
DELETE /api/files/{id}
GET    /api/files/trash
GET    /api/files/starred
GET    /api/files/search
```

### Folders

```text
GET    /api/folders
POST   /api/folders
PATCH  /api/folders/{id}/rename
PATCH  /api/folders/{id}/move
PATCH  /api/folders/{id}/star
POST   /api/folders/{id}/trash
POST   /api/folders/{id}/restore
GET    /api/folders/trash
GET    /api/folders/starred
GET    /api/folders/search
```

### Sharing

```text
POST   /api/shares
GET    /api/shares/file/{fileId}
DELETE /api/shares/{shareId}
GET    /api/shares/shared-with-me
```

### Public Links

```text
POST   /api/public-links
GET    /api/public-links/file/{fileId}
DELETE /api/public-links/{id}
GET    /api/public-links/{token}/preview
POST   /api/public-links/{token}/unlock
GET    /api/public-links/{token}/download
```

### Admin

```text
GET /api/admin/dashboard
GET /api/admin/users
GET /api/admin/users/{id}
PUT /api/admin/users/{id}/status
GET /api/admin/files
GET /api/admin/activities
```

---

## 🗄️ Database & Storage

For local development, Vault uses an embedded file-based H2 database.

The application stores:

* User information
* Folder information
* File metadata
* Sharing information
* Public link information

Uploaded file content is stored separately on local disk during local development.

For production deployment, PostgreSQL can be configured and local storage can be replaced with cloud object storage such as Amazon S3 or another compatible storage service.

---

## 🧪 Testing

The application can be tested using:

* Browser
* Postman
* REST API requests

Important scenarios include:

1. Register a new user.
2. Log in and receive a JWT.
3. Upload a file.
4. Create nested folders.
5. Move and rename files.
6. Star and trash files.
7. Restore deleted files.
8. Share a file with another registered user.
9. Test Viewer and Editor permissions.
10. Generate a password-protected public link.
11. Test an expired public link.
12. Log in as an administrator.
13. Access the Admin Dashboard.
14. Disable a user account.
15. Verify unauthorized users receive appropriate authentication/authorization errors.

---

## 📌 Future Improvements

Possible future improvements include:

* File versioning
* File previews
* Activity logging
* Folder-level sharing
* Tags and labels
* Pagination for large datasets
* Cloud object storage integration
* Advanced storage plans
* Improved admin user-detail interface

---

## 🎯 Project Objective

The objective of Vault is to develop a secure and user-friendly cloud file storage platform where users can store, organize, search, manage, and share their digital files while maintaining authentication, authorization, and access control.

The project demonstrates full-stack development using a **React frontend** and **Java Spring Boot backend**, with REST APIs connecting the application to its database and storage layer.

---

## 👩‍💻 Author

**Menka Kumari**

B.Sc. Information Technology Student

---

## 📜 License

This project was developed for educational and academic purposes.
