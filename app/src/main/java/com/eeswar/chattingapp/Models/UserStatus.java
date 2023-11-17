package com.eeswar.chattingapp.Models;

import java.util.ArrayList;

public class UserStatus {
    private String name, profilepic;
    private long lastUpdated;
    private ArrayList<Status> statuses;
    public UserStatus(String name, String profilepic, long lastUpdated, ArrayList<Status> statuses) {
        this.name = name;
        this.profilepic = profilepic;
        this.lastUpdated = lastUpdated;
        this.statuses = statuses;
    }
    public UserStatus(){
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilepic() {
        return profilepic;
    }

    public void setProfilepic(String profilepic) {
        this.profilepic = profilepic;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public ArrayList<Status> getStatuses() {
        return statuses;
    }

    public void setStatuses(ArrayList<Status> statuses) {
        this.statuses = statuses;
    }
}
