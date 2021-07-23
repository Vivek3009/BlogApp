package rob.myappcompany.myblogapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LogInRegActivity extends AppCompatActivity {

    private EditText emailText;
    private EditText passText;
    private EditText confirmPassText;
    private Button regButton;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private Button moveToLogInButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in_reg);

        mAuth = FirebaseAuth.getInstance();
        progressBar=findViewById(R.id.regProgressBar);

        emailText=findViewById(R.id.regemailTextView);
        passText=findViewById(R.id.regpassTextView);
        regButton=findViewById(R.id.createAccountButton);
        confirmPassText=findViewById(R.id.regConfirmpassTextView);
        moveToLogInButton=findViewById(R.id.moveToLogInButton);

        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 String email=emailText.getText().toString();
                String pass=passText.getText().toString();
                String confirm=confirmPassText.getText().toString();

                if(!TextUtils.isEmpty(email)&&!TextUtils.isEmpty(pass)&&!TextUtils.isEmpty(confirm)) {
                    if (confirm.equals(pass)) {
                        progressBar.setVisibility(View.VISIBLE);
                        mAuth.createUserWithEmailAndPassword(email, pass)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            startActivity(new Intent(LogInRegActivity.this,setupActivity.class));
                                            finish();
                                        } else {
                                            String error = task.getException().getMessage();
                                            Toast.makeText(LogInRegActivity.this, "Error : " + error, Toast.LENGTH_SHORT).show();
                                        }
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                });
                    }
                    else
                    {
                        Toast.makeText(LogInRegActivity.this, "Enter Same Password In Both", Toast.LENGTH_SHORT).show();
                    }
                    }
            }
        });

        moveToLogInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LogInRegActivity.this,LoginActivity.class));
                finish();
            }
        });
    }
}
