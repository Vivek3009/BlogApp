package rob.myappcompany.myblogapp;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.Exclude;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

public class BlogPostId {

    @Exclude
    public String BlogPostId;
    public <T extends BlogPostId > T withId(@NonNull final String id){
    this.BlogPostId=id;
    return (T) this;
    }
}


