package scc.data;

import scc.utils.Hash;

public class Login {
    private String userId;
    private String pwd;// hashed pwd

    public Login(String userId, String pwd){
        this.pwd = pwd;
        this.userId = userId;
    }
    public Login(){
    }

    public String getUserId() {
        return userId;
    }

    public String getPwd() {
        return pwd;
    }

}
