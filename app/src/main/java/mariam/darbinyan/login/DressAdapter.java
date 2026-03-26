package mariam.darbinyan.login;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.List;

public class DressAdapter extends RecyclerView.Adapter<DressAdapter.ViewHolder> {

    private List<String> imageUrls;
    private String categoryPath;
    private String dbUrl = "https://mariam-sproject-default-rtdb.europe-west1.firebasedatabase.app/";

    public DressAdapter(List<String> imageUrls, String categoryPath) {
        this.imageUrls = imageUrls;
        this.categoryPath = categoryPath;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dress, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imageString = imageUrls.get(position);

        // Display Logic
        try {
            byte[] decodedString = android.util.Base64.decode(imageString, android.util.Base64.DEFAULT);
            android.graphics.Bitmap decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            holder.imageView.setImageBitmap(decodedByte);
        } catch (Exception e) {
            com.bumptech.glide.Glide.with(holder.itemView.getContext())
                    .load(imageString)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(holder.imageView);
        }

        // 1. DELETE LOGIC: Now attached to the ImageView to avoid conflict
        holder.imageView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Delete Item")
                    .setMessage("Do you want to remove this from your wardrobe?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        // Pass 'position' to help update the UI after deletion
                        deleteFromFirebase(imageString, v, position);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true; // "true" prevents the regular Click from triggering
        });

        // 2. AI CHAT LOGIC: Regular click
        holder.imageView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(v.getContext(), ChatActivity.class);
            intent.putExtra("image_data", imageString);
            v.getContext().startActivity(intent);
        });
    }

    private void deleteFromFirebase(String imageContent, View view, int position) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance(dbUrl)
                .getReference("Users").child(userId).child(categoryPath);

        // Find the specific item and remove it
        ref.orderByValue().equalTo(imageContent).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        data.getRef().removeValue();
                    }

                    // Update the local list so the item disappears immediately
                    if (position < imageUrls.size()) {
                        imageUrls.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, imageUrls.size());
                    }

                    Toast.makeText(view.getContext(), "Item removed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(view.getContext(), "Delete failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageDressItem);
        }
    }
}