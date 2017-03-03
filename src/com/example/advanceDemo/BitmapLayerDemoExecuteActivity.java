package com.example.advanceDemo;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.lansoeditor.demo.R;
import com.lansosdk.box.BitmapLayer;
import com.lansosdk.box.Layer;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.DrawPadPictureExecute;
import com.lansosdk.box.DrawPadVideoExecute;
import com.lansosdk.box.ViewLayer;
import com.lansosdk.box.onDrawPadCompletedListener;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.videoeditor.CopyFileFromAssets;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.SDKDir;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;

/**
 * 后台执行 照片影集的功能. 
 * 使用DrawPad的扩展类:DrawPadPictureExecute来操作.
 * 
 */
public class BitmapLayerDemoExecuteActivity extends Activity{

	int videoDuration;
	boolean isRuned=false;
	TextView tvProgressHint;
	 TextView tvHint;
	 
	 
	    private String dstPath=null;
	    
	    private String picBackGround=null;
	    
	    private ArrayList<SlideEffect>  slideEffectArray;
	private static final String TAG="BitmapLayerDemoExecuteActivity";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		super.onCreate(savedInstanceState);
		 
		 
		 setContentView(R.layout.video_edit_demo_layout);
		 tvHint=(TextView)findViewById(R.id.id_video_editor_hint);
		 
		 tvHint.setText(R.string.pictureset_execute_demo_hint);
   
		 tvProgressHint=(TextView)findViewById(R.id.id_video_edit_progress_hint);
		 
	       findViewById(R.id.id_video_edit_btn).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
						testDrawPadExecute();
				}
			});
       
       findViewById(R.id.id_video_edit_btn2).setEnabled(false);
       findViewById(R.id.id_video_edit_btn2).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(SDKFileUtils.fileExist(dstPath)){
					Intent intent=new Intent(BitmapLayerDemoExecuteActivity.this,VideoPlayerActivity.class);
	    	    	intent.putExtra("videopath", dstPath);
	    	    	startActivity(intent);
				}else{
					 Toast.makeText(BitmapLayerDemoExecuteActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
				}
			}
		});
       DisplayMetrics dm = new DisplayMetrics();// 获取屏幕密度（方法2）
       dm = getResources().getDisplayMetrics();
        
      /**
       * 这里增加一个图层, 即作为最底部的一张图片.
       */
       
      int screenWidth  = dm.widthPixels;	
      picBackGround=SDKDir.TMP_DIR+"/"+"picname.jpg";   
      if(screenWidth>=1080){
    	  CopyFileFromAssets.copy(getApplicationContext(), "pic1080x1080u2.jpg", SDKDir.TMP_DIR, "picname.jpg");
      }  
      else{
    	  CopyFileFromAssets.copy(getApplicationContext(), "pic720x720.jpg", SDKDir.TMP_DIR, "picname.jpg");
      }

      //在手机的/sdcard/lansongBox/路径下创建一个文件名,用来保存生成的视频文件,(在onDestroy中删除)
       dstPath=SDKFileUtils.newMp4PathInBox();
	}
   @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	
    	if(vDrawPad!=null){
    		vDrawPad.release();
    		vDrawPad=null;
    	}
    	 if(SDKFileUtils.fileExist(dstPath)){
    		 SDKFileUtils.deleteFile(dstPath);
         }
    }
	   
   /**
    * 
    */
	VideoEditor mVideoEditer;
	/**
	 * 图片类的Layer
	 */
	BitmapLayer bitmapLayer=null;
	/**
	 * 使用DrawPad中的Picture执行类来做.
	 */
	DrawPadPictureExecute  vDrawPad=null;
	/**
	 * 当前是否已经在执行, 以免造成多次执行.
	 */
	private boolean isExecuting=false;

	
	private void testDrawPadExecute()
	{
		if(isExecuting)
			return ;
		
		isExecuting=true;
		//注意:这里的是直接把DrawPad设置为480x480,execute是没有自动缩放到屏幕的宽度的,如果加载图片,则最大的图片为480x480,如果超过则只显示480x480的部分.
		 /**
		  * DrawPad的图片转换为视频的后台执行
		  * @param ctx  语境,android的Context
		  * @param glwidth  opengl的display的宽度  可以认为是DrawPad这个池子的宽度.
		  * @param glheight  opengl的display的高度, 可以认为是DrawPad这个池子的高度.
		  * @param duration  视频时长
		  * @param framerate  帧率
		  * @param bitrate   编码视频所希望的码率,比特率,设置的越大,则文件越大, 设置小一些会起到视频压缩的效果.
		  * @param dstPath   编码视频保存的路径.
		  */
		 vDrawPad=new DrawPadPictureExecute(getApplicationContext(), 480, 480, 26*1000, 25, 1000000, dstPath);
		
		 /**
		  * 设置DrawPad的处理进度监听, 您可以在每一帧的过程中对ILayer做各种变化,比如平移,缩放,旋转,颜色变化,增删一个Layer等,来实现各种动画画面.
		  */
		vDrawPad.setDrawPadProgressListener(new onDrawPadProgressListener() {
			
			//currentTimeUs是当前时间戳,单位是微妙,可以根据时间戳/(MediaInfo.vDuration*1000000)来得到当前进度百分比.
			@Override
			public void onProgress(DrawPad v, long currentTimeUs) {
				// TODO Auto-generated method stub
				tvProgressHint.setText(String.valueOf(currentTimeUs));
			
				 if(slideEffectArray!=null && slideEffectArray.size()>0){
					  for(SlideEffect item: slideEffectArray){
						  item.run(currentTimeUs/1000);
					  }
				  }
			}
		});
		/**
		 * 处理完毕后的监听
		 */
		vDrawPad.setDrawPadCompletedListener(new onDrawPadCompletedListener() {
			
			@Override
			public void onCompleted(DrawPad v) {
				// TODO Auto-generated method stub
				tvProgressHint.setText("DrawPadExecute Completed!!!");
				
				isExecuting=false;
				//清空效果数组.
				if(slideEffectArray!=null){
			   		 for(SlideEffect item: slideEffectArray){
			   			vDrawPad.removeLayer(item.getLayer());
			   		 }
			   		 slideEffectArray.clear();
			   		 slideEffectArray=null;
		    	}

				if(SDKFileUtils.fileExist(dstPath)){
					findViewById(R.id.id_video_edit_btn2).setEnabled(true);
				}
			}
		});
		/**
		 *开始处理. 
		 */
		 vDrawPad.startDrawPad();
		 /**
		  * 可以在后台处理过程中,暂停画面的走动.比如想一次性增加多个Layer对象后,在让DrawPad执行,这样比在画面走动中获取更精确一些.
		  */
		 vDrawPad.pauseRefreshDrawPad(); 
		
			//设置一个背景,
			vDrawPad.addBitmapLayer(BitmapFactory.decodeFile(picBackGround));
	      
	       slideEffectArray=new ArrayList<SlideEffect>();
	      
			//这里同时增加多个,只是不显示出来.
	      getLayerToArray(R.drawable.pic1,0,5000);  		//1--5秒.
	      getLayerToArray(R.drawable.pic2,5000,10000);  //5--10秒.
	      getLayerToArray(R.drawable.pic3,10000,15000);	//10---15秒 
	      getLayerToArray(R.drawable.pic4,15000,20000);  //15---20秒
	      getLayerToArray(R.drawable.pic5,20000,25000);  //20---25秒
	      //增加完Layer后,再次恢复DrawPad,让其工作.
	      vDrawPad.resumeRefreshDrawPad();
	}
	  private void getLayerToArray(int resId,long startMS,long endMS)
	    {
	    	Layer item=vDrawPad.addBitmapLayer(BitmapFactory.decodeResource(getResources(), resId));
			SlideEffect  slide=new SlideEffect(item, 25, startMS, endMS, true);
			slideEffectArray.add(slide);
			
	    }
}	