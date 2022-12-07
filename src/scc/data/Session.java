package scc.data;

import java.io.Serializable;

/**
 * This class can be extended when its needed for user session
 */
public class Session  implements Serializable {
    private String userId;
    private String name;

    public Session(String userId, String name){
        this.userId = userId;
        this.name = name;
    }

    public Session(){
    }

    public String getUserId() {
        return userId;
    }

    public String getName() { return name; }

    @Override
    public String toString(){
        return "Session [userId=" + userId + ", name=" + name + "]";
    }

    public String toJson() {
        return "{\"userId\": " + "\""+userId+"\", \"name\": " + "\""+name+"\"}";
    }
}
