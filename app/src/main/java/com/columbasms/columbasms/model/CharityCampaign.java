package com.columbasms.columbasms.model;

import java.util.List;

/**
 * Created by matteobrienza on 1/30/16.
 */
public class CharityCampaign {


    private String id;
    private String message;
    private Association organization;
    private List<Topic> topics;
    private String timestamp;

    //NEW DETAILS
    private String longDescription;
    private String photo;
    private List<Address>addresses;
    //private boolean isExpired;

    public CharityCampaign(){}

    public CharityCampaign(String id, String message, Association organization, List<Topic> topics,String timestamp){
        this.id = id;
        this.message = message;
        this.organization = organization;
        this.topics = topics;
        this.timestamp = timestamp;
    }

    public CharityCampaign(String id, String message, Association organization, List<Topic> topics,String timestamp, String longDescription, String photo, List<Address>addresses){
        this.id = id;
        this.message = message;
        this.organization = organization;
        this.topics = topics;
        this.timestamp = timestamp;
        this.longDescription = longDescription;
        this.photo = photo;
        this.addresses = addresses;
    }

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public Association getOrganization() {
        return organization;
    }

    public List<Topic> getTopics() {
        return topics;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public String getPhoto() {
        return photo;
    }

    public List<Address> getAddresses() {
        return addresses;
    }
}
