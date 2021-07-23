package rob.myappcompany.myblogapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogRecycleAdapter extends RecyclerView.Adapter<BlogRecycleAdapter.ViewHolder> {

    public List<BlogPost> blog_list;
    public Context context;
    public FirebaseFirestore firebaseFirestore;
    public FirebaseAuth firebaseAuth;

    public BlogRecycleAdapter(List<BlogPost> blog_list){
        this.blog_list=blog_list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item,parent,false);
        context=parent.getContext();
        firebaseAuth=FirebaseAuth.getInstance();

        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        holder.setIsRecyclable(false);

        final String blogPostId=blog_list.get(position).BlogPostId;

        String desc_data=blog_list.get(position).getDesc();
        final String currentUserId=firebaseAuth.getCurrentUser().getUid();

        holder.setDescText(desc_data);

        String image_url=blog_list.get(position).getImage_url();
        String thumb_url=blog_list.get(position).getThumb_image();
        holder.setBlogImage(image_url,thumb_url);

        String user_id=blog_list.get(position).getUser_id();

        if(user_id.equals(currentUserId)){
            holder.blogDeleteBtn.setEnabled(true);
            holder.blogDeleteBtn.setVisibility(View.VISIBLE);
        }
        firebaseFirestore=FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                     if(task.isSuccessful()){
                         holder.setUserDescription(task.getResult().getString("name"),task.getResult().getString("image"),task.getResult().getString("thumb_image_uri"));
                     }
            }
        });


        try {
            long millisecond = blog_list.get(position).getTimestamp().getTime();

            String dateString = DateFormat.format("MM/dd/yyyy", new Date(millisecond)).toString();
            holder.setTime(dateString);
        } catch (Exception e) {

            Toast.makeText(context, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();

        }

        firebaseFirestore.collection("Posts/"+blogPostId+"/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(!queryDocumentSnapshots.isEmpty()){
                    int count=queryDocumentSnapshots.size();
                    holder.updateLikeCount(count);
                }else {
                    holder.updateLikeCount(0);
                }
            }
        });

        firebaseFirestore.collection("Posts/"+blogPostId+"/Comments").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(!queryDocumentSnapshots.isEmpty()){
                    int count=queryDocumentSnapshots.size();
                    holder.updateCommentCount(count);
                }else {
                    holder.updateCommentCount(0);
                }
            }
        });


        // Get Likes
        firebaseFirestore.collection("Posts/"+blogPostId+"/Likes").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(documentSnapshot.exists()){
                    holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.drawable.ic_favorite_accent_colour_btn));
                }
                else{
                    holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.drawable.ic_favorite_border_black_24dp));
                }
            }
        });
        holder.blogLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseFirestore.collection("Posts/"+blogPostId+"/Likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                      if(task.getResult().exists())
                      {
                          firebaseFirestore.collection("Posts/"+blogPostId+"/Likes").document(currentUserId).delete();

                      }
                      else
                      {
                          Map<String,Object> likeMap=new HashMap<>();

                          likeMap.put("timestamp", FieldValue.serverTimestamp());
                          firebaseFirestore.collection("Posts/"+blogPostId+"/Likes").document(currentUserId).set(likeMap);
                      }
                    }
                });

            }
        });

        // comment part

        holder.blogCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context,commentActivity.class);
                intent.putExtra("blog_Post_id",blogPostId);
                context.startActivity(intent);
            }
        });

        holder.blogDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseFirestore.collection("Posts").document(blogPostId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        new AlertDialog.Builder(context)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Alert")
                                .setMessage("Are You Sure , Want To Delete")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        blog_list.remove(position);
                                        notifyDataSetChanged();
                                    }
                                })
                                .setNegativeButton("No",null)
                                .show();
                    }
                });
            }
        });

    }

    @Override
    public int getItemCount() {
        return blog_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private View mView;
        private TextView descView;
        private ImageView blogImageView;
        private CircleImageView blogProfileImageView;
        private TextView userName;
        private TextView blogDate;
        private ImageView blogLikeBtn;
        private TextView blogLikeCount;
        private ImageView blogCommentBtn;
        private TextView blogCommentCount;
        private Button blogDeleteBtn;

        public FirebaseFirestore firebaseFirestore;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView=itemView;

            blogCommentBtn=mView.findViewById(R.id.blog_comment_btn);
            blogLikeBtn=mView.findViewById(R.id.blog_like_btn);
            blogDeleteBtn=mView.findViewById(R.id.blog_delete_button);
        }

        public void setDescText(String descText)
        {
            descView=mView.findViewById(R.id.blog_desc);
            descView.setText(descText);
        }

        public void setBlogImage(String downloadUri,String thumbDownloadUri)
        {
            RequestOptions placeHolderRequest=new RequestOptions();
            placeHolderRequest.placeholder(R.drawable.placeholder);
            blogImageView=mView.findViewById(R.id.blog_image);
            Glide.with(context).applyDefaultRequestOptions(placeHolderRequest).load(downloadUri).thumbnail(Glide.with(context).load(thumbDownloadUri)).into(blogImageView);
        }

        public void setUserDescription(String userName,String userProfile,String profile_thumbUri){

            this.userName =mView.findViewById(R.id.blog_user_name);
            this.userName.setText(userName);


            RequestOptions placeHolderRequest=new RequestOptions();
            placeHolderRequest.placeholder(R.drawable.username1);
            blogProfileImageView=mView.findViewById(R.id.blog_user_image);
            Glide.with(context).applyDefaultRequestOptions(placeHolderRequest).load(userProfile).thumbnail(Glide.with(context).load(profile_thumbUri)).into(blogProfileImageView);
        }

        public void setTime(String date){
            blogDate=mView.findViewById(R.id.blog_date);
            blogDate.setText(date);
        }


        public void updateLikeCount(int count){

            blogLikeCount=mView.findViewById(R.id.blog_like_count);
            blogLikeCount.setText(count+" Likes");
        }
        public void updateCommentCount(int count){
            blogCommentCount=mView.findViewById(R.id.blog_Comments_Count);
            blogCommentCount.setText(count+" Comments");
        }

    }

}
