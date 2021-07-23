package rob.myappcompany.myblogapp;

import java.sql.Timestamp;
import java.util.Date;

public class BlogPost extends BlogPostId{


    public String user_id,image_url,thumb_image,desc;
    public Date timestamp;





    public BlogPost(){}

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public BlogPost(String user_id, String image_url, String thumb_image, String desc,Date timestamp) {
        this.user_id = user_id;
        this.image_url = image_url;
        this.thumb_image = thumb_image;
        this.desc = desc;
        this.timestamp = timestamp;
    }
    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
