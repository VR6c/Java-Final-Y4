package com.example.demo.config.logging;

import com.example.demo.service.logging.ActivityLogService;
import com.example.demo.util.ActivityResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Filter that intercepts incoming HTTP requests and outgoing HTTP responses.
 * Terminal console API logging has been removed as requested to keep the terminal clean.
 * It only records audit logs asynchronously to the database if saveToDb is enabled.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HttpLoggingFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    private final ActivityLogService activityLogService;

    @Value("${logging.activity.save-to-db:true}")
    private boolean saveToDb;

    public HttpLoggingFilter(ObjectMapper objectMapper, ActivityLogService activityLogService) {
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper().findAndRegisterModules();
        this.activityLogService = activityLogService;
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return true;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return true;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if (!saveToDb || isStaticOrDocResource(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();
        Exception caughtException = null;

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } catch (Exception ex) {
            caughtException = ex;
            throw ex;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            recordActivity(requestWrapper, responseWrapper, duration, caughtException);
            // Copy body back to original response so the client receives the payload
            responseWrapper.copyBodyToResponse();
        }
    }

    private void recordActivity(
            ContentCachingRequestWrapper request,
            ContentCachingResponseWrapper response,
            long duration,
            Exception exception
    ) {
        if (!saveToDb || activityLogService == null) {
            return;
        }

        try {
            int status = response.getStatus();
            if (exception != null && status == HttpStatus.OK.value()) {
                status = HttpStatus.INTERNAL_SERVER_ERROR.value();
            }

            String method = request.getMethod();
            String uri = getFullRequestUri(request);
            String clientIp = getClientIp(request);

            // Resolve Activity (Action & Resource)
            ActivityResolver.ActivityInfo activity = ActivityResolver.resolve(method, request.getRequestURI());

            // Extract Authenticated User (if present in SecurityContext)
            UserIdentity user = getAuthenticatedUser();

            // Persist activity to database asynchronously
            activityLogService.recordActivityAsync(
                    user.username(),
                    activity.action(),
                    activity.resource(),
                    method + " " + uri,
                    method,
                    status,
                    duration,
                    clientIp,
                    exception != null ? exception.getMessage() : null
            );
        } catch (Exception ignored) {
            // Silently ignore to avoid console clutter
        }
    }

    private UserIdentity getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            String username = auth.getName();
            String roles = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(", "));
            return new UserIdentity(username, roles, true);
        }
        return new UserIdentity("Anonymous", "NONE", false);
    }

    private record UserIdentity(String username, String roles, boolean authenticated) {
    }

    private String getFullRequestUri(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        return queryString != null ? uri + "?" + queryString : uri;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private boolean isStaticOrDocResource(String uri) {
        return uri.startsWith("/swagger-ui")
                || uri.startsWith("/v3/api-docs")
                || uri.startsWith("/swagger-resources")
                || uri.startsWith("/webjars")
                || uri.equals("/favicon.ico")
                || uri.endsWith(".css")
                || uri.endsWith(".js")
                || uri.endsWith(".png")
                || uri.endsWith(".jpg")
                || uri.endsWith(".jpeg")
                || uri.endsWith(".gif")
                || uri.endsWith(".ico")
                || uri.endsWith(".svg")
                || uri.endsWith(".woff")
                || uri.endsWith(".woff2")
                || uri.endsWith(".ttf");
    }
}
