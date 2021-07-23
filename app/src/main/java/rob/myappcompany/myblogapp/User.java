package rob.myappcompany.myblogapp;

public class User {
  public String image,name,thumb_image_uri;

    public String getImage() {
        return image;
    }

    public User(){

    }
    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThumb_image_uri() {
        return thumb_image_uri;
    }

    public void setThumb_image_uri(String thumb_image_uri) {
        this.thumb_image_uri = thumb_image_uri;
    }

    public User(String image, String name, String thumb_image_uri) {
        this.image = image;
        this.name = name;
        this.thumb_image_uri = thumb_image_uri;
    }
}
