package com.example.pawpal.session;

import com.example.pawpal.model.User;

public class Session {

    private static User currentUser;

    public static User getCurrentUser() {

        return currentUser;

    }

    public static void setCurrentUser(User user) {

        currentUser = user;

    }

    public static void logout() {

        currentUser = null;

    }

}