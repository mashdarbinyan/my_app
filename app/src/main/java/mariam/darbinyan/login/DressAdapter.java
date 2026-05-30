package mariam.darbinyan.login;

import android.app.Activity;
import android.content.Context;
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
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.File;
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
        Context context = holder.itemView.getContext();

        holder.btn_favorite.setVisibility(isSelectMode ? View.GONE : View.VISIBLE);


        if (imageString.startsWith("http")) {
            Glide.with(context).load(imageString).placeholder(android.R.drawable.ic_menu_gallery).into(holder.imageView);
        } else {
            File file = new File(context.getFilesDir(), imageString);
            if (file.exists()) {
                Glide.with(context).load(file).into(holder.imageView);
            } else {
                holder.imageView.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }


        if (!isSelectMode) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference favRef = FirebaseDatabase.getInstance(dbUrl).getReference("Users").child(userId).child("favorites");
            String favoriteKey = String.valueOf(imageString.hashCode());

            favRef.child(favoriteKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    holder.btn_favorite.setImageResource(snapshot.exists() ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });

            holder.btn_favorite.setOnClickListener(v -> {
                favRef.child(favoriteKey).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            favRef.child(favoriteKey).removeValue().addOnSuccessListener(aVoid -> {
                                holder.btn_favorite.setImageResource(android.R.drawable.btn_star_big_off);
                                if (isFavoriteScreen) { imageUrls.remove(position); notifyItemRemoved(position); }
                            });
                        } else {
                            favRef.child(favoriteKey).setValue(imageString).addOnSuccessListener(aVoid -> holder.btn_favorite.setImageResource(android.R.drawable.btn_star_big_on));
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
            });
        }


        holder.imageView.setOnLongClickListener(v -> {
            if (!isSelectMode) {
                new AlertDialog.Builder(context)
                        .setTitle("Delete Item")
                        .setPositiveButton("Delete", (dialog, which) -> deleteFromFirebase(imageString, position, context))
                        .setNegativeButton("Cancel", null).show();
            }
            return true;
        });

        holder.imageView.setOnClickListener(v -> {
            if (isSelectMode) {
                Intent intent = new Intent();
                intent.putExtra("PICKED_IMAGE", imageString);
                ((Activity) context).setResult(Activity.RESULT_OK, intent);
                ((Activity) context).finish();
            } else {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("image_data", imageString);
                context.startActivity(intent);
            }
        });
    }

    private void deleteFromFirebase(String imageContent, int position, Context context) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance(dbUrl).getReference("Users").child(userId).child(categoryPath);

        ref.orderByValue().equalTo(imageContent).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) data.getRef().removeValue();

                if (!imageContent.startsWith("http")) {
                    new File(context.getFilesDir(), imageContent).delete();
                }

                imageUrls.remove(position);
                notifyItemRemoved(position);
                Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override public int getItemCount() { return imageUrls.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView; ImageButton btn_favorite;
        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageDressItem);
            btn_favorite = itemView.findViewById(R.id.btn_favorite);
        }
    }
}