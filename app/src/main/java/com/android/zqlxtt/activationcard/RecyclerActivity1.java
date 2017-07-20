package com.android.zqlxtt.activationcard;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.concurrent.Executors;

public class RecyclerActivity1 extends AppCompatActivity {


    private RecyclerView recyclerView;

    private Handler handler = new Handler();
    LruCache<String,Bitmap> cache = new LruCache<String,Bitmap>(12*1024*1024){
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        recyclerView.setLayoutManager(linearLayoutManager);
        final Adapter adapter = new Adapter();
        recyclerView.setAdapter(adapter);
        ActivationCard.enableCache();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private class Adapter extends RecyclerView.Adapter<Holder>{

        private int[] res = {R.drawable.a1,
                R.drawable.a2,
                R.drawable.a3,
                R.drawable.a4,
                R.drawable.a5,
                R.drawable.a1,
                R.drawable.a2,
                R.drawable.a3,
                R.drawable.a4,
                R.drawable.a5,
                R.drawable.a1,
                R.drawable.a2,
                R.drawable.a3,
                R.drawable.a4,
                R.drawable.a5};

        private int prePosition = -1;
        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.item_layout,parent,false);
            Holder holder = new Holder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(final Holder holder, final int position) {
            final String key = String.valueOf(position);
            final Bitmap bitmap = cache.get(key);
            if(bitmap != null){
                holder.activationCard.enableActivation(bitmap,key);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(prePosition > position){
                            holder.activationCard.postRight();
                        }else {
                            holder.activationCard.postLeft();
                        }
                        prePosition = position;
                    }
                },200);

            }else {
                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        final Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(),res[position]);
                        final Bitmap reConfigBitmap = bitmap1.copy(Bitmap.Config.RGB_565,true);
                        bitmap1.recycle();
                        cache.put(key,reConfigBitmap);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                holder.activationCard.enableActivation(reConfigBitmap,key);
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(prePosition > position){
                                            holder.activationCard.postRight();
                                        }else {
                                            holder.activationCard.postLeft();
                                        }
                                        prePosition = position;
                                    }
                                },200);
                            }
                        });
                    }
                });
            }

        }

        @Override
        public int getItemCount() {
            return res.length;
        }
    }

    private class Holder extends RecyclerView.ViewHolder{

        ActivationCard activationCard;
        public Holder(View itemView) {
            super(itemView);
            activationCard = (ActivationCard) itemView.findViewById(R.id.activation_card);
        }
    }
}
