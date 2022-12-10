package scc.data;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class Bid {
    private String id;
    private String auctionId;
    private float amount;
    private String userId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    private Date time;

    public Bid(){}

    public Bid(String id,String auctionId, float amount, String userId,Date time){
        super();
        this.auctionId = auctionId;
        this.id = id;
        this.amount = amount;
        this.userId = userId;
        this.time = time;
    }

    public Bid(String auctionId, float amount, String userId,Date time){
        super();
        id = null;
        this.auctionId = auctionId;
        this.id = id;
        this.amount = amount;
        this.userId = userId;
        this.time = time;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public float getAmount() {
        return amount;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    @Override
    public String toString(){
        return "Bid [id=" + id + ", auctionId=" + this.auctionId + ", amount=" + this.amount+", userId=" + this.userId + ", time=" + this.time + "]";
    }
}
