package akb.chat.com;

import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private List<Messages> list;

    public MessageAdapter(List<Messages> list) {
        this.list = list;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView smsgtxt, rmsgtxt;
        public CircleImageView rdp;
        public ImageView senderImage, recImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            smsgtxt = (TextView) itemView.findViewById(R.id.sender_messsage_text);
            rmsgtxt = (TextView) itemView.findViewById(R.id.receiver_message_text);
            rdp = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
            senderImage = (ImageView) itemView.findViewById(R.id.message_sender_image_view);
            recImage = (ImageView) itemView.findViewById(R.id.message_receiver_image_view);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_messages_layout, viewGroup, false);

        mAuth = FirebaseAuth.getInstance();
        //usersRef = FirebaseDatabase.getInstance().getReference().child("Users")

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, final int pos) {

        String msid = mAuth.getCurrentUser().getUid();
        Messages messages = list.get(pos);

        String fromuserid = messages.getFrom();
        String frommsgtype = messages.getType();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromuserid);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("image")) {
                    String recdp = dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(recdp).placeholder(R.drawable.profile_image).into(messageViewHolder.rdp);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        messageViewHolder.rmsgtxt.setVisibility(View.GONE);
        messageViewHolder.rdp.setVisibility(View.GONE);
        messageViewHolder.smsgtxt.setVisibility(View.GONE);
        messageViewHolder.senderImage.setVisibility(View.GONE);
        messageViewHolder.recImage.setVisibility(View.GONE);

        if (frommsgtype.equals("text")) {

            if (fromuserid.equals(msid)) {
                messageViewHolder.smsgtxt.setVisibility(View.VISIBLE);
                messageViewHolder.smsgtxt.setBackgroundResource(R.drawable.sender_message_layout);
                messageViewHolder.smsgtxt.setTextColor(Color.BLACK);
                messageViewHolder.smsgtxt.setText(messages.getMessage());
            } else {
                messageViewHolder.rdp.setVisibility(View.VISIBLE);
                messageViewHolder.rmsgtxt.setVisibility(View.VISIBLE);

                messageViewHolder.rmsgtxt.setBackgroundResource(R.drawable.rec_message_layout);
                messageViewHolder.rmsgtxt.setTextColor(Color.BLACK);
                messageViewHolder.rmsgtxt.setText(messages.getMessage());

            }
        } else if (frommsgtype.equals("image")) {
            if (fromuserid.equals(msid)) {
                messageViewHolder.senderImage.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(messageViewHolder.senderImage);
            } else {
                messageViewHolder.recImage.setVisibility(View.VISIBLE);
                messageViewHolder.rdp.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(messageViewHolder.recImage);
            }
        } else if (frommsgtype.equals("pdf") || frommsgtype.equals("docx")) {
            if (fromuserid.equals(msid)) {
                messageViewHolder.senderImage.setVisibility(View.VISIBLE);
                Picasso.get()
                        .load("https://firebasestorage.googleapis.com/v0/b/fir-notification-3d8ea.appspot.com/o/file.png?alt=media&token=4f05853e-9acc-423f-9b19-a265e1e23a9c")
                        .into(messageViewHolder.senderImage);
            } else {
                messageViewHolder.recImage.setVisibility(View.VISIBLE);
                messageViewHolder.rdp.setVisibility(View.VISIBLE);

                Picasso.get()
                        .load("https://firebasestorage.googleapis.com/v0/b/fir-notification-3d8ea.appspot.com/o/file.png?alt=media&token=4f05853e-9acc-423f-9b19-a265e1e23a9c")
                        .into(messageViewHolder.recImage);

            }
        }


        if (fromuserid == msid) {

            messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (list.get(pos).getType().equals("pdf") || (list.get(pos).getType().equals("docx"))) {
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(list.get(pos).getMessage()));
                        messageViewHolder.itemView.getContext().startActivity(i);
                    }
                    else if (list.get(pos).getType().equals("image")) {
                        Intent i = new Intent(messageViewHolder.itemView.getContext(), ImageViewerActivity.class);
                        i.putExtra("url", list.get(pos).getMessage());
                        messageViewHolder.itemView.getContext().startActivity(i);
                    }
                }
            });

            messageViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    Vibrator vibe = (Vibrator)messageViewHolder.itemView.getContext().getSystemService(Context.VIBRATOR_SERVICE);
                    vibe.vibrate(50);

                    if (list.get(pos).getType().equals("pdf") || (list.get(pos).getType().equals("docx"))) {
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "Download and view this document",
                                "Cancel",
                                "Delete for everyone"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete file?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {

                                    AlertDialog.Builder builder
                                            = new AlertDialog
                                            .Builder(messageViewHolder.itemView.getContext());

                                    builder.setMessage("Are you sure you want to delete this document?");
                                    builder.setTitle("Alert !");
                                    builder.setCancelable(false);

                                    builder.setPositiveButton(
                                            "Yes",
                                            new DialogInterface.OnClickListener() {

                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int which)
                                                {
                                                    deleteSentMessage(pos, messageViewHolder);

                                                    Intent i = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                                    messageViewHolder.itemView.getContext().startActivity(i);
                                                }
                                            });

                                    builder.setNegativeButton(
                                            "No",
                                            new DialogInterface.OnClickListener() {

                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int which)
                                                {
                                                    dialog.cancel();
                                                }
                                            });
                                    AlertDialog alertDialog = builder.create();

                                    alertDialog.show();
                                }

                                else if (which == 1) {
                                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(list.get(pos).getMessage()));
                                    messageViewHolder.itemView.getContext().startActivity(i);
                                } else if (which == 3) {

                                    AlertDialog.Builder builder
                                            = new AlertDialog
                                            .Builder(messageViewHolder.itemView.getContext());

                                    builder.setMessage("Are you sure you want to delete this document for everyone?");
                                    builder.setTitle("Alert !");
                                    builder.setCancelable(false);

                                    builder.setPositiveButton(
                                            "Yes",
                                            new DialogInterface.OnClickListener() {

                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int which)
                                                {
                                                    deleteForEveryone(pos, messageViewHolder);

                                                    Intent i = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                                    messageViewHolder.itemView.getContext().startActivity(i);
                                                }
                                            });

                                    builder.setNegativeButton(
                                            "No",
                                            new DialogInterface.OnClickListener() {

                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int which)
                                                {
                                                    dialog.cancel();
                                                }
                                            });
                                    AlertDialog alertDialog = builder.create();

                                    alertDialog.show();
                                }
                            }
                        });
                        builder.show();
                    }

                    else if (list.get(pos).getType().equals("text")) {
                        CharSequence options[] = new CharSequence[]{
                                "Copy this message",
                                "Delete for me",
                                "Cancel",
                                "Delete for everyone"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    ClipboardManager cm = (ClipboardManager)messageViewHolder.itemView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                    cm.setText(list.get(pos).getMessage());
                                    Toast.makeText(messageViewHolder.itemView.getContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show();
                                }
                                else if (which == 1) {

                                    AlertDialog.Builder builder
                                            = new AlertDialog
                                            .Builder(messageViewHolder.itemView.getContext());

                                    builder.setMessage("Are you sure you want to delete this message?");
                                    builder.setTitle("Alert !");
                                    builder.setCancelable(false);

                                    builder.setPositiveButton(
                                            "Yes",
                                            new DialogInterface.OnClickListener() {

                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int which)
                                                {
                                                    deleteSentMessage(pos, messageViewHolder);

                                                    Intent i = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                                    messageViewHolder.itemView.getContext().startActivity(i);
                                                }
                                            });

                                    builder.setNegativeButton(
                                            "No",
                                            new DialogInterface.OnClickListener() {

                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int which)
                                                {
                                                    dialog.cancel();
                                                }
                                            });
                                    AlertDialog alertDialog = builder.create();

                                    alertDialog.show();
                                }

                                else if (which == 3) {

                                    AlertDialog.Builder builder
                                            = new AlertDialog
                                            .Builder(messageViewHolder.itemView.getContext());

                                    builder.setMessage("Are you sure you want to delete this message for everyone?");
                                    builder.setTitle("Alert !");
                                    builder.setCancelable(false);

                                    builder.setPositiveButton(
                                            "Yes",
                                            new DialogInterface.OnClickListener() {

                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int which)
                                                {
                                                    deleteForEveryone(pos, messageViewHolder);

                                                    Intent i = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                                    messageViewHolder.itemView.getContext().startActivity(i);
                                                }
                                            });

                                    builder.setNegativeButton(
                                            "No",
                                            new DialogInterface.OnClickListener() {

                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int which)
                                                {
                                                    dialog.cancel();
                                                }
                                            });
                                    AlertDialog alertDialog = builder.create();

                                    alertDialog.show();
                                }
                            }
                        });
                        builder.show();
                    }

                    else if (list.get(pos).getType().equals("image")) {
                        CharSequence options[] = new CharSequence[]{
                                "Delete for me",
                                "View this image",
                                "Cancel",
                                "Delete for everyone"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete image?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {

                                    AlertDialog.Builder builder
                                            = new AlertDialog
                                            .Builder(messageViewHolder.itemView.getContext());

                                    builder.setMessage("Are you sure you want to delete this image?");
                                    builder.setTitle("Alert !");
                                    builder.setCancelable(false);

                                    builder.setPositiveButton(
                                            "Yes",
                                            new DialogInterface.OnClickListener() {

                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int which)
                                                {
                                                    deleteSentMessage(pos, messageViewHolder);

                                                    Intent i = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                                    messageViewHolder.itemView.getContext().startActivity(i);
                                                }
                                            });

                                    builder.setNegativeButton(
                                            "No",
                                            new DialogInterface.OnClickListener() {

                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int which)
                                                {
                                                    dialog.cancel();
                                                }
                                            });
                                    AlertDialog alertDialog = builder.create();

                                    alertDialog.show();

                                }

                                else if (which == 1) {
                                    Intent i = new Intent(messageViewHolder.itemView.getContext(), ImageViewerActivity.class);
                                    i.putExtra("url", list.get(pos).getMessage());
                                    messageViewHolder.itemView.getContext().startActivity(i);
                                } else if (which == 3) {

                                    AlertDialog.Builder builder
                                            = new AlertDialog
                                            .Builder(messageViewHolder.itemView.getContext());

                                    builder.setMessage("Are you sure you want to delete this image for everyone?");
                                    builder.setTitle("Alert !");
                                    builder.setCancelable(false);

                                    builder.setPositiveButton(
                                            "Yes",
                                            new DialogInterface.OnClickListener() {

                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int which)
                                                {
                                                    deleteForEveryone(pos, messageViewHolder);

                                                    Intent i = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                                    messageViewHolder.itemView.getContext().startActivity(i);
                                                }
                                            });

                                    builder.setNegativeButton(
                                            "No",
                                            new DialogInterface.OnClickListener() {

                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int which)
                                                {
                                                    dialog.cancel();
                                                }
                                            });
                                    AlertDialog alertDialog = builder.create();

                                    alertDialog.show();
                                }
                            }
                        });
                        builder.show();
                    }

                    return false;
                }
            });
        }

        else {

            messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (list.get(pos).getType().equals("pdf") || (list.get(pos).getType().equals("docx"))) {
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(list.get(pos).getMessage()));
                        messageViewHolder.itemView.getContext().startActivity(i);
                    }
                    else if (list.get(pos).getType().equals("image")) {
                        Intent i = new Intent(messageViewHolder.itemView.getContext(), ImageViewerActivity.class);
                        i.putExtra("url", list.get(pos).getMessage());
                        messageViewHolder.itemView.getContext().startActivity(i);
                    }
                }
            });

            messageViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Vibrator vibe = (Vibrator)messageViewHolder.itemView.getContext().getSystemService(Context.VIBRATOR_SERVICE);
                    vibe.vibrate(50);

                    if (list.get(pos).getType().equals("pdf") || (list.get(pos).getType().equals("docx"))) {
                        CharSequence options[] = new CharSequence[]{
                                "Delete this document",
                                "Download and view this document",
                                "Cancel",
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete file?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {

                                    AlertDialog.Builder builder
                                            = new AlertDialog
                                            .Builder(messageViewHolder.itemView.getContext());

                                    builder.setMessage("Are you sure you want to delete this document?");
                                    builder.setTitle("Alert !");
                                    builder.setCancelable(false);

                                    builder.setPositiveButton(
                                            "Yes",
                                            new DialogInterface.OnClickListener() {

                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int which)
                                                {
                                                    deleteReceivedMessage(pos, messageViewHolder);

                                                    Intent i = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                                    messageViewHolder.itemView.getContext().startActivity(i);
                                                }
                                            });

                                    builder.setNegativeButton(
                                            "No",
                                            new DialogInterface.OnClickListener() {

                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int which)
                                                {
                                                    dialog.cancel();
                                                }
                                            });
                                    AlertDialog alertDialog = builder.create();

                                    alertDialog.show();
                                }

                                else if (which == 1) {
                                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(list.get(pos).getMessage()));
                                    messageViewHolder.itemView.getContext().startActivity(i);
                                }
                            }
                        });
                        builder.show();
                    } else if (list.get(pos).getType().equals("text")) {
                        CharSequence options[] = new CharSequence[]{
                                "Copy this message",
                                "Delete message",
                                "Cancel",
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete message?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0)
                                {
                                    ClipboardManager cm = (ClipboardManager)messageViewHolder.itemView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                    cm.setText(list.get(pos).getMessage());
                                    Toast.makeText(messageViewHolder.itemView.getContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show();
                                }
                                else if (which == 1) {

                                    AlertDialog.Builder builder
                                            = new AlertDialog
                                            .Builder(messageViewHolder.itemView.getContext());

                                    builder.setMessage("Are you sure you want to delete this message?");
                                    builder.setTitle("Alert !");
                                    builder.setCancelable(false);

                                    builder.setPositiveButton(
                                            "Yes",
                                            new DialogInterface.OnClickListener() {

                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int which)
                                                {
                                                    deleteReceivedMessage(pos, messageViewHolder);

                                                    Intent i = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                                    messageViewHolder.itemView.getContext().startActivity(i);
                                                }
                                            });

                                    builder.setNegativeButton(
                                            "No",
                                            new DialogInterface.OnClickListener() {

                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int which)
                                                {
                                                    dialog.cancel();
                                                }
                                            });
                                    AlertDialog alertDialog = builder.create();

                                    alertDialog.show();
                                }
                            }
                        });
                        builder.show();
                    } else if (list.get(pos).getType().equals("image")) {
                        CharSequence options[] = new CharSequence[]{
                                "Delete this image",
                                "View this image",
                                "Cancel",
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                        builder.setTitle("Delete image?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {

                                    AlertDialog.Builder builder
                                            = new AlertDialog
                                            .Builder(messageViewHolder.itemView.getContext());

                                    builder.setMessage("Are you sure you want to delete this document?");
                                    builder.setTitle("Alert !");
                                    builder.setCancelable(false);

                                    builder.setPositiveButton(
                                            "Yes",
                                            new DialogInterface.OnClickListener() {

                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int which)
                                                {
                                                    deleteReceivedMessage(pos, messageViewHolder);

                                                    Intent i = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                                    messageViewHolder.itemView.getContext().startActivity(i);
                                                }
                                            });

                                    builder.setNegativeButton(
                                            "No",
                                            new DialogInterface.OnClickListener() {

                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int which)
                                                {
                                                    dialog.cancel();
                                                }
                                            });
                                    AlertDialog alertDialog = builder.create();

                                    alertDialog.show();
                                } else if (which == 1) {
                                    Intent i = new Intent(messageViewHolder.itemView.getContext(), ImageViewerActivity.class);
                                    i.putExtra("url", list.get(pos).getMessage());
                                    messageViewHolder.itemView.getContext().startActivity(i);
                                }
                            }
                        });
                        builder.show();
                    }


                    return false;
                }
            });
        }

    }


    private void deleteSentMessage(final int pos, final MessageViewHolder holder) {
        DatabaseReference RootRef = FirebaseDatabase.getInstance().getReference();
        RootRef.child("Messages")
                .child(list.get(pos).getFrom())
                .child(list.get(pos).getTo())
                .child(list.get(pos).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(holder.itemView.getContext(), "Deleted successfully", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Error occured", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteReceivedMessage(final int pos, final MessageViewHolder holder) {
        DatabaseReference RootRef = FirebaseDatabase.getInstance().getReference();
        RootRef.child("Messages")
                .child(list.get(pos).getTo())
                .child(list.get(pos).getFrom())
                .child(list.get(pos).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(holder.itemView.getContext(), "Deleted successfully", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Error occured", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteForEveryone(final int pos, final MessageViewHolder holder) {
        final DatabaseReference RootRef = FirebaseDatabase.getInstance().getReference();
        RootRef.child("Messages")
                .child(list.get(pos).getTo())
                .child(list.get(pos).getFrom())
                .child(list.get(pos).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    RootRef.child("Messages")
                            .child(list.get(pos).getFrom())
                            .child(list.get(pos).getTo())
                            .child(list.get(pos).getMessageID())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(holder.itemView.getContext(), "Deleted for everyone successfully", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Error occured", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

}
