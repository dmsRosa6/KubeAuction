package scc.data;

public class Question {
    private String id;
    private String auctionId;
    private String userId;
    private String message;
    private String reply;

    public Question(){}

    public Question(String id, String auctionId, String userId,String message){
        super();
        this.auctionId = auctionId;
        this.id = id;
        this.userId = userId;
        this.message = message;
        this.reply = null;
    }

    public Question(String id, String auctionId, String userId, String message, String reply) {
        super();
        this.auctionId = auctionId;
        this.id = id;
        this.userId = userId;
        this.message = message;
        this.reply = reply;
    }

    public String getId() {
        return id;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getMessage() {
        return message;
    }

    public String getReply() {
        return reply;
    }

    public String getUserId() {
        return userId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString(){
        return "Question [id=" + id + ", auctionId=" + auctionId + ", userId=" + userId + ", message="+message+", reply="+reply+"]";
    }

}
