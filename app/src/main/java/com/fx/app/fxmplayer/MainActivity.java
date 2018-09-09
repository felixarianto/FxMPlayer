package com.fx.app.fxmplayer;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:

                    return true;
                case R.id.navigation_dashboard:
                    return true;
                case R.id.navigation_notifications:
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    }

    private void createList() {

    }

    private class Holder extends RecyclerView.ViewHolder {

        public TextView title;
        public TextView time;

        public Holder(@NonNull View itemView) {
            super(itemView);
        }

    }

    private class Adapter extends RecyclerView.Adapter<Holder> {

        public ArrayList<Object[]> DATA = new ArrayList<>();
        //0:_id 1:title 2:sc 3:ec

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = viewGroup.inflate(viewGroup.getContext(), R.layout.track_adapter, null);
            Holder holder = new Holder(view);
            holder.title  = view.findViewById(R.id.title);
            holder.time   = view.findViewById(R.id.time);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int index) {

        }

        @Override
        public int getItemCount() {
            return DATA.size();
        }
    }


}
