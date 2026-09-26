package com.example.demo.util;

/**
 * Utility helper to classify HTTP requests into structured activities
 * (Action and Resource) based on HTTP method and URI path.
 */
public final class ActivityResolver {

    private ActivityResolver() {
    }

    public record ActivityInfo(String action, String resource) {
        @Override
        public String toString() {
            return "[" + action + "] " + resource;
        }
    }

    public static ActivityInfo resolve(String method, String uri) {
        if (uri == null || uri.isBlank()) {
            return new ActivityInfo(method != null ? method.toUpperCase() : "UNKNOWN", "SYSTEM");
        }

        String cleanUri = uri;
        int queryIdx = cleanUri.indexOf('?');
        if (queryIdx != -1) {
            cleanUri = cleanUri.substring(0, queryIdx);
        }

        String[] parts = cleanUri.split("/");
        String primarySegment = "";
        String subSegment = "";
        for (String part : parts) {
            if (!part.isEmpty()) {
                if (primarySegment.isEmpty()) {
                    primarySegment = part;
                } else if (subSegment.isEmpty()) {
                    subSegment = part;
                }
            }
        }

        String resource = primarySegment.toUpperCase();
        if (resource.endsWith("S") && !resource.equalsIgnoreCase("STATUS")) {
            // Convert simple plural to singular, e.g. CUSTOMERS -> CUSTOMER
            resource = resource.substring(0, resource.length() - 1);
        }

        String action;
        if (cleanUri.contains("/search")) {
            action = "SEARCH";
        } else if (cleanUri.contains("/login")) {
            action = "LOGIN";
            resource = "AUTH";
        } else if (cleanUri.contains("/register")) {
            action = "REGISTER";
            resource = "AUTH";
        } else if (cleanUri.contains("/refresh")) {
            action = "REFRESH_TOKEN";
            resource = "AUTH";
        } else {
            action = switch (method.toUpperCase()) {
                case "GET" -> (!subSegment.isEmpty() && !subSegment.equalsIgnoreCase("search"))
                        ? "VIEW_DETAIL"
                        : "FETCH_ALL";
                case "POST" -> "CREATE";
                case "PUT" -> "UPDATE";
                case "PATCH" -> "PATCH_UPDATE";
                case "DELETE" -> "DELETE";
                default -> method.toUpperCase();
            };
        }

        return new ActivityInfo(action, resource.isEmpty() ? "SYSTEM" : resource);
    }
}
