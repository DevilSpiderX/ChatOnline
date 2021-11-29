package zdy.chatonline.sql;

import org.teasoft.bee.osql.SuidRich;
import org.teasoft.honey.osql.core.BeeFactory;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

public class MessageRecord implements Serializable, Comparable<MessageRecord> {

    private static final long serialVersionUID = 1596161788907L;

    private Long id;
    private String message;
    private String state;
    private Timestamp time;
    private String senderUid;
    private String receiverUid;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getReceiverUid() {
        return receiverUid;
    }

    public void setReceiverUid(String receiverUid) {
        this.receiverUid = receiverUid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageRecord)) return false;
        MessageRecord that = (MessageRecord) o;
        return Objects.equals(id, that.id) && Objects.equals(message, that.message) && Objects.equals(state, that.state) && Objects.equals(time, that.time) && Objects.equals(senderUid, that.senderUid) && Objects.equals(receiverUid, that.receiverUid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, message, state, time, senderUid, receiverUid);
    }

    @Override
    public String toString() {
        return "MessageRecord{" +
                "id=" + id +
                ", message='" + message + '\'' +
                ", state='" + state + '\'' +
                ", time=" + time +
                ", senderUid='" + senderUid + '\'' +
                ", receiverUid='" + receiverUid + '\'' +
                '}';
    }

    @Override
    public int compareTo(MessageRecord o) {
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
        for (MessageRecord record : suidRich.select(new MessageRecord())) {
            System.out.println(record);
        }
    }
}

