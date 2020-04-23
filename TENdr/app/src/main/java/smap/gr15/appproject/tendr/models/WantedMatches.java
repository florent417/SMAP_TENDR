package smap.gr15.appproject.tendr.models;

import java.util.ArrayList;

public class WantedMatches {
    private final String userId;
    public ArrayList<String> wantedMatches;

    public WantedMatches(String userId) {
        this.userId = userId;
        this.wantedMatches = new ArrayList<>();
    }
}
