package ru.rutoken.demobank.ui.usertable;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ru.rutoken.demobank.R;
import ru.rutoken.demobank.database.User;

import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.MyViewHolder> {

    private Context context;
    private List<User> userList;
    public UserListAdapter(Context context) {
        this.context = context;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_user_list_adapter, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserListAdapter.MyViewHolder holder, int position) {
        holder.tokenSerialName.setText(this.userList.get(position).TokenSerialNumber);
        //holder.CodeName.setText(this.userList.get(position).codename);
        holder.vector.setText(this.userList.get(position).vector);
    }

    @Override
    public int getItemCount() {
        return  this.userList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView tokenSerialName;
        TextView CodeName;
        TextView vector;

        public MyViewHolder(View view) {
            super(view);
            tokenSerialName = view.findViewById(R.id.TokenSerialName);
            CodeName = view.findViewById(R.id.codename);
            vector = view.findViewById(R.id.vector);
        }
    }
}