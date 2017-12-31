package mif50.com.orderfoodsserver;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.UUID;

import adapter.FoodViewHolder;
import common.Common;
import inter.ItemClickListener;
import model.Food;

public class FoodList extends AppCompatActivity implements View.OnClickListener {

    RecyclerView recycler_food_list;
    RecyclerView.LayoutManager layoutManager;
    FloatingActionButton fab_add_food_list;

    private final static String MENU_ID="menu_id";

    FirebaseDatabase database;
    DatabaseReference foods;
    FirebaseRecyclerAdapter<Food, FoodViewHolder>adapter;
    private String menuId;
    // view of add_food_list_layout
    MaterialEditText txt_name_food_list_item,txt_description_food_list_item,txt_price_food_list_item,txt_discount_food_list_item;
    Button btn_select_food_item,btn_upload_food_item;
    // firebase storage
    FirebaseStorage storage;
    StorageReference storageReference;
    Uri saveUri;

    //Food to hold data that added
    Food food;
    // obj of ConstraintLayout
    ConstraintLayout root;

    MaterialEditText txt_name_update,txt_description_update,txt_price_update,txt_discount_update;
    //Button btn_select_update,btn_upload_update;
    Food food_updated=new Food(); // used to update data in firebase as Model Food

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        root=findViewById(R.id.food_list);

        recycler_food_list=findViewById(R.id.recycler_food_list);
        recycler_food_list.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recycler_food_list.setLayoutManager(layoutManager);

        fab_add_food_list=findViewById(R.id.fab_add_food_list);
        fab_add_food_list.setOnClickListener(this);
        // initial firebase
        database=FirebaseDatabase.getInstance();
        foods=database.getReference("Foods");
        // get menuId from Intent
        if (getIntent()!=null){
            menuId=getIntent().getStringExtra(MENU_ID);
            if (!menuId.isEmpty()){
                loadFoodListMenu(menuId);
            }
        }
        // inital storage firebase
        storage=FirebaseStorage.getInstance();
        storageReference=storage.getReference();
    }

    private void loadFoodListMenu(String menuId) {
        adapter=new FirebaseRecyclerAdapter<Food, FoodViewHolder>(Food.class,
                R.layout.food_list_layout,
                FoodViewHolder.class,
                foods.orderByChild("menuId").equalTo(menuId)) {
            @Override
            protected void populateViewHolder(FoodViewHolder viewHolder, Food model, int position) {
                viewHolder.name_food_list.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(viewHolder.image_food_list);
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View v, int position, boolean isLongClick) {
                        // code later
                    }
                });

            }
        };
        adapter.notifyDataSetChanged();
        recycler_food_list.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        int id=v.getId();
        if (id==R.id.fab_add_food_list){
            addNewFoodInList();
        }
    }

    private void addNewFoodInList() {
        AlertDialog.Builder dialog=new AlertDialog.Builder(this);
        dialog.setTitle("add new Food");
        dialog.setMessage("please fill full information");
        LayoutInflater inflater=LayoutInflater.from(this);
        View item=inflater.inflate(R.layout.add_food_list_layout,null);
        txt_name_food_list_item=item.findViewById(R.id.txt_name_food_list_item);
        txt_description_food_list_item=item.findViewById(R.id.txt_description_food_list_item);
        txt_price_food_list_item=item.findViewById(R.id.txt_price_food_list_item);
        txt_discount_food_list_item=item.findViewById(R.id.txt_discount_food_list_item);
        btn_select_food_item=item.findViewById(R.id.btn_select_food_item);
        btn_upload_food_item=item.findViewById(R.id.btn_upload_food_item);
        // scroll bar in dialog

        // event Button
        btn_select_food_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage(); // let user select image from gallery and save uri of this image
            }
        });
        btn_upload_food_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage(); //save image in firebase storage
            }
        });
        dialog.setIcon(R.drawable.ic_add);
        dialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (food!=null){
                    foods.push().setValue(food);
                    Snackbar.make(root, "new Food "+food.getName()+" added in List",
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
        dialog.setView(item);
        AlertDialog builder=dialog.create();
        
        //dialog.show();
        builder.show();
    }
    /*
    * this method to get image from phone
    * */
    private void chooseImage() {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"selected picture"), Common.PICK_IMAGE_REQUEST);
    }
    /*
    * this method to get result of image that user selected
    * save image (data) in saveUri
    * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==Common.PICK_IMAGE_REQUEST && resultCode==RESULT_OK
                && data!=null && data.getData()!=null){
            saveUri=data.getData();
            //btn_select_update.setText("image selected");
            btn_select_food_item.setText("image_selected");
        }
    }
    /*
    * this method to upload image in firebase storage
    * set value to new Food (name,description,image,price,discount)
    * to use it to add in firebase obj json Foods
    * */
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
                            Toast.makeText(FoodList.this, "uploaded", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // set value of Food that you want to add it
                                    // get value from add_food_list_item_layout
                                    food=new Food();
                                    food.setName(txt_name_food_list_item.getText().toString());
                                    food.setDescription(txt_description_food_list_item.getText().toString());
                                    food.setImage(uri.toString());
                                    food.setPrice(txt_price_food_list_item.getText().toString());
                                    food.setMenuId(menuId);
                                    food.setDiscount(txt_discount_food_list_item.getText().toString());
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            Toast.makeText(FoodList.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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

    /*this method used to move to ListFood and send data to this activity*/
    public static Intent newIntent(Context context,String menuId){
        Intent intent=new Intent(context,FoodList.class);
        intent.putExtra(MENU_ID,menuId);
        return intent;
    }
    /* this method show Action Event of Dialog (select Action,update,delete) what happen when click on update or delete*/
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        String title=item.getTitle().toString();
        if (title.equals(Common.UPDATE)){
            // method to show action when select update this method take to param(key,item)
            showUpdateFoodList(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
        }else if (title.equals(Common.DELETE)){
            // method to show action when select delete this method take param(key)
            deleteFood(adapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);
    }

    /* this method used to delete item depend on key from Firebase*/
    private void deleteFood(String key) {
        foods.child(key).removeValue();
    }
    /*this method used to update food depend on key and item
    * @param (key,item) key => to determine item that will be update , item => to change data that will be update
    * */
    private void showUpdateFoodList(final String key, Food item) {
        final Food model=item;
        AlertDialog.Builder dialog=new AlertDialog.Builder(this);
        dialog.setTitle("update Food");
        dialog.setMessage("please fill full information");
        LayoutInflater inflater=LayoutInflater.from(this);
        View item_view=inflater.inflate(R.layout.add_food_list_layout,null);
        // find Views
        txt_name_update=item_view.findViewById(R.id.txt_name_food_list_item);
        txt_description_update=item_view.findViewById(R.id.txt_description_food_list_item);
        txt_price_update=item_view.findViewById(R.id.txt_price_food_list_item);
        txt_discount_update=item_view.findViewById(R.id.txt_discount_food_list_item);
        btn_select_food_item=item_view.findViewById(R.id.btn_select_food_item);
        btn_upload_food_item=item_view.findViewById(R.id.btn_upload_food_item);
        // get data from model and set in Views
        txt_name_update.setText(model.getName());
        txt_description_update.setText(model.getDescription());
        txt_price_update.setText(model.getPrice());
        txt_discount_update.setText(model.getDiscount());


        // event Button
        btn_select_food_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage(); // choose image from phone
            }
        });
        btn_upload_food_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeImage(model); //save image in firebase storage
            }
        });
        dialog.setView(item_view);
        dialog.setIcon(R.drawable.ic_add);
        dialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                // get name , description , price ,discount => (that will be update ) set model to obj json firebase
                model.setName(txt_name_update.getText().toString());
                model.setDescription(txt_description_update.getText().toString());
                model.setPrice(txt_price_update.getText().toString());
                model.setDiscount(txt_discount_update.getText().toString());
                foods.child(key).setValue(model);
                Snackbar.make(root,"food update",Snackbar.LENGTH_SHORT).show();
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
    /* this method used to upload image in firebase storage
    * used to update image of item that will be update*/
    private void changeImage(final Food model) {
        if (saveUri!=null){
            final ProgressDialog dialog=new ProgressDialog(this);
            dialog.setMessage("uploading");
            dialog.show();
            String imageName= UUID.randomUUID().toString(); // create random name to image
            final StorageReference imageFolder=storageReference.child("images/"+imageName); // path of Image in firebase
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            dialog.dismiss();
                            Toast.makeText(FoodList.this, "uploaded", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // set value for model of  image that upload and we can get download link
                                    model.setImage(uri.toString());
                                    Snackbar.make(root,"image change",Snackbar.LENGTH_SHORT).show();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            Toast.makeText(FoodList.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
