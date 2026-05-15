package mariam.darbinyan.login;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;

public class LookAdapter extends RecyclerView.Adapter<LookAdapter.ViewHolder> {
    private List<Map<String, String>> lookList;

    public LookAdapter(List<Map<String, String>> lookList) {
        this.lookList = lookList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate your custom layout (item_saved_look) instead of the simple system one
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saved_look, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // 1. Get the data for the current row
        Map<String, String> look = lookList.get(position);

        // 2. Set the Title
        holder.tvName.setText(look.get("lookName"));

        // 3. Set the image previews (so the list looks pretty!)
        setImage(look.get("dress"), holder.imgDress);
        setImage(look.get("jacket"), holder.imgJacket);
        setImage(look.get("pants"), holder.imgPants);
        setImage(look.get("shoes"), holder.imgShoes);

        // 4. Handle the click to open Detail View
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), LookDetailActivity.class);

            intent.putExtra("lookName", look.get("lookName"));
            intent.putExtra("dress", look.get("dress"));
            intent.putExtra("jacket", look.get("jacket"));
            intent.putExtra("pants", look.get("pants"));
            intent.putExtra("shoes", look.get("shoes"));

            v.getContext().startActivity(intent);
        });
    }

    // Helper method to decode Base64 and show small previews in the list
    private void setImage(String b64, ImageView imageView) {
        if (b64 != null && !b64.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(b64, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                imageView.setImageBitmap(decodedByte);
                imageView.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                imageView.setVisibility(View.GONE);
            }
        } else {
            imageView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return lookList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView imgDress, imgJacket, imgPants, imgShoes;

        public ViewHolder(View itemView) {
            super(itemView);
            // These IDs must match your item_saved_look.xml
            tvName = itemView.findViewById(R.id.tvSavedLookName);
            imgDress = itemView.findViewById(R.id.imgPreviewDress);
            imgJacket = itemView.findViewById(R.id.imgPreviewJacket);
            imgPants = itemView.findViewById(R.id.imgPreviewPants);
            imgShoes = itemView.findViewById(R.id.imgPreviewShoes);
        }
    }
}