package scc.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import scc.data.Bid;

import java.util.Arrays;
import java.util.Date;

public class BidDAO {

    private String _rid;
    private String _ts;
    private String id;
    private String userId;
    private String auctionId;
    private float amount;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    private Date time;

    public BidDAO(){}

    public BidDAO(Bid bid){
        this(bid.getId(),bid.getAuctionId(),bid.getAmount(),bid.getUserId(),bid.getTime());
    }

    public BidDAO( String id,String auctionId, float amount,String userId,Date time){
        super();
        this.auctionId = auctionId;
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.time = time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Date getTime() {
        return time;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public float getAmount() {
        return amount;
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

    public String get_rid() {
        return _rid;
    }

    public void set_rid(String _rid) {
        this._rid = _rid;
    }

    public String get_ts() {
        return _ts;
    }

    public void set_ts(String _ts) {
        this._ts = _ts;
    }

    public Bid toBid() {
        return new Bid( id,auctionId,amount,userId,time);
    }

    @Override
    public String toString(){
        return "BidDAO [_rid=" + _rid + ", _ts=" + _ts + ", id=" + id + ", auctionId=" + this.auctionId + ", amount=" + this.amount+"]";
    }

}
