package mif50.com.orderfoodsserver;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import common.Common;
import model.User;

public class SignIn extends AppCompatActivity implements View.OnClickListener{

    TextView txt_phone,txt_password;
    Button btn_sign_in_home;
    FirebaseDatabase database;
    DatabaseReference users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        // initial firebase
        database=FirebaseDatabase.getInstance();
        users=database.getReference("User");

        // initial and find view
        txt_phone=findViewById(R.id.txt_phone);
        txt_password=findViewById(R.id.txt_password);
        btn_sign_in_home=findViewById(R.id.btn_sign_in_home);
        btn_sign_in_home.setOnClickListener(this);

    }
    public static Intent newIntent(Context context){
        Intent intent=new Intent(context,SignIn.class);
        return intent;
    }

    @Override
    public void onClick(View v) {
        int id=v.getId();
        if (id==R.id.btn_sign_in_home){
            signIn(txt_phone.getText().toString(),txt_password.getText().toString());
        }
    }

    private void signIn(String phone, String password) {
        final String localPhone=phone;
        final String localPassword=password;
        final ProgressDialog dialog=new ProgressDialog(this);
        dialog.setMessage("please wait");
        dialog.show();
        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(localPhone).exists()){
                    dialog.dismiss();
                    User user=dataSnapshot.child(localPhone).getValue(User.class);
                    if (Boolean.parseBoolean(user.getIsStuff())){
                        if (user.getPassword().equals(localPassword)){
                            // login success
                            dialog.dismiss();
                            Common.currentUser=user;
                            startActivity(Home.newIntent(SignIn.this));
                            finish();
                        }else {
                            Toast.makeText(SignIn.this, "password is wrong", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(SignIn.this, "you are not stuff this only to stuff", Toast.LENGTH_SHORT).show();
                    }

                }else{
                    dialog.dismiss();
                    Toast.makeText(SignIn.this, "phone not exits pls sign up", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
