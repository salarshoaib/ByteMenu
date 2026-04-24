package com.bytemenu.util;

import com.bytemenu.model.User;

/**
 * In-memory session holder. Stores the currently logged-in user.
 */
public class SessionManager {

    private static User currentUser;

    public static void setCurrentUser(User user) { currentUser = user; }
    public static User getCurrentUser()          { return currentUser; }
    public static boolean isLoggedIn()           { return currentUser != null; }
    public static void logout()                  { currentUser = null; }
}
