package br.com.versalius.checkhotel.model;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by jn18 on 13/01/2017.
 */

public class User implements Serializable {

    private long id;
    private String avatar;
    private String name;
    private String email;
    private String birthday;
    private int gender_id;
    private String phone;
    private String rg;
    private String passport;
    private String shipping_agent;
    private String nationality;
    private String street;
    private String number;
    private String zip_code;
    private int city_id;
    private String profession;
    private String neighborhood;
    private String cpf;
    private String key;

    public User(JSONObject json) {
        if (json != null) {
            this.id = json.optLong("id");
            this.avatar = json.optString("avatar", "");
            this.name = json.optString("name", "");
            this.email = json.optString("email", "");
            this.birthday = json.optString("birthday", "");
            this.email = json.optString("email", "");
            this.gender_id = json.optInt("gender_id");
            this.phone = json.optString("phone");
            this.rg = json.optString("rg");
            this.passport = json.optString("passport");
            this.shipping_agent = json.optString("shipping_agent");
            this.nationality = json.optString("nationality");
            this.street = json.optString("street");
            this.number = json.optString("number");
            this.zip_code = json.optString("zipcode");
            this.city_id = json.optInt("city_id");
            this.profession = json.optString("profession");
            this.neighborhood = json.optString("neighborhood");
            this.cpf = json.optString("cpf");
            this.key = json.optString("key");
        }
    }

    public long getId() {
        return this.id;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {return email;}

    public String getBirthday() {return birthday;}

    public int getGender_id() {
        return gender_id;
    }

    public String getPhone() {
        return phone;
    }

    public String getRg() {return rg;}

    public String getPassport() {
        return passport;
    }

    public String getShipping_agent() {
        return shipping_agent;
    }

    public String getNationality() {
        return nationality;
    }

    public String getStreet() {
        return street;
    }
    public String getNumber(){ return number;}

    public String getZip_code() {
        return zip_code;
    }

    public int getCity_id() {
        return city_id;
    }

    public String getProfession() {
        return profession;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public String getCpf() {
        return cpf;
    }

    public String getKey() {
        return key;
    }

}
