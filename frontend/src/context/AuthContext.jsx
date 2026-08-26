import { createContext, useContext, useState, useEffect } from "react";
import { loginUser, registerUser, fetchMe } from "../services/api";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const raw = localStorage.getItem("vault_user");
    return raw ? JSON.parse(raw) : null;
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem("vault_token");
    if (!token) {
      setLoading(false);
      return;
    }
    fetchMe()
      .then((res) => {
        setUser(res.data);
        localStorage.setItem("vault_user", JSON.stringify(res.data));
      })
      .catch(() => {
        localStorage.removeItem("vault_token");
        localStorage.removeItem("vault_user");
        setUser(null);
      })
      .finally(() => setLoading(false));
  }, []);

  const login = async (email, password) => {
    const res = await loginUser({ email, password });
    localStorage.setItem("vault_token", res.data.token);
    localStorage.setItem("vault_user", JSON.stringify(res.data.user));
    setUser(res.data.user);
    return res.data.user;
  };

  const register = async (name, email, password) => {
    const res = await registerUser({ name, email, password });
    localStorage.setItem("vault_token", res.data.token);
    localStorage.setItem("vault_user", JSON.stringify(res.data.user));
    setUser(res.data.user);
    return res.data.user;
  };

  const logout = () => {
    localStorage.removeItem("vault_token");
    localStorage.removeItem("vault_user");
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
