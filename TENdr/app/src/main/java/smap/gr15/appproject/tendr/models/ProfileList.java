package smap.gr15.appproject.tendr.models;

import java.util.ArrayList;

public class ProfileList {
    private final String userId;
    public ArrayList<String> list;

    public ProfileList(String userId) {
        this.userId = userId;
        this.list = new ArrayList<>();
    }
}