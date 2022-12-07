package scc.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Calendar;
import java.util.Date;

public class Auction {


    private String id;
    private String title;
    private String description;
    private String imageId;
    private String ownerId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX")
    private Date endDate;
    private float minimumPrice;
    private AuctionStatus auctionStatus;
    private Bid winnerBid;

    public Auction() {}

    public Auction(String id, String title,String description,String imageId,String ownerId,Date endDate, float minimumPrice,AuctionStatus auctionStatus, Bid winnerBid){
        super();
        this.id = id;
        this.description = description;
        this.title = title;
        this.endDate = endDate;
        this.ownerId = ownerId;
        this.imageId = imageId;
        this.minimumPrice = minimumPrice;
        this.auctionStatus = auctionStatus;
        this.winnerBid = winnerBid;
    }

    public Auction(String id, String title,String description,String imageId,String ownerId,Date endDate, float minimumPrice,AuctionStatus auctionStatus){
        super();
        this.id = id;
        this.description = description;
        this.title = title;
        this.endDate = endDate;
        this.ownerId = ownerId;
        this.imageId = imageId;
        this.minimumPrice = minimumPrice;
        this.auctionStatus = auctionStatus;
        this.winnerBid = null;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public AuctionStatus getAuctionStatus() {
        return auctionStatus;
    }

    public Date getEndDate() {
        return endDate;
    }

    public float getMinimumPrice() {
        return minimumPrice;
    }

    public String getDescription() {
        return description;
    }

    public String getImageId() {
        return imageId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getTitle() {
        return title;
    }

    public Bid getWinnerBid() {
        return this.winnerBid;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public void setMinimumPrice(float minimumPrice) {
        this.minimumPrice = minimumPrice;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public void setAuctionStatus(AuctionStatus auctionStatus) {
        this.auctionStatus = auctionStatus;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setWinnerBid(Bid winnerBid) {
        this.winnerBid = winnerBid;
    }

    @Override
    public String toString(){
        return "Auction [id=" + id + ", title=" + this.title + ", description=" + this.description+", endDate="+this.endDate+", ownerId="
        +this.ownerId+", imageId="+this.imageId+", minimumPrice="+this.minimumPrice+", auctionStatus="+this.auctionStatus+", winnerId="+this.winnerBid+"]";
    }

}
