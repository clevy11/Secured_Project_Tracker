package com.example.clb.projecttracker.security.oauth2;

import com.example.clb.projecttracker.config.AppProperties;
import com.example.clb.projecttracker.exception.BadRequestException;
import com.example.clb.projecttracker.security.jwt.JwtUtils;
import com.example.clb.projecttracker.util.CookieUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import static com.example.clb.projecttracker.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);

    private JwtUtils jwtUtils;
    private AppProperties appProperties;
    private HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Autowired
    OAuth2AuthenticationSuccessHandler(JwtUtils jwtUtils, AppProperties appProperties,
                                     HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository) {
        this.jwtUtils = jwtUtils;
        this.appProperties = appProperties;
        this.httpCookieOAuth2AuthorizationRequestRepository = httpCookieOAuth2AuthorizationRequestRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        clearAuthenticationAttributes(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // Get the redirect URI from cookie
        Optional<String> redirectUri = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue);

        // If no redirect URI is provided, use a default fallback URL
        String targetUrl = redirectUri.orElse("http://localhost:3000");
        
        // Log the redirect URI handling process
        logger.debug("OAuth2 callback received. Raw redirect URI from cookie: {}", redirectUri.orElse("not provided"));
        logger.debug("Using target URL: {}", targetUrl);
        logger.debug("Authorized redirect URIs: {}", 
                String.join(", ", appProperties.getOauth2().getAuthorizedRedirectUris()));

        // Validate the redirect URI
        if (redirectUri.isPresent()) {
            String rawUri = redirectUri.get();
            if (!isAuthorizedRedirectUri(rawUri)) {
                logger.error("Unauthorized redirect URI attempt. URI: {}", rawUri);
                throw new BadRequestException(String.format("Sorry! Redirect URI %s is not authorized. Please ensure it matches one of the authorized URIs: %s", 
                    rawUri, String.join(", ", appProperties.getOauth2().getAuthorizedRedirectUris())));
            }
            logger.debug("Redirect URI {} is authorized", rawUri);
        } else {
            logger.debug("No redirect URI found in cookie, using default URL: {}", targetUrl);
        }

        // Generate JWT token
        String token = jwtUtils.generateJwtToken(authentication);
        
        // Add token to response header
        response.setHeader("Authorization", "Bearer " + token);
        
        // For OAuth2 login, we'll redirect to the frontend with the token
        String finalUrl = UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("token", token)
                .build().toUriString();
                
        logger.debug("Redirecting user to: {}", finalUrl);
        return finalUrl;
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }

    private boolean isAuthorizedRedirectUri(String uri) {
        try {
            URI clientRedirectUri = URI.create(uri);
            
            // Get the base URL (scheme + host + port) for comparison
            String clientBaseUrl = String.format("%s://%s%s",
                clientRedirectUri.getScheme(),
                clientRedirectUri.getHost(),
                clientRedirectUri.getPort() == -1 ? "" : ":" + clientRedirectUri.getPort());

            logger.debug("Checking authorization for base URL: {}", clientBaseUrl);

            // Check if the redirect URI matches any of our authorized patterns
            return appProperties.getOauth2().getAuthorizedRedirectUris()
                    .stream()
                    .anyMatch(authorizedUri -> {
                        try {
                            URI authorizedURI = URI.create(authorizedUri);
                            
                            // Check if the base URLs match (scheme + host + port)
                            String authorizedBaseUrl = String.format("%s://%s%s",
                                authorizedURI.getScheme(),
                                authorizedURI.getHost(),
                                authorizedURI.getPort() == -1 ? "" : ":" + authorizedURI.getPort());

                            // Also check if the path starts with any of our authorized paths
                            String clientPath = clientRedirectUri.getPath();
                            String authorizedPath = authorizedURI.getPath();
                            
                            boolean isAuthorized = clientBaseUrl.equalsIgnoreCase(authorizedBaseUrl) && 
                                                 (clientPath.startsWith(authorizedPath) || authorizedPath.equals("/"));
                            
                            if (!isAuthorized) {
                                logger.debug("URI {} not authorized. Base URL match: {}, Path match: {}", 
                                    uri, clientBaseUrl.equalsIgnoreCase(authorizedBaseUrl), 
                                    clientPath.startsWith(authorizedPath));
                            }
                            return isAuthorized;
                        } catch (Exception e) {
                            logger.error("Error processing authorized URI: {}", authorizedUri, e);
                            return false;
                        }
                    });
        } catch (Exception e) {
            logger.error("Error processing client redirect URI: {}", uri, e);
            throw new BadRequestException(String.format("Invalid redirect URI: %s", uri));
        }
    }


}
