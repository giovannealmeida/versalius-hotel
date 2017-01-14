package br.com.versalius.checkhotel.model;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by jn18 on 13/01/2017.
 */

public class User implements Serializable {

        private long id;
        private String first_name;
        private String last_name;
        private String cpf;
        private String password;
        private String email;
        private int photo_id;
        private int role_id;
        private boolean status;

        public User(JSONObject json) {
        if (json != null) {
            this.id = json.optLong("id");
            this.first_name = json.optString("first_name", "");
            this.last_name = json.optString("last_name", "");
            this.cpf = json.optString("cpf", "");
            this.password = json.optString("password", "");
            this.email = json.optString("email", "");
            this.role_id = json.optInt("role_id");
            this.status = json.optBoolean("role_id");
            this.photo_id = json.optInt("photo_id");
        }
    }

    public long getId(){
        return this.id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public String getCpf() {
        return cpf;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public int getPhoto_id() {
        return photo_id;
    }

    public int getRole_id() {
        return role_id;
    }

    public boolean isStatus() {
        return status;
    }
}
