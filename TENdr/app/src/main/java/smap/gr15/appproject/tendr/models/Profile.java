package smap.gr15.appproject.tendr.models;

import java.util.ArrayList;
import java.util.List;
import smap.gr15.appproject.tendr.utils.helpers;

public class Profile {
    private String firstName;
    private int age;
    private String bio;
    private String occupation;
    private String city;
    private String country;
    private String gender;
    private List<String> genderPreference = new ArrayList<>();
    private String email;
    private String password;
    private List<String> pictures;

    //This is used to create a new profile on first launch
    public Profile(String firstName, int age, String occupation, String city, String country, String gender, String email, String password) {
        this.firstName = firstName;
        this.age = age;
        this.occupation = occupation;
        this.city = city;
        this.country = country;
        this.gender = gender;
        this.genderPreference.add(helpers.setGenderOpposite(gender));
        this.email = email;
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public List<String> getGenderPreference() {
        return genderPreference;
    }

    public void setGenderPreference(List<String> genderPreference) {
        this.genderPreference = genderPreference;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getPictures() {
        return pictures;
    }

    public void setPictures(List<String> pictures) {
        this.pictures = pictures;
    }


    // TODO: Maybe add IG account
    // TODO: Feature add points


}
