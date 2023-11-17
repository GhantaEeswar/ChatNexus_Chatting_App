package com.eeswar.chattingapp.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.eeswar.chattingapp.Models.MessagesModel;
import com.eeswar.chattingapp.Models.Users;
import com.eeswar.chattingapp.R;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class GroupChatAdapter extends RecyclerView.Adapter {
    ArrayList<MessagesModel> messageModels;
    Context context;
    String recId;
    String senderRoom, receiverRoom;
    public GroupChatAdapter(ArrayList<MessagesModel> messageModels, Context context, String recId, String senderRoom, String receiverRoom) {
        this.messageModels = messageModels;
        this.context = context;
        this.recId = recId;
        this.senderRoom = senderRoom;
        this.receiverRoom = receiverRoom;
    }
    public GroupChatAdapter(ArrayList<MessagesModel> messageModels, Context context) {
        this.messageModels = messageModels;
        this.context = context;
    }
    public GroupChatAdapter(ArrayList<MessagesModel> messageModels, Context context, String recId) {
        this.messageModels = messageModels;
        this.context = context;
        this.recId = recId;
    }
    int SENDER_VIEW_TYPE = 1;
    int RECEIVER_VIEW_TYPE = 2;
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SENDER_VIEW_TYPE){
            View view = LayoutInflater.from(context).inflate(R.layout.group_sender, parent, false);
            return new SenderViewHolder(view);
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.group_reciever, parent, false);
            return new ReceiverViewHolder(view);
        }
    }
    @Override
    public int getItemViewType(int position) {
        if (messageModels.get(position).getuId().equals(FirebaseAuth.getInstance().getUid())){
            return SENDER_VIEW_TYPE;
        }
        else {
            return RECEIVER_VIEW_TYPE;
        }
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessagesModel messagesModel = messageModels.get(position);

        int reactions[] = new int[]{
                R.drawable.ic_like,
                R.drawable.ic_love,
                R.drawable.ic_care,
                R.drawable.ic_haha,
                R.drawable.ic_wow,
                R.drawable.ic_sad,
                R.drawable.ic_angry};

        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reactions)
                .build();

        ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {
            if(pos<0)
                return false;
            if (holder.getClass() == SenderViewHolder.class) {
                SenderViewHolder viewHolder = (SenderViewHolder)holder;
                viewHolder.senderFeeling.setImageResource(reactions[pos]);
                viewHolder.senderFeeling.setVisibility(View.VISIBLE);
            }
            else {
                ReceiverViewHolder viewHolder = (ReceiverViewHolder)holder;
                viewHolder.receiverFeeling.setImageResource(reactions[pos]);
                viewHolder.receiverFeeling.setVisibility(View.VISIBLE);
            }

            messagesModel.setFeeling(pos);

            FirebaseDatabase.getInstance().getReference()
                    .child("Group Chat")
                    .child(messagesModel.getMessageId())
                    .setValue(messagesModel);

            return true; // true is closing popup, false is requesting a new selection
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("Delete")
                        .setMessage("Are you sure you want to delete the message?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseDatabase database = FirebaseDatabase.getInstance();

                                database.getReference().child("Group Chat")
                                        .child(messagesModel.getMessageId())
                                        .setValue(null);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                return false;
            }
        });

        if(holder.getClass()== SenderViewHolder.class){
            SenderViewHolder viewHolder = (SenderViewHolder)holder;

            if (messagesModel.getMessage().equals("photo")){
                viewHolder.senderImage.setVisibility(View.VISIBLE);
                viewHolder.senderMsg.setVisibility(View.GONE);
                Glide.with(context)
                        .load(messagesModel.getImageUrl())
                        .placeholder(R.drawable.image_placeholder)
                        .into(viewHolder.senderImage);
            }
            FirebaseDatabase.getInstance()
                    .getReference().child("Users")
                    .child(messagesModel.getuId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()) {
                                Users user = snapshot.getValue(Users.class);
                                viewHolder.sender_name.setText("@" + user.getUserName());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
            DateFormat df = new SimpleDateFormat("hh:mm a");
            viewHolder.senderTime.setText(df.format(messagesModel.getTimestamp()));

            viewHolder.senderMsg.setText(messagesModel.getMessage());

            if (messagesModel.getFeeling() >= 0){
                messagesModel.setFeeling(reactions[messagesModel.getFeeling()]);
                viewHolder.senderFeeling.setImageResource(messagesModel.getFeeling());
                viewHolder.senderFeeling.setVisibility(View.VISIBLE);
            }
            else {
                viewHolder.senderFeeling.setVisibility(View.GONE);
            }

            viewHolder.senderMsg.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v, event);
                    return true;
                }
            });
            viewHolder.senderImage.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v, event);
                    return false;
                }
            });
        }
        else{
            ReceiverViewHolder viewHolder = (ReceiverViewHolder)holder;
            if (messagesModel.getMessage().equals("photo")){
                viewHolder.receiverImage.setVisibility(View.VISIBLE);
                viewHolder.receiverMsg.setVisibility(View.GONE);
                Glide.with(context)
                        .load(messagesModel.getImageUrl())
                        .placeholder(R.drawable.image_placeholder)
                        .into(viewHolder.receiverImage);
            }

            FirebaseDatabase.getInstance()
                    .getReference().child("Users")
                    .child(messagesModel.getuId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()) {
                                Users user = snapshot.getValue(Users.class);

                                viewHolder.receiver_name.setText("@" + user.getUserName());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
            DateFormat df = new SimpleDateFormat("hh:mm a");
            viewHolder.receiverTime.setText(df.format(messagesModel.getTimestamp()));
            viewHolder.receiverMsg.setText(messagesModel.getMessage());

            if (messagesModel.getFeeling() >= 0){
                messagesModel.setFeeling(reactions[messagesModel.getFeeling()]);
                viewHolder.receiverFeeling.setImageResource(messagesModel.getFeeling());
                viewHolder.receiverFeeling.setVisibility(View.VISIBLE);
            }
            else {
                viewHolder.receiverFeeling.setVisibility(View.GONE);
            }

            viewHolder.receiverMsg.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v, event);
                    return false;
                }
            });
            viewHolder.receiverImage.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popup.onTouch(v, event);
                    return false;
                }
            });
        }
    }
    @Override
    public int getItemCount() {
        return messageModels.size();
    }

    public class ReceiverViewHolder extends RecyclerView.ViewHolder {

        TextView receiverMsg, receiverTime, receiver_name;
        ImageView receiverFeeling, receiverImage;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            receiverMsg = itemView.findViewById(R.id.recieverText);
            receiverTime = itemView.findViewById(R.id.recieverTime);
            receiverFeeling = itemView.findViewById(R.id.recieverFeeling);
            receiverImage = itemView.findViewById(R.id.recieverImage);
            receiver_name = itemView.findViewById(R.id.reciever_name);
        }
    }

    public class SenderViewHolder extends RecyclerView.ViewHolder {

        TextView senderMsg, senderTime, sender_name;
        ImageView senderFeeling, senderImage;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMsg = itemView.findViewById(R.id.senderText);
            senderTime = itemView.findViewById(R.id.senderTime);
            senderFeeling = itemView.findViewById(R.id.senderFeeling);
            senderImage = itemView.findViewById(R.id.senderImage);
            sender_name = itemView.findViewById(R.id.sender_name);
        }
    }
}
