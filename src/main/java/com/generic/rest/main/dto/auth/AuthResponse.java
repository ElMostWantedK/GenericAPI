package com.generic.rest.main.dto.auth;

import java.util.List;

public class AuthResponse {
    private String accessToken;
    private List<String> items;
    private String role;

    public AuthResponse(String accessToken, List<String> items, String role) {
        this.accessToken = accessToken;
        this.items = items;
        this.role = role;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
