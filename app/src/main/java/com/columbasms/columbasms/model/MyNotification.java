package com.columbasms.columbasms.model;

/**
 * Created by Matteo Brienza on 4/27/16.
 */
public class MyNotification {

    private String organization_name;
    private String organization_avatar_normal;
    private String message;

    public MyNotification(String organization_name, String organization_avatar_normal, String message){
        this.organization_name = organization_name;
        this.organization_avatar_normal = organization_avatar_normal;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getOrganization_avatar_normal() {
        return organization_avatar_normal;
    }

    public String getOrganization_name() {
        return organization_name;
    }

}
