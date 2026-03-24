package mariam.darbinyan.login;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class DressAdapter extends RecyclerView.Adapter<DressAdapter.ViewHolder> {

    private List<String> imageUrls;

    public DressAdapter(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // This links to the item_dress.xml we talked about
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dress, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imageString = imageUrls.get(position);

        try {
            // 1. Convert the Base64 text back into a Byte Array
            byte[] decodedString = android.util.Base64.decode(imageString, android.util.Base64.DEFAULT);

            // 2. Turn the Byte Array into a real Bitmap photo
            android.graphics.Bitmap decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            // 3. Put the photo into your ImageView
            holder.imageView.setImageBitmap(decodedByte);

        } catch (Exception e) {
            // Backup: If it's a normal web link, use Glide
            com.bumptech.glide.Glide.with(holder.itemView.getContext())
                    .load(imageString)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(holder.imageView);
        }
    }
    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // This ID must match the ImageView inside item_dress.xml
            imageView = itemView.findViewById(R.id.imageDressItem);
        }
    }
}