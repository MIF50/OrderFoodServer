package mif50.com.orderfoodsserver;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.UUID;

import adapter.MenuViewHolder;
import common.Common;
import inter.ItemClickListener;
import model.Category;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    TextView txt_name_menu;
    RecyclerView recycler_item_server;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference category;
    FirebaseRecyclerAdapter<Category, MenuViewHolder> adapter;
    int count=0;
    TextView txt_name_menu_add;
    Button btn_select,btn_upload;

    FirebaseStorage storage;
    StorageReference storageReference;
    Uri saveUri;

    Category categoryModel;
    DrawerLayout drawer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Menu Management");
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                showDialog();
            }
        });

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // set name in header of navigation bar
        View header=navigationView.getHeaderView(0);
        txt_name_menu=header.findViewById(R.id.txt_name_menu);
        txt_name_menu.setText(Common.currentUser.getName());

        // initial recycler view
        recycler_item_server=findViewById(R.id.recycler_item_server);
        recycler_item_server.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recycler_item_server.setLayoutManager(layoutManager);

        // initial firebase
        database=FirebaseDatabase.getInstance();
        category=database.getReference("Category");
        loadMenu();
        // intial firebase storage
        storage=FirebaseStorage.getInstance();
        storageReference=storage.getReference();
    }

    private void showDialog() {
        AlertDialog.Builder dialog=new AlertDialog.Builder(this);
        dialog.setTitle("add new Menu");
        dialog.setMessage("please fill full information");
        LayoutInflater inflater=LayoutInflater.from(this);
        View item=inflater.inflate(R.layout.add_menu_item_layout,null);
        txt_name_menu_add=item.findViewById(R.id.txt_name_menu_add);
        btn_select=item.findViewById(R.id.btn_select);
        btn_upload=item.findViewById(R.id.btn_upload);
        // event Button
        btn_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage(); // let user select image from gallery and save uri of this image
            }
        });
        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage(); //save image in firebase storage 
            }
        });
        dialog.setView(item);
        dialog.setIcon(R.drawable.ic_add);
        dialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (categoryModel!=null){
                    category.push().setValue(categoryModel);
                    Snackbar.make(drawer, "new category"+categoryModel.getName()+" added",
                            Snackbar.LENGTH_LONG).show();

                }
            }
        });
        dialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void uploadImage() {
        if (saveUri!=null){
            final ProgressDialog dialog=new ProgressDialog(this);
            dialog.setMessage("uploading");
            dialog.show();
            String imageName= UUID.randomUUID().toString();
            final StorageReference imageFolder=storageReference.child("images/"+imageName);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    dialog.dismiss();
                    Toast.makeText(Home.this, "uploaded", Toast.LENGTH_SHORT).show();
                    imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            // set value for new category if image upload and we can get download link
                            categoryModel=new Category(txt_name_menu_add.getText().toString(),uri.toString());

                        }
                    });
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress=(100.0*taskSnapshot.getBytesTransferred())/(taskSnapshot.getTotalByteCount());
                            dialog.setMessage("uploaded "+progress+" %");
                        }
                    });
        }
    }

    private void chooseImage() {
        //
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"selected picture"),Common.PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==Common.PICK_IMAGE_REQUEST && resultCode==RESULT_OK
                && data!=null && data.getData()!=null){
            saveUri=data.getData();
            btn_select.setText("image selected");
        }
    }

    private void loadMenu() {
        adapter=new FirebaseRecyclerAdapter<Category, MenuViewHolder>(Category.class,
                R.layout.menu_item,
                MenuViewHolder.class,
                category) {
            @Override
            protected void populateViewHolder(MenuViewHolder viewHolder, Category model, int position) {
                viewHolder.txt_name_item.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.image_item);
                count =adapter.getItemCount();
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View v, int position, boolean isLongClick) {
                       // move to FoodList and sent menuId to load food list depend on this menu
                        startActivity(FoodList.newIntent(Home.this,adapter.getRef(position).getKey()));
                    }
                });
            }
        };

        adapter.notifyDataSetChanged();
        //count=adapter.getItemCount

        recycler_item_server.setAdapter(adapter);


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_menu) {
            //
        } else if (id == R.id.nav_order) {
            // move to OrderStatus Activity
            startActivity(OrderStatus.newIntent(Home.this));
            finish();

        } else if (id == R.id.nav_card) {

        } else if (id == R.id.nav_sign_out) {

        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    /**/
    public static Intent newIntent(Context context){
        Intent intent=new Intent(context,Home.class);
        return intent;
    }

    /* this method show Action Event of Dialog (select Action,update,delete) that appear when click on food
     what happen when click on update or delete*/
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals(Common.UPDATE)){
            // this method show action when select update take two @param (key,item)
            showUpdateDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
        }
        else if (item.getTitle().equals(Common.DELETE)){
            // this method show action when select delete @param (key)
            deleteItemOfCategory(adapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);
    }

    private void deleteItemOfCategory(String key) {
        category.child(key).removeValue();
        Toast.makeText(this, "item of category is delete", Toast.LENGTH_SHORT).show();
    }
    /*this method show how to update data of menu in firebase
    * @param (key,item) key => to determine item that will update , item => to change data of this item thta will be updated
    * */
    private void showUpdateDialog(final String key,Category cat) {
        final Category model=cat;
        AlertDialog.Builder dialog=new AlertDialog.Builder(this);
        dialog.setTitle("change item menu");
        dialog.setMessage("please fill full information");
        LayoutInflater inflater=LayoutInflater.from(this);
        View item=inflater.inflate(R.layout.add_menu_item_layout,null);
        txt_name_menu_add=item.findViewById(R.id.txt_name_menu_add);
        btn_select=item.findViewById(R.id.btn_select);
        btn_upload=item.findViewById(R.id.btn_upload);
        txt_name_menu_add.setText(model.getName());
        
        // event Button
        btn_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               chooseImage(); // choose image from phone
            }
        });
        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeImage(model); //save image in firebase storage
            }
        });
        dialog.setView(item);
        dialog.setIcon(R.drawable.ic_add);
        dialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // get name and set model to obj json firebase
                model.setName(txt_name_menu_add.getText().toString());
                category.child(key).setValue(model);
            }
        });
        dialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();

    }
    /*this method used upload image in firebase Storage
    * to used it to update Image if the item that will be update*/
    private void changeImage(final Category model) {

        if (saveUri!=null){
            final ProgressDialog dialog=new ProgressDialog(this);
            dialog.setMessage("uploading");
            dialog.show();
            String imageName= UUID.randomUUID().toString();
            final StorageReference imageFolder=storageReference.child("images/"+imageName);
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            dialog.dismiss();
                            Toast.makeText(Home.this, "uploaded", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // set value for model of  image that upload and we can get download link
                                    model.setImage(uri.toString());
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress=(100.0*taskSnapshot.getBytesTransferred())/(taskSnapshot.getTotalByteCount());
                            dialog.setMessage("uploaded "+progress+" %");
                        }
                    });
        }
    }
}
