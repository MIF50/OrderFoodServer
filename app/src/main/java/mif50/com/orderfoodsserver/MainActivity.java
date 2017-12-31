package mif50.com.orderfoodsserver;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    TextView txt_login;
    Button btn_sign_in;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt_login=findViewById(R.id.txt_login);
        btn_sign_in=findViewById(R.id.btn_sign_in);
        Typeface face=Typeface.createFromAsset(getAssets(),"fonts/NABILA.TTF");
        txt_login.setTypeface(face);
        btn_sign_in.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        int id =v.getId();
        if (id==R.id.btn_sign_in){
            startActivity(SignIn.newIntent(MainActivity.this));
            finish();
        }
    }
}
