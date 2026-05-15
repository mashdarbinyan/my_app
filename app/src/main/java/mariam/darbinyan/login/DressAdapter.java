package mariam.darbinyan.login;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
    private boolean isFavoriteScreen;
    private boolean isSelectMode;
    private String dbUrl = "https://mariam-sproject-default-rtdb.europe-west1.firebasedatabase.app/";

    public DressAdapter(List<String> imageUrls, String categoryPath, boolean isSelectMode) {
        this.imageUrls = imageUrls;
        this.categoryPath = categoryPath;
        this.isSelectMode = isSelectMode;
        // Automatically detect if we are on the favorites screen
        this.isFavoriteScreen = "favorites".equals(categoryPath);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.clothing_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imageString = imageUrls.get(position);

        if (imageString == null || imageString.isEmpty()) {
            holder.imageView.setImageResource(android.R.drawable.ic_menu_gallery);
            return;
        }

        android.content.Context context = holder.itemView.getContext();

        // 1. UI Handling for Selection Mode
        if (isSelectMode) {
            holder.btn_favorite.setVisibility(View.GONE);
        } else {
            holder.btn_favorite.setVisibility(View.VISIBLE);
        }

        // 2. Image Loading
        try {
            byte[] decodedString = android.util.Base64.decode(imageString, android.util.Base64.DEFAULT);
            com.bumptech.glide.Glide.with(context)
                    .asBitmap()
                    .load(decodedString)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(holder.imageView);
        } catch (Exception e) {
            com.bumptech.glide.Glide.with(context).load(imageString).into(holder.imageView);
        }

        // 3. Complete Favorites System
        if (!isSelectMode) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference favRef = FirebaseDatabase.getInstance(dbUrl).getReference("Users")
                    .child(userId).child("favorites");

            // Create a unique key based on the image content
            String favoriteKey = String.valueOf(imageString.hashCode());

            // Check if item is already favorited to set correct star icon
            favRef.child(favoriteKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        holder.btn_favorite.setImageResource(android.R.drawable.btn_star_big_on);
                    } else {
                        holder.btn_favorite.setImageResource(android.R.drawable.btn_star_big_off);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });

            // Toggle Favorite on Click
            holder.btn_favorite.setOnClickListener(v -> {
                favRef.child(favoriteKey).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Remove from Favorites
                            favRef.child(favoriteKey).removeValue().addOnSuccessListener(aVoid -> {
                                holder.btn_favorite.setImageResource(android.R.drawable.btn_star_big_off);
                                Toast.makeText(context, "Removed from Favorites", Toast.LENGTH_SHORT).show();

                                if (isFavoriteScreen) {
                                    imageUrls.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, imageUrls.size());
                                }
                            });
                        } else {
                            // Add to Favorites
                            favRef.child(favoriteKey).setValue(imageString).addOnSuccessListener(aVoid -> {
                                holder.btn_favorite.setImageResource(android.R.drawable.btn_star_big_on);
                                Toast.makeText(context, "Added to Favorites! ❤️", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            });
        }

        // 4. Delete and Navigation Click Listeners
        holder.imageView.setOnLongClickListener(v -> {
            if (!isSelectMode) {
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Delete Item")
                        .setMessage("Do you want to remove this from your wardrobe?")
                        .setPositiveButton("Delete", (dialog, which) -> deleteFromFirebase(imageString, v, position))
                        .setNegativeButton("Cancel", null)
                        .show();
            }
            return true;
        });

        holder.imageView.setOnClickListener(v -> {
            if (isSelectMode) {
                Intent intent = new Intent();
                intent.putExtra("PICKED_IMAGE", imageString);
                if (context instanceof Activity) {
                    ((Activity) context).setResult(Activity.RESULT_OK, intent);
                    ((Activity) context).finish();
                }
            } else {
                Intent intent = new Intent(v.getContext(), ChatActivity.class);
                intent.putExtra("image_data", imageString);
                v.getContext().startActivity(intent);
            }
        });
    }

    private void deleteFromFirebase(String imageContent, View view, int position) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference categoryRef = FirebaseDatabase.getInstance(dbUrl)
                .getReference("Users").child(userId).child(categoryPath);
        DatabaseReference favRef = FirebaseDatabase.getInstance(dbUrl)
                .getReference("Users").child(userId).child("favorites");

        categoryRef.orderByValue().equalTo(imageContent).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        data.getRef().removeValue();
                    }

                    if (position < imageUrls.size()) {
                        imageUrls.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, imageUrls.size());
                    }

                    // Always try to clean up the favorite entry if it was deleted from wardrobe
                    String favoriteKey = String.valueOf(imageContent.hashCode());
                    favRef.child(favoriteKey).removeValue();

                    Toast.makeText(view.getContext(), "Deleted successfully", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton btn_favorite;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageDressItem);
            btn_favorite = itemView.findViewById(R.id.btn_favorite);
        }
    }
}