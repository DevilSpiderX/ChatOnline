package zdy.chatonline.sql;

import org.teasoft.bee.osql.SuidRich;
import org.teasoft.honey.osql.core.BeeFactory;
import org.teasoft.honey.osql.core.CallableSqlLib;

import java.io.Serializable;
import java.util.Objects;

public class User implements Serializable {

    private static final long serialVersionUID = 1592387931315L;

    private String uid;
    private String password;
    private String nickname;
    private Integer age;
    private String gender;
    private String introduction;

    public User() {
    }

    public User(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(uid, user.uid) && Objects.equals(password, user.password) && Objects.equals(nickname, user.nickname) && Objects.equals(age, user.age) && Objects.equals(gender, user.gender) && Objects.equals(introduction, user.introduction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, password, nickname, age, gender, introduction);
    }

    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", password='" + password + '\'' +
                ", nickname='" + nickname + '\'' +
                ", age=" + age +
                ", gender='" + gender + '\'' +
                ", introduction='" + introduction + '\'' +
                '}';
    }

    public static int add_user(String uid, String password, String nickname, int age, String gender, String introduction) {
        CallableSqlLib call = new CallableSqlLib();
        return call.modify("add_user(?,?,?,?,?,?)",
                new Object[]{uid, password, nickname, age, gender, introduction});
    }

    public static void main(String[] args) {
        SuidRich suidRich = BeeFactory.getHoneyFactory().getSuidRich();
        for (User user : suidRich.select(new User())) {
            System.out.println(user);
        }
    }
}