package mariam.darbinyan.login;

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
    private boolean isFavoriteScreen; // Added variable
    private String dbUrl = "https://mariam-sproject-default-rtdb.europe-west1.firebasedatabase.app/";

    // Updated constructor to handle the Favorite Screen state
    public DressAdapter(List<String> imageUrls, String categoryPath, boolean isFavoriteScreen) {
        this.imageUrls = imageUrls;
        this.categoryPath = categoryPath;
        this.isFavoriteScreen = isFavoriteScreen;
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
        if (context instanceof android.app.Activity) {
            if (((android.app.Activity) context).isFinishing() || ((android.app.Activity) context).isDestroyed()) {
                return;
            }
        }

        try {
            byte[] decodedString = android.util.Base64.decode(imageString, android.util.Base64.DEFAULT);
            com.bumptech.glide.Glide.with(context)
                    .asBitmap()
                    .load(decodedString)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.stat_notify_error)
                    .into(holder.imageView);
        } catch (Exception e) {
            com.bumptech.glide.Glide.with(context)
                    .load(imageString)
                    .into(holder.imageView);
        }

        // --- DYNAMIC STAR ICON ---
        if (isFavoriteScreen) {
            holder.btn_favorite.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            holder.btn_favorite.setImageResource(android.R.drawable.btn_star_big_off);
        }

        // --- FAVORITES & REMOVAL LOGIC ---
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference favRef = FirebaseDatabase.getInstance(dbUrl).getReference("users")
                .child(userId).child("favorites");

        holder.btn_favorite.setOnClickListener(v -> {
            String favoriteKey = String.valueOf(imageString.hashCode());

            if (isFavoriteScreen) {
                // If we are in the Favorites Activity, clicking the star REMOVES it
                favRef.child(favoriteKey).removeValue().addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Removed from Favorites", Toast.LENGTH_SHORT).show();
                    if (position < imageUrls.size()) {
                        imageUrls.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, imageUrls.size());
                    }
                });
            } else {
                // If we are in a regular Activity, clicking the star SAVES it
                favRef.child(favoriteKey).setValue(imageString).addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Saved to My Favorites! ❤️", Toast.LENGTH_SHORT).show();
                    holder.btn_favorite.setImageResource(android.R.drawable.btn_star_big_on);
                });
            }
        });

        holder.imageView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Delete Item")
                    .setMessage("Do you want to remove this from your wardrobe?")
                    .setPositiveButton("Delete", (dialog, which) -> deleteFromFirebase(imageString, v, position))
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });

        holder.imageView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(v.getContext(), ChatActivity.class);
            intent.putExtra("image_data", imageString);
            v.getContext().startActivity(intent);
        });
    }

    private void deleteFromFirebase(String imageContent, View view, int position) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance(dbUrl)
                .getReference("users").child(userId).child(categoryPath);

        ref.orderByValue().equalTo(imageContent).addListenerForSingleValueEvent(new ValueEventListener() {
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
        ImageButton btn_favorite;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageDressItem);
            btn_favorite = itemView.findViewById(R.id.btn_favorite);
        }
    }
}