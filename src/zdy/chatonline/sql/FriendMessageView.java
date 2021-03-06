package zdy.chatonline.sql;

import org.teasoft.bee.osql.SuidRich;
import org.teasoft.honey.osql.core.BeeFactory;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

public class FriendMessageView implements Serializable, Comparable<FriendMessageView> {

    private static final long serialVersionUID = 1599933065743L;

    private Long id;
    private String ownUid;
    private String friendUid;
    private String message;
    private String state;
    private Timestamp time;
    private String senderUid;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOwnUid() {
        return ownUid;
    }

    public void setOwnUid(String ownUid) {
        this.ownUid = ownUid;
    }

    public String getFriendUid() {
        return friendUid;
    }

    public void setFriendUid(String friendUid) {
        this.friendUid = friendUid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public String getSenderUid() {
        return senderUid;
    }

    public void setSenderUid(String senderUid) {
        this.senderUid = senderUid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FriendMessageView)) return false;
        FriendMessageView that = (FriendMessageView) o;
        return Objects.equals(ownUid, that.ownUid) && Objects.equals(friendUid, that.friendUid) &&
                Objects.equals(message, that.message) && Objects.equals(state, that.state) &&
                Objects.equals(time, that.time) && Objects.equals(senderUid, that.senderUid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ownUid, friendUid, message, state, time, senderUid);
    }

    @Override
    public String toString() {
        return "FriendMessageView{" +
                "id=" + id +
                ", ownUid='" + ownUid + '\'' +
                ", friendUid='" + friendUid + '\'' +
                ", message='" + message + '\'' +
                ", state='" + state + '\'' +
                ", time=" + time +
                ", senderUid='" + senderUid + '\'' +
                '}';
    }

    @Override
    public int compareTo(FriendMessageView o) {
        if (this.id != null) {
            return this.id.compareTo(o.id);
        } else if (this.time != null) {
            return this.time.compareTo(o.time);
        } else {
            return 0;
        }
    }

    public static void main(String[] args) {
        SuidRich suidRich = BeeFactory.getHoneyFactory().getSuidRich();
        for (FriendMessageView messageView : suidRich.select(new FriendMessageView())) {
            System.out.println(messageView);
        }
    }
}