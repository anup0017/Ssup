package akb.chat.com;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

public class RegisterProfileActivity extends AppCompatActivity {

    private Button newupdate;
    private CircleImageView newdp;
    private EditText newname, newstatus;
    private ProgressDialog progressDialog;

    private StorageReference UserProfileStorageRef;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private Toolbar toolbar;

    private static final int galleryPick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_profile);

        progressDialog = new ProgressDialog(this);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        UserProfileStorageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        newupdate = (Button) findViewById(R.id.newregupdate);
        newdp = (CircleImageView) findViewById(R.id.newregdp);
        newname = (EditText) findViewById(R.id.newregname);
        newstatus = (EditText) findViewById(R.id.newregstatus);
        toolbar = (Toolbar) findViewById(R.id.new_account_app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Set Profile");

        newdp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setAction(Intent.ACTION_GET_CONTENT);
                i.setType("image/*");
                startActivityForResult(i, galleryPick);
            }
        });

        newupdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                create();
            }
        });
        RetrieveUserInfo();
    }

    private void create() {
        String Username = newname.getText().toString();
        String Status = newstatus.getText().toString();
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
                        Toast.makeText(RegisterProfileActivity.this, "Profile Created Successfully", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        String message = task.getException().toString();
                        Toast.makeText(RegisterProfileActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void sendToMainPage() {
        Intent i = new Intent(RegisterProfileActivity.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
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
                                                    Toast.makeText(RegisterProfileActivity.this, "Image saved in database", Toast.LENGTH_SHORT).show();
                                                    progressDialog.dismiss();
                                                }
                                                else {
                                                    String m = task.getException().toString();
                                                    Toast.makeText(RegisterProfileActivity.this, "Error: " + m, Toast.LENGTH_SHORT).show();
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
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("image"))) {
                    String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(retrieveProfileImage).placeholder(R.drawable.profile_image).into(newdp);
                }
                else {
                    Toast.makeText(RegisterProfileActivity.this, "Please set and update your profile information", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
