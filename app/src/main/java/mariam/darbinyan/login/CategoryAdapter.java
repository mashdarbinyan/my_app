package mariam.darbinyan.login;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private final List<CategoryModel> categoryList;
    private final OnCategoryLongClickListener listener;


    public interface OnCategoryLongClickListener {
        void onCategoryLongClick(CategoryModel category);
    }

    public CategoryAdapter(List<CategoryModel> categoryList, OnCategoryLongClickListener listener) {
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryModel category = categoryList.get(position);
        holder.textView.setText(category.getName());
        holder.textView.setPadding(32, 32, 32, 32);
        holder.textView.setTextSize(18);
        holder.textView.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.purpule));


        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), CustomCategoryActivity.class);
            intent.putExtra("CATEGORY_NAME", category.getName());
            intent.putExtra("CATEGORY_KEY", category.getKey());
            v.getContext().startActivity(intent);
        });


        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onCategoryLongClick(category);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}