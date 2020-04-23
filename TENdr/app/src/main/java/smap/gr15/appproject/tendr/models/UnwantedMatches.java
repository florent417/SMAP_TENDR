package smap.gr15.appproject.tendr.models;

import java.util.ArrayList;

public class UnwantedMatches {
    private final String userId;
    public ArrayList<String> unwantedMatches;
    //private final int unwantedMatchesLimit;

    public UnwantedMatches(String userId) {
        this.userId = userId;
      //  this.unwantedMatchesLimit = limit;
        this.unwantedMatches = new ArrayList<>();
    }
    /*

    public ArrayList<String> getUnwantedMatches() {
        return unwantedMatches;
    }

    public void addUnwantedMatch(String unwantedMatchUserId) {
        this.unwantedMatches.add(unwantedMatchUserId);
    }

    public int getUnwantedMatchesLimit() {
        return unwantedMatchesLimit;
    }

    public int getSize*/
}
