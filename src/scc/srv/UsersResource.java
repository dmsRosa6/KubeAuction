package scc.srv;

import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.Mongo;
import com.sun.tools.xjc.model.CAdapter;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import redis.clients.jedis.Jedis;
import scc.cache.RedisCache;
import scc.data.*;
import scc.utils.Hash;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Path("/user")
public class UsersResource {

    private static final String USER_ALREADY_EXISTS_EXCEPTION = "User already exists";
    public static final String USER_DOES_NOT_EXIST_EXCEPTION = "User does not exist";
    private static final String INCORRECT_LOGIN_EXCEPTION = "Incorrect login";

    private static final String NO_SESSION_EXCEPTION = "No session initialized";
    private static final String NO_VALID_SESSION_EXCEPTION = "No valid session initialized";
    private static final String INVALID_USER = "Invalid user : ";
    private static final String INVALID_USER_NULL_PARAMS = "Invalid user - null params";

    public static final String COOKIE_PARAM_SESSION = "scc:session";

    private MongoDBLayer db;

    private ObjectMapper mapper;

    public UsersResource(){
        db = MongoDBLayer.getInstance();
        mapper = new ObjectMapper();
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public User putUser(User user){
        User UserResponse =null;

        isUserValid(user);

        user.setPwd(Hash.of(user.getPwd()));

        try{
            Jedis jedis =null;
            if(RedisCache.USE_CACHE) {
                jedis = RedisCache.getCachePool().getResource();
                String cacheRes = jedis.get(RedisCache.CACHE_USER_PREFIX + user.getId());
                if (cacheRes != null) throw new WebApplicationException(USER_ALREADY_EXISTS_EXCEPTION);
            }
            var userExistsResponse = db.getUser(user.getId());
            if(userExistsResponse == null) throw new WebApplicationException(USER_ALREADY_EXISTS_EXCEPTION);

            UserResponse = db.putUser(user);

            if(RedisCache.USE_CACHE) jedis.setex(RedisCache.CACHE_USER_PREFIX+user.getId(), RedisCache.DEFAULT_CACHE_TIMEOUT, mapper.writeValueAsString(UserResponse));
        }catch (Exception e) {
            e.printStackTrace();
        }
        return UserResponse;
    }


    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public User updateUser(@CookieParam(COOKIE_PARAM_SESSION) Cookie session, @PathParam("id") String userId, User user){

        User oldUser = getUserById(userId);
        User updatedUser = null;

        checkCookieUser(session,oldUser.getId());

        if(user.getName() != null) oldUser.setName(user.getName());
        if(user.getPwd() != null) oldUser.setPwd(user.getPwd());
        if(user.getPhotoId() != null) oldUser.setName(user.getPhotoId());

        updatedUser = db.updateUser(userId,oldUser);
        if(RedisCache.USE_CACHE) {
            try (Jedis jedis = RedisCache.getCachePool().getResource()) {
                jedis.setex(RedisCache.CACHE_USER_PREFIX + userId, RedisCache.DEFAULT_CACHE_TIMEOUT, mapper.writeValueAsString(updatedUser));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return updatedUser;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public User getUser(@PathParam("id") String userId){
        return getUserById(userId);
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public User deleteUser(@CookieParam(UsersResource.COOKIE_PARAM_SESSION) Cookie session, @PathParam("id") String userId) {
        checkCookieUser(session,userId);
        User user = getUserById(userId);
        return db.deleteUser(userId,user);
    }

    /**
    @GET
    @Path("/{id}/auctions")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Auction> listUserAuctions(@PathParam("id") String userId,
                                          @QueryParam("auctionStatus") String auctionStatus) {
        getUserById(userId);
        List<Auction> auctions = new ArrayList<>();
        CosmosPagedIterable<AuctionDAO> result = db.getUsersAuctionsById(userId,auctionStatus);
        Iterator<AuctionDAO> ite = result.iterator();
        while (ite.hasNext()){
            auctions.add(ite.next().toAuction());
        }
        return auctions;
    }

    @GET
    @Path("/{id}/following")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> listUserFollowedAuctions(@PathParam("id") String userId){
        User user = getUserById(userId);
        List<String> auctionsId = new ArrayList<>();
        CosmosPagedIterable<BidDAO> result = db.getUserFollowedAuctions(userId);
        Iterator<BidDAO> ite = result.iterator();
        while (ite.hasNext()){
            BidDAO bid = ite.next();
            auctionsId.add(ite.next().toBid().getAuctionId());
        }
        return auctionsId;
    }
**/
    @POST
    @Path("/auth")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response auth(Login login){
        boolean pwdOK = false;

        User user = getUserById(login.getUserId());

        if (user == null) throw new WebApplicationException();

        pwdOK = user.getPwd().equals(Hash.of(login.getPwd()));

        if(pwdOK){
            String uid = UUID.randomUUID().toString();
            NewCookie cookie = new NewCookie.Builder(COOKIE_PARAM_SESSION).value(uid).path("/").comment("sessionid").maxAge(3600).secure(false).httpOnly(true).build();

            try (Jedis jedis = RedisCache.getCachePool().getResource()) {
                Session s = new Session(login.getUserId(), user.getName());
                jedis.setex(RedisCache.CACHE_SESSION_PREFIX+login.getUserId(),RedisCache.DEFAULT_CACHE_TIMEOUT,mapper.writeValueAsString(s));
            }catch (Exception e){
                e.printStackTrace();
            }
            return Response.ok().cookie(cookie).build();
        }
        else
            throw new NotAuthorizedException(INCORRECT_LOGIN_EXCEPTION);
    }

    public static Session checkCookieUser(Cookie session, String id) throws NotAuthorizedException {
        if (session == null || session.getValue() == null) throw new NotAuthorizedException(NO_SESSION_EXCEPTION);

        Session s = null;

        try (Jedis jedis = RedisCache.getCachePool().getResource()) {
            ObjectMapper mapper = new ObjectMapper();
            String cacheRes = jedis.get(RedisCache.CACHE_SESSION_PREFIX + id);
            s = mapper.readValue(cacheRes, Session.class);
        } catch (Exception e) {
            throw new NotAuthorizedException(NO_VALID_SESSION_EXCEPTION);
        }

        if (s == null || s.getUserId() == null)
            throw new NotAuthorizedException(NO_VALID_SESSION_EXCEPTION);
        if (!s.getUserId().equals(id))
            throw new NotAuthorizedException(INVALID_USER + s.getUserId());

        return s;
    }


    private User getUserById(String id){
        User response = null;

        try {
            Jedis jedis = null;
            if(RedisCache.USE_CACHE) {
                jedis = RedisCache.getCachePool().getResource();
                String cacheRes = jedis.get(RedisCache.CACHE_USER_PREFIX + id);
                if (cacheRes != null) {
                    return mapper.readValue(cacheRes, User.class);
                }
            }
            response = db.getUser(id);
            if(response == null) throw new WebApplicationException(USER_DOES_NOT_EXIST_EXCEPTION);
            if(RedisCache.USE_CACHE) jedis.setex(RedisCache.CACHE_USER_PREFIX+id,RedisCache.DEFAULT_CACHE_TIMEOUT,mapper.writeValueAsString(response));

        }catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    private void isUserValid(User user){
        if (user.getId() == null) throw  new WebApplicationException(INVALID_USER_NULL_PARAMS);
        if (user.getPwd() == null) throw  new WebApplicationException(INVALID_USER_NULL_PARAMS);
        if (user.getName() == null) throw  new WebApplicationException(INVALID_USER_NULL_PARAMS);
        if (user.getPhotoId() == null) throw  new WebApplicationException(INVALID_USER_NULL_PARAMS);
    }
}