package com.example.instagramclone.adapter;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagramclone.R;
import com.example.instagramclone.model.Post;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyViewHolder> {

    private List<Post> mList ;
    private Context context ;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    public PostAdapter(List<Post> mList , Context context){
        this.mList = mList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.post_layout , parent , false);
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.MyViewHolder holder, int position) {
        Post post = mList.get(position);

        holder.setImgPost2(post.getImage());
        holder.imgPost2.setBackground(null);

        holder.setTxtPost(post.getText());

        long ms = post.getTime().getTime();
        String date = DateFormat.format("MM/dd/yyyy" , new Date(ms)).toString();
        holder.setTxtDate(date);

        String userID = post.getUser();
        firestore.collection("Users").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful())
                {
                    String name = task.getResult().getString("name");
                    String image = task.getResult().getString("image");

                    holder.setImgUser(image);
                    holder.setTxtNameUser(name);
                }
                else
                {
                    Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        String postId = post.PostId;
        String currentUserID = auth.getCurrentUser().getUid();
        if (currentUserID.equals(userID))
        {
            holder.imgDelete.setClickable(true);
            holder.imgDelete.setVisibility(View.VISIBLE);
            holder.imgDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(context)
                            .setTitle("Delete")
                            .setMessage("Are You Sure?")
                            .setNegativeButton("No" , null)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    firestore.collection("Posts").document(postId).delete();
                                    mList.remove(position);
                                    notifyDataSetChanged();
                                }
                            });
                    alert.show();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder{

        private ImageView imgPost2 , imgDelete ;
        private CircleImageView imgUser;
        private TextView txtNameUser , txtPost ,txtDate ;
        private View mView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            imgDelete = mView.findViewById(R.id.imgDeleteID);
        }

        public void setImgPost2(String urlPost){
            imgPost2 = mView.findViewById(R.id.imgPost2ID);
            Glide.with(context).load(urlPost).into(imgPost2);
        }

        public void setImgUser(String urlUser){
            imgUser = mView.findViewById(R.id.imgUserID);
            Glide.with(context).load(urlUser).into(imgUser);
        }

        public void setTxtNameUser(String nameUser){
            txtNameUser = mView.findViewById(R.id.txtNameUserID);
            txtNameUser.setText(nameUser);
        }

        public void setTxtPost(String post){
            txtPost = mView.findViewById(R.id.txtPostID);
            txtPost.setText(post);
        }

        public void setTxtDate(String date){
            txtDate = mView.findViewById(R.id.txtDateID);
            txtDate.setText(date);
        }
    }
}
