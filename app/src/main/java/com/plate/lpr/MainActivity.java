package com.plate.lpr;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {

    PlateRecognition plateRecognition;

    private ImageView imageView;
    private Button in_b,re_b;
    private Bitmap bitmap1;
    public long handle;
    private String re_str;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestDangerousPermissions();

        imageView=findViewById(R.id.img);
        in_b=findViewById(R.id.in_img);
        re_b=findViewById(R.id.reg);

        in_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //intent可以应用于广播和发起意图，其中属性有：ComponentName,action,data等
                Intent intent=new Intent();
                intent.setType("image/*");
                //action表示intent的类型，可以是查看、删除、发布或其他情况；我们选择ACTION_GET_CONTENT，系统可以根据Type类型来调用系统程序选择Type
                //类型的内容给你选择
                intent.setAction(Intent.ACTION_GET_CONTENT);
                //如果第二个参数大于或等于0，那么当用户操作完成后会返回到本程序的onActivityResult方法
                startActivityForResult(intent, 1);

            }
        });


        re_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Mat m=new Mat();
                Utils.bitmapToMat(bitmap1,m);
                re_str=PlateRecognition.SimpleRecognization(m.getNativeObjAddr(), handle);


                Message message=new Message();
                message.obj=re_str;
                message.what=100;
                mHandler.sendMessage(message);

            }
        });

    }
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 100://recognize finish
                    String result = (String) msg.obj;
                    Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
                    re_str=null;
                    break;
            }
        }
    };

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {

            Log.i("cv", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
        } else {
            Log.i("cv", "OpenCV library found inside package. Using it!");

          //  plateRecognition=new PlateRecognition();
//            System.loadLibrary("lpr");
//            DeepAssetUtil.initRecognizer(MainActivity.this);

            System.loadLibrary("lpr");

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    handle = DeepAssetUtil.initRecognizer(MainActivity.this);
                    return null;
                }
            }.execute();





        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //用户操作完成，结果码返回是-1，即RESULT_OK
        if(resultCode==RESULT_OK){
            //获取选中文件的定位符
            Uri uri = data.getData();
            Log.e("uri", uri.toString());
            //使用content的接口
            ContentResolver cr = this.getContentResolver();
            try {
                //获取图片
                bitmap1 = BitmapFactory.decodeStream(cr.openInputStream(uri));
                imageView.setImageBitmap(bitmap1);

            } catch (FileNotFoundException e) {
                Log.e("Exception", e.getMessage(),e);
            }
        }else{
            //操作错误或没有选择图片
            Log.i("MainActivtiy", "operation error");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void requestDangerousPermissions() {
        String[] strings = new String[]{
//                Manifest.permission.CAMERA,
//                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(this, strings, 100);
    }
}
