package com.example.clb.projecttracker.listeners;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private final HttpServletRequest request;

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        Authentication authentication = event.getAuthentication();
        String username = null;
        String authType = "";

        // Handle different types of authentication
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof OAuth2User oauth2User) {
            // Handle OAuth2 login
            username = oauth2User.getAttribute("name");
            if (username == null) {
                username = oauth2User.getAttribute("login"); // For GitHub
            }
            if (username == null) {
                username = oauth2User.getAttribute("email");
            }
            authType = "OAuth2";
        } else if (principal instanceof UserDetails userDetails) {
            // Handle regular form login
            username = userDetails.getUsername();
            authType = "Form";
        } else if (principal instanceof String) {
            // Handle other cases where principal might be a String
            username = (String) principal;
            authType = "String";
        } else {
            // Fallback for any other type
            username = authentication.getName();
            authType = authentication.getClass().getSimpleName();
        }

        String ipAddress = request.getRemoteAddr();
        log.info("Successful {} login for user: {} from IP: {}", authType, username, ipAddress);
    }
}
