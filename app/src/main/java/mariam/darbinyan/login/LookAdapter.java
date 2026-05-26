package mariam.darbinyan.login;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.io.File;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saved_look, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, String> look = lookList.get(position);
        holder.tvName.setText(look.get("lookName"));

        setImage(look.get("dress"), holder.imgDress);
        setImage(look.get("jacket"), holder.imgJacket);
        setImage(look.get("pants"), holder.imgPants);
        setImage(look.get("shoes"), holder.imgShoes);

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

    private void setImage(String content, ImageView imageView) {
        if (content != null && !content.isEmpty()) {
            imageView.setVisibility(View.VISIBLE);
            android.content.Context context = imageView.getContext();
            if (content.startsWith("http")) {
                Glide.with(context).load(content).into(imageView);
            } else {
                File file = new File(context.getFilesDir(), content);
                Glide.with(context).load(file).into(imageView);
            }
        } else {
            imageView.setVisibility(View.GONE);
        }
    }

    @Override public int getItemCount() { return lookList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView imgDress, imgJacket, imgPants, imgShoes;
        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvSavedLookName);
            imgDress = itemView.findViewById(R.id.imgPreviewDress);
            imgJacket = itemView.findViewById(R.id.imgPreviewJacket);
            imgPants = itemView.findViewById(R.id.imgPreviewPants);
            imgShoes = itemView.findViewById(R.id.imgPreviewShoes);
        }
    }
}