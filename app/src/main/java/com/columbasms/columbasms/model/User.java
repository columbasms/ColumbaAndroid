package com.columbasms.columbasms.model;

/**
 * Created by Matteo Brienza on 2/9/16.
 */
public class User {

    private String id;
    private String digits_id;
    private String digits_token;
    private String phone_number;
    private String fullName;
    private String profile_image;
    private String cover_image;
    private int assFollowed;
    private int campForwarder;
    private int score;
    private int rank;
    private int sms_sended_total;
    private int sms_sended_month;

    public User(String id,String digits_id, String digits_token,String phone_number){
        this.id = id;
        this.digits_id = digits_id;
        this.digits_token = digits_token;
        this.phone_number = phone_number;
    }

    public User(String fullName, String profile_image, String cover_image){
        this.fullName = fullName;
        this.profile_image = profile_image;
        this.cover_image = cover_image;
    }

    public User(String fullName, String profile_image, String cover_image, int assFollowed,int campForwarder, int sms_sended_month, int sms_sended_total){
        this.fullName = fullName;
        this.profile_image = profile_image;
        this.cover_image = cover_image;
        this.assFollowed = assFollowed;
        this.campForwarder = campForwarder;
        this.sms_sended_month = sms_sended_month;
        this.sms_sended_total = sms_sended_total;
    }

    public User(String fullName, String profile_image, int score,int rank){
        this.fullName = fullName;
        this.profile_image = profile_image;
        this.score = score;
        this.rank = rank;
    }

    public int getRank() {
        return rank;
    }

    public String getId() {
        return id;
    }

    public String getDigits_id() {
        return digits_id;
    }

    public String getDigits_token() {
        return digits_token;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public String getFullName() {
        return fullName;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public String getCover_image() {
        return cover_image;
    }

    public int getAssFollowed() {
        return assFollowed;
    }

    public int getCampForwarder() {
        return campForwarder;
    }

    public int getScore() {
        return score;
    }

    public int getSms_sended_month() {
        return sms_sended_month;
    }

    public int getSms_sended_total() {
        return sms_sended_total;
    }
}
