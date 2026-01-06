package com.cinebook.demo1.app;

import com.cinebook.demo1.model.Utilisateur;

public final class Session {
    private static Utilisateur currentUser;

    private Session() {}

    public static void setCurrentUser(Utilisateur user) {
        currentUser = user;
    }

    public static Utilisateur getCurrentUser() {
        return currentUser;
    }

    public static void clear() {
        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}
