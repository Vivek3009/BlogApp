package rob.myappcompany.myblogapp;



import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.content.pm.ActivityInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationMenu;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.zip.Inflater;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    static  public Toolbar toolbar;
    private FloatingActionButton floatingActionButton;
    FirebaseUser currentUser;
    FirebaseFirestore firebaseFirestore;

    private HomeFragment homeFragment;
    private NotificationFragment notificationFragment;
    private AccountFragment accountFragment;

    static public BottomNavigationView mainbottomView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setTitle("Photo Blog");
        floatingActionButton=findViewById(R.id.add_post_button);

        if(mAuth.getCurrentUser()!=null) {
            homeFragment = new HomeFragment();
            notificationFragment = new NotificationFragment();
            accountFragment = new AccountFragment();

            mainbottomView = findViewById(R.id.bottom_nev_bar);

            repleaceFragment(homeFragment);
            mainbottomView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    switch (item.getItemId()) {
                        case R.id.bottom_home_bar:
                            repleaceFragment(homeFragment);
                            return true;
                        case R.id.bottom_notification_bar:
                            repleaceFragment(notificationFragment);
                            return true;
                        case R.id.bottom_account_bar:
                            repleaceFragment(accountFragment);
                            return true;
                        default:
                            return false;
                    }
                }
            });

            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, addPostActivity.class));

                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
       switch (item.getItemId())
       {
           case R.id.logOut:
                              logOut();
                              return true;

           case R.id.setting: startActivity(new Intent(MainActivity.this,setupActivity.class));
                               return true;

           default:
                      return false;
       }
    }

    private void logOut() {
        mAuth.signOut();
        moveToLogin();
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();

        if(currentUser==null)
        {
            moveToLogin();
        }
        else
        {

            //repleaceFragment(homeFragment);
            String user_id=mAuth.getCurrentUser().getUid();
            firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful())
                    {
                        if(!task.getResult().exists()) {
                            startActivity(new Intent(MainActivity.this,setupActivity.class));
                            finish();
                        }
                    }
                    else
                    {
                        String error=task.getException().getMessage();
                        Toast.makeText(MainActivity.this, "Error : "+error, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }


    }

    private void moveToLogin() {
        Intent intent=new Intent(MainActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void repleaceFragment(Fragment fragment)
    {
        FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container,fragment);
        fragmentTransaction.commit();
    }
}
