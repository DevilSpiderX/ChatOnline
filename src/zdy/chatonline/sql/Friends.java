package zdy.chatonline.sql;

import org.teasoft.bee.osql.SuidRich;
import org.teasoft.honey.osql.core.BeeFactory;

import java.io.Serializable;
import java.util.Objects;

public class Friends implements Serializable {

    private static final long serialVersionUID = 1598864183256L;

    private Long id;
    private String ownUid;
    private String friendUid;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Friends)) return false;
        Friends friends = (Friends) o;
        return Objects.equals(id, friends.id) && Objects.equals(ownUid, friends.ownUid) &&
                Objects.equals(friendUid, friends.friendUid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ownUid, friendUid);
    }

    @Override
    public String toString() {
        return "Friends{" +
                "id=" + id +
                ", ownUid='" + ownUid + '\'' +
                ", friendUid='" + friendUid + '\'' +
                '}';
    }

    public static void main(String[] args) {
        SuidRich suidRich = BeeFactory.getHoneyFactory().getSuidRich();
        for (Friends friends : suidRich.select(new Friends())) {
            System.out.println(friends);
        }
    }
}