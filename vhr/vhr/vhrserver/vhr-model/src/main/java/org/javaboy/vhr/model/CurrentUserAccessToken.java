package org.javaboy.vhr.model;

public class CurrentUserAccessToken {

    private static final ThreadLocal<String> ACCESS_TOKEN = new ThreadLocal<>();

    public static void setAccessToken(String accessToken){
        ACCESS_TOKEN.set(accessToken);
    }

    public static String getAccessToken(){
        return ACCESS_TOKEN.get();
    }

    public static void removeAccessToken(){
        ACCESS_TOKEN.remove();
    }

}
