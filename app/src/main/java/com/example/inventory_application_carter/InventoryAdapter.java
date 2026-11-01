package com.example.inventory_application_carter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

//Recycler view adapter for all inventory items
public class InventoryAdapter
        extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {

    private ArrayList<InventoryItem> inventoryList;
    private OnDeleteClickListener deleteListener;
    private OnUpdateClickListener updateListener;

    //Interface to listen for delete button clicks
    public interface OnDeleteClickListener {
        void onDeleteClick(int itemId);
    }
    //Interface to listen for update button clicks
    public interface OnUpdateClickListener {
        void onUpdateClick(int itemId);
    }

    //Constructor
    public InventoryAdapter(
            ArrayList<InventoryItem> inventoryList,
            OnDeleteClickListener deleteListener,
            OnUpdateClickListener updateListener) {

        this.inventoryList = inventoryList;
        this.deleteListener = deleteListener;
        this.updateListener = updateListener;
    }

   //Make a view holder for each row
    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_items, parent, false);
        return new InventoryViewHolder(view);
    }

   //Holds inventory data to a row
    @Override
    public void onBindViewHolder(
            @NonNull InventoryViewHolder holder,
            int position) {

        InventoryItem item =
                inventoryList.get(position);

        holder.textItemName.setText(item.getItemName());

        holder.textQuantity.setText(String.valueOf(item.getQuantity()));

        holder.buttonUpdate.setOnClickListener(view -> updateListener.onUpdateClick(item.getItemId()));

        holder.buttonDelete.setOnClickListener(view -> deleteListener.onDeleteClick(item.getItemId()));
    }

    //return the number of items in the array
    @Override
    public int getItemCount() {
        return inventoryList.size();
    }

    //View holder for rows of inventory
    public static class InventoryViewHolder
            extends RecyclerView.ViewHolder {

        TextView textItemName;
        TextView textQuantity;
        Button buttonDelete;
        Button buttonUpdate;
        public InventoryViewHolder(
                @NonNull View itemView) {

            super(itemView);

            textItemName = itemView.findViewById(R.id.textItemName);

            textQuantity = itemView.findViewById(R.id.textQuantity);

            buttonUpdate = itemView.findViewById(R.id.buttonUpdate);

            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}