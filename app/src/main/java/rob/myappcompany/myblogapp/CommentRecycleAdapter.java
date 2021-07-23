package rob.myappcompany.myblogapp;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentRecycleAdapter extends RecyclerView.Adapter<CommentRecycleAdapter.ViewHolder> {

    public List<Comment> commentsList;
    public Context context;
   public FirebaseFirestore firebaseFirestore;
   public FirebaseAuth firebaseAuth;
    public CommentRecycleAdapter(List<Comment> commentsList){

        this.commentsList = commentsList;

    }

    @Override
    public CommentRecycleAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_list_item, parent, false);
        context = parent.getContext();
        firebaseFirestore=FirebaseFirestore.getInstance();
        firebaseAuth=FirebaseAuth.getInstance();
        return new CommentRecycleAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CommentRecycleAdapter.ViewHolder holder, int position) {

        holder.setIsRecyclable(false);

        String commentMessage = commentsList.get(position).getMessage();
        holder.setComment_message(commentMessage);

        String user_id=commentsList.get(position).getUser_id();
        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    holder.setUserDescription(task.getResult().getString("name"),task.getResult().getString("image"),task.getResult().getString("thumb_image_uri"));
                }
            }
        });

        try {
            long millisecond = commentsList.get(position).getTimestamp().getTime();
            String time=commentsList.get(position).getTimestamp().toString().split(" ")[3];
            String dateString = DateFormat.format("MM/dd/yyyy", new Date(millisecond)).toString();
            holder.setTime(dateString,time);
        } catch (Exception e) {

            Log.i("Error ",e.getMessage());
            Toast.makeText(context, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();

        }
    }


    @Override
    public int getItemCount() {

        if(commentsList != null) {

            return commentsList.size();

        } else {

            return 0;

        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;

        private TextView comment_message;
        private CircleImageView commmet_image;
        private TextView comment_username;
        private TextView comment_date;
        private TextView comment_time;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setComment_message(String message){

            comment_message = mView.findViewById(R.id.comment_message);
            comment_message.setText(message);

        }

        public void setUserDescription(String userName,String userProfile,String profile_thumbUri){

            this.comment_username =mView.findViewById(R.id.comment_username);
            this.comment_username.setText(userName);


            RequestOptions placeHolderRequest=new RequestOptions();
            placeHolderRequest.placeholder(R.drawable.username1);
            commmet_image=mView.findViewById(R.id.comment_image);
            Glide.with(context).applyDefaultRequestOptions(placeHolderRequest).load(userProfile).thumbnail(Glide.with(context).load(profile_thumbUri)).into(commmet_image);
        }

         public void setTime(String date,String time){

            comment_date=mView.findViewById(R.id.comment_date);
            comment_date.setText(date);

            comment_time=mView.findViewById(R.id.comment_time);
            comment_time.setText(time);
         }
    }
}
