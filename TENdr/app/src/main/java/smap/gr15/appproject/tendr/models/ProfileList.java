package smap.gr15.appproject.tendr.models;

import java.util.ArrayList;

public class ProfileList {
    public String userId;
    public ArrayList<String> list = new ArrayList<>();

    public ProfileList(){};

    public ProfileList(String userId) {
        this.userId = userId;
        this.list = new ArrayList<>();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public ArrayList<String> getList() {
        return list;
    }

    public void setList(ArrayList<String> list) {
        this.list = list;
    }
}
