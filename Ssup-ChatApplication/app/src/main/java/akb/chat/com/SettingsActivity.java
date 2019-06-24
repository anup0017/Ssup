package akb.chat.com;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button update;
    private EditText userName, status;
    private CircleImageView dp;

    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    private StorageReference UserProfileStorageRef;

    private ProgressDialog progressDialog;
    private Toolbar toolbar;

    private String imageUrl = null;

    private static final int galleryPick = 1;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(SettingsActivity.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        UserProfileStorageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        progressDialog = new ProgressDialog(this);
        InitializeFields();

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSettings();
            }
        });

        dp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setAction(Intent.ACTION_GET_CONTENT);
                i.setType("image/*");
                startActivityForResult(i, galleryPick);
            }
        });

        RetrieveUserInfo();

        dp.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibe.vibrate(50);

                if (imageUrl != null) {
                    CharSequence options[] = new CharSequence[]{
                            "View profile picture",
                            "Remove profile picture",
                            "Cancel"
                    };

                    final AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                    builder.setTitle("Remove Profile pic?");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {
                                Intent i = new Intent(SettingsActivity.this, ImageViewerActivity.class);
                                i.putExtra("url", imageUrl);
                                startActivity(i);
                            } else if (which == 1) {


                                AlertDialog.Builder builder
                                        = new AlertDialog
                                        .Builder(SettingsActivity.this);

                                builder.setMessage("Are you sure you want to logout?");
                                builder.setTitle("Alert !");
                                builder.setCancelable(false);

                                builder.setPositiveButton(
                                        "Yes",
                                        new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialog,
                                                                int which)
                                            {
                                                databaseReference.child("Users").child(currentUserID).child("image").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Picasso.get().load(R.drawable.profile_image).into(dp);
                                                            imageUrl = null;
                                                            Toast.makeText(SettingsActivity.this, "Profile pic removed", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(SettingsActivity.this, "Couldn't remove profile pic...", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
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
                else
                {
                    Toast.makeText(SettingsActivity.this, "Please add profile picture", Toast.LENGTH_SHORT).show();
                }

                return false;
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == galleryPick && resultCode == RESULT_OK && data != null) {
            Uri ImageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                progressDialog.setTitle("Profile Image upload");
                progressDialog.setMessage("Profile image uploading, Please wait...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
                Uri resultUri = result.getUri();
                final StorageReference filePath = UserProfileStorageRef.child(currentUserID + ".jpg");

                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                progressDialog.dismiss();
                                //Toast.makeText(SettingsActivity.this, "onSuccess: uri= " + uri.toString(), Toast.LENGTH_SHORT).show();
                                final String url = uri.toString();
                                databaseReference.child("Users").child(currentUserID).child("image")
                                        .setValue(url)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(SettingsActivity.this, "Image saved in database", Toast.LENGTH_SHORT).show();
                                                    progressDialog.dismiss();
                                                }
                                                else {
                                                    String m = task.getException().toString();
                                                    Toast.makeText(SettingsActivity.this, "Error: " + m, Toast.LENGTH_SHORT).show();
                                                    progressDialog.dismiss();
                                                }
                                            }
                                        });
                            }
                        });
                    }
                });
            }
        }

    }

    private void RetrieveUserInfo() {
        databaseReference.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("image")) && dataSnapshot.hasChild("name") && dataSnapshot.hasChild("status")) {
                    String retrieveUsername = dataSnapshot.child("name").getValue().toString();
                    String retrieveStatus = dataSnapshot.child("status").getValue().toString();
                    String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();

                    userName.setHint(retrieveUsername);
                    status.setHint(retrieveStatus);
                    Picasso.get().load(retrieveProfileImage).placeholder(R.drawable.profile_image).into(dp);
                    imageUrl = retrieveProfileImage;
                }
                else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")) && (dataSnapshot.hasChild("status"))) {
                    String retrieveUsername = dataSnapshot.child("name").getValue().toString();
                    String retrieveStatus = dataSnapshot.child("status").getValue().toString();
                    //String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();

                    userName.setHint(retrieveUsername);
                    status.setHint(retrieveStatus);
                    //Picasso.get().load(retrieveProfileImage).into(dp);
                }
                else {
                    Toast.makeText(SettingsActivity.this, "Please set and update your profile information", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateSettings() {
        String Username = userName.getText().toString();
        String Status = status.getText().toString();
        if (TextUtils.isEmpty(Username)) {
            Toast.makeText(this, "Please Enter the user name", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(Status)) {
            Toast.makeText(this, "Please Enter the status", Toast.LENGTH_SHORT).show();
        }
        else
        {
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserID);
            profileMap.put("name", Username);
            profileMap.put("status", Status);

            databaseReference.child("Users").child(currentUserID).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        sendToMainPage();
                        Toast.makeText(SettingsActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        String message = task.getException().toString();
                        Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void InitializeFields() {
        update = (Button) findViewById(R.id.newregupdate);
        userName = (EditText) findViewById(R.id.newregname);
        status = (EditText) findViewById(R.id.newregstatus);
        dp = (CircleImageView) findViewById(R.id.newregdp);
        toolbar = (Toolbar) findViewById(R.id.settings_app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Profile");
    }


    private void sendToMainPage() {
        Intent i = new Intent(SettingsActivity.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
