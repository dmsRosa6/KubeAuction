package scc.data;

import reactor.netty.http.Http2SslContextSpec;
import scc.data.Question;

public class QuestionDAO {

    private String _rid;
    private String _ts;
    private String id;
    private String auctionId;
    private String userId;
    private String message;
    private String reply;

    public QuestionDAO(){}

    public QuestionDAO(Question question){
        this(question.getId(),question.getAuctionId(),question.getUserId(),question.getMessage());
    }

    public QuestionDAO(String id, String auctionId, String userId,String message){
        super();
        this.auctionId = auctionId;
        this.id = id;
        this.userId = userId;
        this.message = message;
        this.reply = null;
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

    public Question toQuestion() {
        return new Question(id,auctionId,userId,message,reply);
    }

    @Override
    public String toString() {
        return "QuestionDAO [_rid=" + _rid + ", _ts=" + _ts + ", id=" + id + ", auctionId=" + auctionId + ", userId="+auctionId+", message=" + message
                + ", reply=" + reply + "]";
    }
}
