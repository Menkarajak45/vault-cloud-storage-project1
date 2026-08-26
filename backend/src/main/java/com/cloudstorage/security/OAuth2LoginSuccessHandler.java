package com.cloudstorage.security;

import com.cloudstorage.model.User;
import com.cloudstorage.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    @Value("${app.frontend-url:http://localhost:5173}") private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oauth = (OAuth2User) authentication.getPrincipal();
        String email = oauth.getAttribute("email");
        String name = oauth.getAttribute("name");
        if (email == null || email.isBlank()) { response.sendRedirect(frontendUrl + "/login?oauthError=email"); return; }
        String normalized = email.toLowerCase().trim();
        User user = userRepository.findByEmail(normalized).orElseGet(() -> userRepository.save(User.builder().name(name == null ? normalized : name).email(normalized).passwordHash(null).build()));
        if (!user.isEnabled()) { response.sendRedirect(frontendUrl + "/login?oauthError=disabled"); return; }
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        response.sendRedirect(frontendUrl + "/oauth/callback?token=" + java.net.URLEncoder.encode(token, java.nio.charset.StandardCharsets.UTF_8));
    }
}
