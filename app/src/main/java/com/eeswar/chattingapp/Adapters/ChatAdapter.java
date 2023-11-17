package com.eeswar.chattingapp.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.eeswar.chattingapp.Models.MessagesModel;
import com.eeswar.chattingapp.R;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter{
    ArrayList<MessagesModel> messagesModels;
    Context context;
    String recId;
    String senderRoom, receiverRoom;

    public ChatAdapter(ArrayList<MessagesModel> messagesModels, Context context) {
        this.messagesModels = messagesModels;
        this.context = context;
    }

    public ChatAdapter(ArrayList<MessagesModel> messagesModels, Context context, String recId) {
        this.messagesModels = messagesModels;
        this.context = context;
        this.recId = recId;
    }

    public ChatAdapter(ArrayList<MessagesModel> messagesModels, Context context, String recId, String senderRoom, String receiverRoom) {
        this.messagesModels = messagesModels;
        this.context = context;
        this.recId = recId;
        this.senderRoom = senderRoom;
        this.receiverRoom = receiverRoom;
    }

    int SENDER_VIEW_TYPE = 1;
    int RECEIVER_VIEW_TYPE = 2;
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType==SENDER_VIEW_TYPE) {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_sender, parent, false);
            return new SenderViewHolder(view);
        }
        else{
            View view = LayoutInflater.from(context).inflate(R.layout.sample_reciever, parent, false);
            return new RecieverViewHolder(view);
        }

    }

    @Override
    public int getItemViewType(int position) {
        MessagesModel message = messagesModels.get(position);
        if(FirebaseAuth.getInstance().getUid().equals(message.getuId())){
            return SENDER_VIEW_TYPE;
        }
        else {
            return RECEIVER_VIEW_TYPE;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessagesModel messagesModel = messagesModels.get(position);
        senderRoom = FirebaseAuth.getInstance().getUid() + recId;
        receiverRoom = recId + FirebaseAuth.getInstance().getUid();

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
                ((SenderViewHolder)holder).senderFeeling.setImageResource(reactions[pos]);
                ((SenderViewHolder)holder).senderFeeling.setVisibility(View.VISIBLE);
            }
            else {
                ((RecieverViewHolder)holder).receiverFeeling.setImageResource(reactions[pos]);
                ((RecieverViewHolder)holder).receiverFeeling.setVisibility(View.VISIBLE);
            }

            messagesModel.setFeeling(pos);

            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(senderRoom)
                    .child(messagesModel.getMessageId())
                    .setValue(messagesModel);

            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(receiverRoom)
                    .child(messagesModel.getMessageId())
                    .setValue(messagesModel);

            return true; // true is closing popup, false is requesting a new selection
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new AlertDialog.Builder(context)
                        .setTitle("Delete")
                        .setMessage("Are you sure you want to delete the message?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                String senderRoom = FirebaseAuth.getInstance().getUid()+recId;
                                database.getReference().child("chats").child(senderRoom)
                                        .child(messagesModel.getMessageId())
                                        .setValue(null);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();
                return false;
            }
        });
        if(holder.getClass()==SenderViewHolder.class){
            SenderViewHolder viewHolder = (SenderViewHolder)holder;

            if (messagesModel.getMessage().equals("photo")){
                viewHolder.senderImage.setVisibility(View.VISIBLE);
                viewHolder.SenderMsg.setVisibility(View.GONE);
                Glide.with(context)
                        .load(messagesModel.getImageUrl())
                        .placeholder(R.drawable.image_placeholder)
                        .into(viewHolder.senderImage);
            }
            SimpleDateFormat df = new SimpleDateFormat("hh:mm a");
            viewHolder.SenderTime.setText(df.format(messagesModel.getTimestamp()));
            viewHolder.SenderMsg.setText(messagesModel.getMessage());


            if (messagesModel.getFeeling() >= 0 && messagesModel.getFeeling()<7){
                messagesModel.setFeeling(reactions[messagesModel.getFeeling()]);
                viewHolder.senderFeeling.setImageResource(messagesModel.getFeeling());
                viewHolder.senderFeeling.setVisibility(View.VISIBLE);
            }
            else {
                viewHolder.senderFeeling.setVisibility(View.GONE);
            }

            viewHolder.SenderMsg.setOnTouchListener(new View.OnTouchListener() {
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
            RecieverViewHolder viewHolder = (RecieverViewHolder)holder;
            if (messagesModel.getMessage().equals("photo")){
                viewHolder.receiverImage.setVisibility(View.VISIBLE);
                viewHolder.recieverMsg.setVisibility(View.GONE);
                Glide.with(context)
                        .load(messagesModel.getImageUrl())
                        .placeholder(R.drawable.image_placeholder)
                        .into(viewHolder.receiverImage);
            }
            DateFormat df = new SimpleDateFormat("hh:mm a");
            viewHolder.receiverTime.setText(df.format(messagesModel.getTimestamp()));
            viewHolder.recieverMsg.setText(messagesModel.getMessage());

            if (messagesModel.getFeeling() >= 0 && messagesModel.getFeeling()<7){
                messagesModel.setFeeling(reactions[messagesModel.getFeeling()]);
                viewHolder.receiverFeeling.setImageResource(messagesModel.getFeeling());
                viewHolder.receiverFeeling.setVisibility(View.VISIBLE);
            }
            else {
                viewHolder.receiverFeeling.setVisibility(View.GONE);
            }

            viewHolder.recieverMsg.setOnTouchListener(new View.OnTouchListener() {
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
        return messagesModels.size();
    }

    public class RecieverViewHolder extends RecyclerView.ViewHolder{
        TextView recieverMsg, receiverTime;
        ImageView receiverFeeling, receiverImage;
        public RecieverViewHolder(@NonNull View itemView) {
            super(itemView);
            recieverMsg = itemView.findViewById(R.id.recieverText);
            receiverTime = itemView.findViewById(R.id.recieverTime);
            receiverFeeling = itemView.findViewById(R.id.recieverFeeling);
            receiverImage = itemView.findViewById(R.id.recieverImage);
        }
    }
    public class SenderViewHolder extends RecyclerView.ViewHolder{
        TextView SenderMsg, SenderTime;
        ImageView senderFeeling, senderImage;
        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            SenderMsg = itemView.findViewById(R.id.senderText);
            SenderTime = itemView.findViewById(R.id.senderTime);
            senderFeeling = itemView.findViewById(R.id.senderFeeling);
            senderImage = itemView.findViewById(R.id.senderImage);

        }
    }
}
