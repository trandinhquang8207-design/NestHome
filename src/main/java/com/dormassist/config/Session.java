package com.dormassist.config;

import com.dormassist.model.User;

public class Session {
    private static User currentUser;

    public static void setCurrentUser(User u) { currentUser = u; }
    public static User getCurrentUser()        { return currentUser; }
    public static void clear()                 { currentUser = null; }
    public static boolean isLoggedIn()         { return currentUser != null; }
    public static String getRole()             { return currentUser != null ? currentUser.getRole() : ""; }
    public static int getUserId()              { return currentUser != null ? currentUser.getId() : -1; }

    public static boolean isAdminSuper()     { return AppConstants.ROLE_ADMIN_SUPER.equals(getRole()); }
    public static boolean isAdminBase()      { return AppConstants.ROLE_ADMIN_BASE.equals(getRole()); }
    public static boolean isBase1()          { return AppConstants.ROLE_BASE1.equals(getRole()); }
    public static boolean isBase2()          { return AppConstants.ROLE_BASE2.equals(getRole()); }
    public static boolean isBase3()          { return AppConstants.ROLE_BASE3.equals(getRole()); }
    public static boolean isAdmin()          { return isAdminSuper() || isAdminBase(); }
    public static boolean canManageFinance() { return isAdmin(); }
    public static boolean canManageAssets()  { return isAdmin(); }
    public static boolean canApproveVisitors(){ return isAdmin() || isBase1(); }
    public static boolean canViewAllRooms()  { return isAdmin() || isBase1(); }
}
