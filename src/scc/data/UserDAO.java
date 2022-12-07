package scc.data;

import scc.data.User;
import scc.utils.Hash;

import java.util.Arrays;

/**
 * Represents a User, as stored in the database
 */
public class UserDAO {
	private String _rid;
	private String _ts;
	private String _self;
	private String _etag;
	private String _attachments;
	private String _lsn;
	private String id;
	private String name;
	private String pwd;
	private String photoId;
	private boolean deleted;

	public UserDAO() {
	}

	public UserDAO( User u) {
		this(u.getId(), u.getName(),u.getPwd(), u.getPhotoId());
	}

	public UserDAO(String id, String name,String pwd, String photoId) {
		super();
		this.id = id;
		this.name = name;
		this.pwd = pwd;
		this.photoId = photoId;
		this.deleted = false;
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
	public String get_self() {
		return _self;
	}
	public void set_self(String _self) {
		this._self = _self;
	}
	public String get_etag() {
		return _etag;
	}
	public void set_etag(String _etag) {
		this._etag = _etag;
	}
	public String get_attachments() {
		return _attachments;
	}
	public void set_attachments(String _attachments) {
		this._attachments = _attachments;
	}
	public String get_lsn() {
		return _lsn;
	}
	public void set_lsn(String _lsn) {
		this._lsn = _lsn;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	public String getPhotoId() {
		return photoId;
	}
	public void setPhotoId(String photoId) {
		this.photoId = photoId;
	}
	public boolean isDeleted() {
		return deleted;
	}
	public void deleted() {
		this.deleted = true;
	}

	public User toUser() {
		return new User( id, name ,pwd, photoId,deleted);
	}
	@Override
	public String toString() {
		return "UserDAO [_rid=" + _rid + ", _ts=" + _ts + ", id=" + id + ", name=" + name +", pwd=" + pwd
				+ ", photoId=" + photoId + "]";
	}

}
