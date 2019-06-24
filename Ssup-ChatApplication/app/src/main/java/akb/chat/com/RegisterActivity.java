package akb.chat.com;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {

    private Button register;
    private TextView goToLogin;
    private EditText reg_email, reg_pass;
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        InitializeFields();

        goToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToLoginPage();
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });

    }

    private void CreateNewAccount() {
        String email = reg_email.getText().toString();
        String pass = reg_pass.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter Email...", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Please enter Password..", Toast.LENGTH_SHORT).show();
        }
        else {
            progressDialog.setTitle("Creating new account");
            progressDialog.setMessage("Please wait...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){

                        String deviceToken = FirebaseInstanceId.getInstance().getToken();

                        String currentUser = mAuth.getCurrentUser().getUid();
                        databaseReference.child("Users").child(currentUser).setValue("");

                        databaseReference.child("Users").child(currentUser).child("device_token").setValue(deviceToken);

                        sendToMainPage();
                        Toast.makeText(RegisterActivity.this, "Account Created Successfully!!", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                    else {
                        String message = task.getException().toString();
                        Toast.makeText(RegisterActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            });
        }
    }

    private void sendToLoginPage() {
        Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(i);
    }

    private void sendToMainPage() {
        Intent i = new Intent(RegisterActivity.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private void InitializeFields() {
        register = (Button) findViewById(R.id.register_button);
        goToLogin = (TextView) findViewById(R.id.old_account);
        reg_email = (EditText) findViewById(R.id.register_email);
        reg_pass = (EditText) findViewById(R.id.register_password);
        progressDialog = new ProgressDialog(this);

    }

}
