package com.project.vaccinenotifier;

public class User {

    public String email, state, district;
    public int age, state_id, district_id;

    public User() {

    }

    public User(String email, int age, String state, String district, int state_id, int district_id) {
        this.email = email;
        this.age = age;
        this.state = state;
        this.district = district;
        this.state_id = state_id;
        this.district_id = district_id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getState_id() {
        return state_id;
    }

    public void setState_id(int state_id) {
        this.state_id = state_id;
    }

    public int getDistrict_id() {
        return district_id;
    }

    public void setDistrict_id(int district_id) {
        this.district_id = district_id;
    }
}
