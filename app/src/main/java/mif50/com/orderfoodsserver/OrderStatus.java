package mif50.com.orderfoodsserver;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jaredrummler.materialspinner.MaterialSpinner;

import adapter.OrderViewHolder;
import common.Common;
import inter.ItemClickListener;
import model.Request;

public class OrderStatus extends AppCompatActivity {
   // view in activity
   RecyclerView recycler_list_order;
   RecyclerView.LayoutManager layoutManager;
   //Firebase
    FirebaseDatabase database;
    DatabaseReference requests;
    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        // find view in activity
        recycler_list_order=findViewById(R.id.recycler_list_order);
        recycler_list_order.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recycler_list_order.setLayoutManager(layoutManager);
        // initial Firebase
        database=FirebaseDatabase.getInstance();
        requests=database.getReference("Requests");
        // load Order from firebase
        loadOrders();
    }
    /* this method that get data From Firebase json In Adapter => FirebaseRecyclerAdapter*/
    private void loadOrders() {
        adapter=new FirebaseRecyclerAdapter<Request, OrderViewHolder>(
                Request.class,
                R.layout.order_layout,
                OrderViewHolder.class,
                requests) {
            @Override
            protected void populateViewHolder(OrderViewHolder viewHolder, final Request model, int position) {
                // get data From Model and set In View Holder and By adapter set View Holder in RecyclerView
                viewHolder.txt_id.setText(adapter.getRef(position).getKey());
                viewHolder.txt_status.setText(Common.convertCodeToStatus(model.getStatus()));
                viewHolder.txt_phone.setText(model.getPhone());
                viewHolder.txt_address.setText(model.getAddress());
                // action View holder
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View v, int position, boolean isLongClick) {
                        // move to google map
                        Common.currentRequest=model; // set currentRequest = model to get data im map from currentRequest
                        startActivity(OrderTracking.newIntent(OrderStatus.this));
                    }
                });
            }
        };
        adapter.notifyDataSetChanged(); // get data when refresh
        recycler_list_order.setAdapter(adapter); // set up adapter
    }

    /*show action when select option of context menu
    * @param item of MenuItem
    * */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals(Common.UPDATE)){
            // show update action
            showUpdateDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
        }
        else if (item.getTitle().equals(Common.DELETE)){
            // show delete Action
            deleteOrder(adapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);
    }
    /*this delete to delete Order
     * @param key => used to delete item */
    private void deleteOrder(String key) {
        requests.child(key).removeValue(); // delete item from firebase json Requests
    }

    /*this method create dialog to update status  an save it in Firebase
    * @param  key=> to determine item that will update
    *          item => to change item and save it
    *          */
    private void showUpdateDialog(String key, final Request item) {
        final String localKey=key;
        // create Dialog
        AlertDialog.Builder dialog=new AlertDialog.Builder(this);
        dialog.setTitle("Update Status");
        dialog.setMessage("Please fill full Information");
        // get Inflater
        LayoutInflater inflater=this.getLayoutInflater();
        // create View
        View view=inflater.inflate(R.layout.update_context_menu_layout,null);
        // find View in this layout
        final MaterialSpinner spinner=view.findViewById(R.id.spinner_update);
        spinner.setItems("Placed","On My Way","Shipped"); // set item of spinner
        // set View in dialog
        dialog.setView(view);
        dialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // change status in item
                item.setStatus(String.valueOf(spinner.getSelectedIndex()));
                requests.child(localKey).setValue(item); // update item in Requests Json

            }
        });
        dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        //show dialog
        dialog.show();
    }

    /* this method to move to this Activity*/
    public static Intent newIntent(Context context){
        Intent intent=new Intent(context,OrderStatus.class);
        return intent;
    }
}
