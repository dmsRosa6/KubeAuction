package scc.data;

import com.azure.cosmos.*;
import com.azure.cosmos.models.*;
import com.azure.cosmos.util.CosmosPagedIterable;
import scc.data.Auction;
import scc.data.AuctionDAO;
import scc.data.AuctionStatus;
import scc.data.Bid;
import scc.data.BidDAO;
import scc.data.Question;
import scc.data.QuestionDAO;
import scc.data.User;
import scc.data.UserDAO;

import java.util.Date;


public class CosmosDBLayer {
	private static final String CONNECTION_URL = "https://scc23rosatiago.documents.azure.com:443/";
	//private static final String DB_KEY = System.getenv("DB_KEY");
	private static final String DB_KEY = "icoEY7EREbi8lvvGZ78mfdBMNeObPCEfWY7SMmcKgNiAClVT13E1A6YhCCkKzIwpMNAncWdQUVL3ACDbYDpyPg==";
	private static final String DB_NAME = "scc23dbrosatiago";
	private static final String CLOSING_TIME_HOURS = "1";


	private static CosmosDBLayer instance;

	public static synchronized CosmosDBLayer getInstance() {
		if( instance != null)
			return instance;

		CosmosClient client = new CosmosClientBuilder()
				.endpoint(CONNECTION_URL)
				.key(DB_KEY)
				//.directMode()
				.gatewayMode()
				// replace by .directMode() for better performance
				.consistencyLevel(ConsistencyLevel.SESSION)
				.connectionSharingAcrossClientsEnabled(true)
				.contentResponseOnWriteEnabled(true)
				.buildClient();
		instance = new CosmosDBLayer( client);
		return instance;

	}

	private CosmosClient client;
	private CosmosDatabase db;
	private CosmosContainer users;
	private CosmosContainer auctions;
	private CosmosContainer bids;
	private CosmosContainer questions;

	private CosmosContainer popularAuctions;


	public CosmosDBLayer(CosmosClient client) {
		this.client = client;
	}

	private synchronized void init() {
		if( db != null)
			return;
		db = client.getDatabase(DB_NAME);
		users = db.getContainer("users");
		auctions = db.getContainer("auctions");
		bids = db.getContainer("bids");
		questions = db.getContainer("questions");
		popularAuctions = db.getContainer("popularAuctions");
	}

	public CosmosItemResponse<Question> putQuestion(Question question){
		init();
		return questions.createItem(question);
	}

	public CosmosPagedIterable<QuestionDAO> getQuestionById(String id){
		init();
		return questions.queryItems("SELECT * FROM questions WHERE questions.id=\"" + id + "\"", new CosmosQueryRequestOptions(), QuestionDAO.class);
	}

	public CosmosPagedIterable<QuestionDAO> listQuestions(String auctionId) {
		init();
		return questions.queryItems("SELECT * FROM questions WHERE questions.auctionId=\"" + auctionId + "\"", new CosmosQueryRequestOptions(), QuestionDAO.class);
	}

	public CosmosItemResponse<Question> replyQuestion(String id, String reply){
		init();
		PartitionKey key = new PartitionKey( id);
		var getQuestionResponse = questions.queryItems("SELECT * FROM questions WHERE questions.id=\"" + id + "\"", new CosmosQueryRequestOptions(), QuestionDAO.class);
		var question = getQuestionResponse.iterator().next().toQuestion();
		question.setReply(reply);
		questions.deleteItem(id,key,new CosmosItemRequestOptions());
		return questions.createItem(question);
	}

	public CosmosItemResponse<Bid> putBid(Bid bid){
		init();
		return bids.createItem(bid);
	}

	public CosmosPagedIterable<BidDAO> listAuctionBids(String id) {
		init();
		return bids.queryItems("SELECT * FROM bids WHERE bids.auctionId=\"" + id + "\" ORDER BY bids.amount DESC", new CosmosQueryRequestOptions(), BidDAO.class);
	}

	public CosmosItemResponse<Auction> putAuction(Auction auction){
		init();
		return auctions.createItem(auction);
	}
	public CosmosItemResponse<Auction> updateAuction(String auctionId, Auction newAuction){
		init();
		PartitionKey key = new PartitionKey(auctionId);
		return auctions.replaceItem(newAuction,auctionId,key,new CosmosItemRequestOptions());
	}

	public CosmosItemResponse<User> softDeleteUser(String userId, User user){
		user.delete();
		init();
		PartitionKey key = new PartitionKey(userId);
		return users.replaceItem(user,userId,key,new CosmosItemRequestOptions());
	}

	public CosmosItemResponse<User> updateUser(String userId, User user){
		init();
		PartitionKey key = new PartitionKey(userId);
		return users.replaceItem(user,userId,key,new CosmosItemRequestOptions());
	}

	public CosmosPagedIterable<AuctionDAO> getAuctionById(String id){
		init();
		return auctions.queryItems("SELECT * FROM auctions WHERE auctions.id=\"" + id + "\"", new CosmosQueryRequestOptions(), AuctionDAO.class);
	}

	public CosmosPagedIterable<AuctionDAO> getUsersAuctionsById(String userId, String auctionStatus){
		init();
		if(auctionStatus == null)
			return auctions.queryItems("SELECT * FROM auctions WHERE auctions.ownerId=\"" + userId + "\"", new CosmosQueryRequestOptions(), AuctionDAO.class);
		return auctions.queryItems("SELECT * FROM auctions WHERE auctions.auctionStatus=\""+ auctionStatus+"\" AND auctions.ownerId=\"" + userId +"\"", new CosmosQueryRequestOptions(), AuctionDAO.class);
	}

	public CosmosPagedIterable<BidDAO> getUserFollowedAuctions(String userId){
		init();
		return bids.queryItems("SELECT bids.auctionId FROM bids WHERE bids.userId=\"" + userId + "\" GROUP BY bids.auctionId", new CosmosQueryRequestOptions(), BidDAO.class);
	}

	public CosmosPagedIterable<AuctionDAO> getClosingAuctions(){
		init();
		return auctions.queryItems("SELECT * FROM auctions  WHERE auctions.endDate >= GetCurrentDateTime() AND auctions.endDate <=  DateTimeAdd(\"hh\","+CLOSING_TIME_HOURS+",GetCurrentDateTime())", new CosmosQueryRequestOptions(), AuctionDAO.class);
	}

	public CosmosPagedIterable<AuctionDAO> listPopularAuctions() {
		init();
		return popularAuctions.queryItems("SELECT * FROM popularAuctions", new CosmosQueryRequestOptions(), AuctionDAO.class);
	}

	public CosmosItemResponse<User> putUser(User user) {
		init();
		return users.createItem(user);
	}

	public CosmosPagedIterable<UserDAO> getUserById(String id) {
		init();
		return users.queryItems("SELECT * FROM users WHERE users.id=\"" + id + "\"", new CosmosQueryRequestOptions(), UserDAO.class);
	}

	public CosmosPagedIterable<UserDAO> getUsers() {
		init();
		return users.queryItems("SELECT * FROM users ", new CosmosQueryRequestOptions(), UserDAO.class);
	}

	public void close() {
		client.close();
	}


}