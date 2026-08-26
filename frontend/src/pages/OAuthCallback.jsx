import { useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { fetchMe } from "../services/api";

export default function OAuthCallback() {
  const [params] = useSearchParams();
  const navigate = useNavigate();
  useEffect(() => {
    const token = params.get("token");
    if (!token) { navigate("/login?oauthError=missing_token", { replace: true }); return; }
    localStorage.setItem("vault_token", token);
    fetchMe().then((res) => {
      localStorage.setItem("vault_user", JSON.stringify(res.data));
      navigate(res.data.role === "ADMIN" ? "/admin/dashboard" : "/", { replace: true });
    }).catch(() => { localStorage.removeItem("vault_token"); navigate("/login?oauthError=failed", { replace: true }); });
  }, [params, navigate]);
  return <div className="min-h-screen flex items-center justify-center bg-paper text-sm text-muted">Signing you in with Google…</div>;
}
