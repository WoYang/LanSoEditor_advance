package com.example.lansongeditordemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import su.levenetc.android.textsurface.TextSurface;
import su.levenetc.android.textsurface.interfaces.IEndListener;
import su.levenetc.android.textsurface.interfaces.ISurfaceAnimation;

import jp.co.cyberagent.lansongsdk.gpuimage.GPUImageFilter;

import com.example.lansong.animview.LanSongLoveText;
import com.example.lansongeditordemo.view.DrawPadView;
import com.example.lansongeditordemo.view.DrawPadView.onViewAvailable;
import com.lansoeditor.demo.R;
import com.lansosdk.box.DrawPad;
import com.lansosdk.box.DrawPadUpdateMode;
import com.lansosdk.box.VideoPen;
import com.lansosdk.box.ViewPen;
import com.lansosdk.box.Pen;
import com.lansosdk.box.ViewPenRelativeLayout;
import com.lansosdk.box.onDrawPadCompletedListener;
import com.lansosdk.box.onDrawPadProgressListener;
import com.lansosdk.box.onDrawPadSizeChangedListener;
import com.lansosdk.videoeditor.CopyFileFromAssets;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.SDKDir;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;
import com.lansosdk.videoeditor.player.IMediaPlayer;
import com.lansosdk.videoeditor.player.IMediaPlayer.OnPlayerPreparedListener;
import com.lansosdk.videoeditor.player.VPlayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

/**
 *  演示:  告白浪漫情诗 .
 *  流程: 在DrawPad画板上增加一个ViePen的画板 ,利用TextSurface这个文字动画效果的开源库, 绘制出浪漫的情诗文字.
 *  
 *  
 */
public class ViewPenRealTimeActivity extends Activity implements IEndListener{
    private static final String TAG = "ViewPenPrivewDemoActivity";


    private DrawPadView mPlayView;
    /**
     * 采用github上开源的文字动画类, 您可以从https://github.com/elevenetc/TextSurface下载源代码.
     * 当前也可以直接使用我们封装好的textsurface.jar库.
     */
    private TextSurface textSurface;
    private ViewPenRelativeLayout mGLRelativeLayout;
    
    
    private String dstPath=null;
    private Context mContext=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
		 Thread.setDefaultUncaughtExceptionHandler(new snoCrashHandler());
        setContentView(R.layout.viewpen_preview_layout);
        
        
        mPlayView = (DrawPadView) findViewById(R.id.id_viewPen_DrawPad_view);
        
        
        findViewById(R.id.id_viewPen_saveplay).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 if(SDKFileUtils.fileExist(dstPath)){
		   			 	Intent intent=new Intent(ViewPenRealTimeActivity.this,VideoPlayerActivity.class);
			    	    	intent.putExtra("videopath", dstPath);
			    	    	startActivity(intent);
		   		 }else{
		   			 Toast.makeText(ViewPenRealTimeActivity.this, "目标文件不存在", Toast.LENGTH_SHORT).show();
		   		 }
			}
		});
        
        findViewById(R.id.id_viewPen_saveplay).setVisibility(View.GONE);
        
        mGLRelativeLayout=(ViewPenRelativeLayout)findViewById(R.id.id_viewPen_gllayout);
        textSurface = (TextSurface) findViewById(R.id.text_surface);


        //在手机的/sdcard/lansongBox/路径下创建一个文件名,用来保存生成的视频文件,(在onDestroy中删除)
        dstPath=SDKFileUtils.newMp4PathInBox();
//        dstPath="/sdcard/text2.mp4";
        
        mContext=getApplicationContext();
        
    }
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				startDrawPad();
			}
		}, 100);
    }
    //Step1:开始运行 DrawPad 画板
    private void startDrawPad()
    {
		//设置为自动刷新模式, 帧率为25
    	mPlayView.setUpdateMode(DrawPadUpdateMode.AUTO_FLUSH,25);
    	//使能实时录制,并设置录制后视频的宽度和高度, 码率, 帧率,保存路径.//因为视频就是一些文字,没有剧烈的画面运动,这里把码率设置小一些为600k
    	mPlayView.setRealEncodeEnable(480,480,1000*1000,(int)25,dstPath);
    	
    	//设置DrawPad的宽高, 这里设置为480x480,如果您已经在xml中固定大小,则不需要再次设置,
    	//可以直接调用startDrawPad来开始录制.
    	mPlayView.setDrawPadSize(480,480,new onDrawPadSizeChangedListener() {
			
			@Override
			public void onSizeChanged(int viewWidth, int viewHeight) {
				// TODO Auto-generated method stub
				mPlayView.startDrawPad(null,new DrawPadCompleted());
				
				
				   DisplayMetrics dm = new DisplayMetrics();// 获取屏幕密度（方法2）
			       dm = getResources().getDisplayMetrics();
			      addViewPen();
			      playGaoBai();
			}
		});
    }
    
    private ViewPen mViewPen=null;
    
    //Step2 增加一个ViewPen到画板上.
    private void addViewPen()
    {
    	mViewPen=mPlayView.addViewPen();
        mGLRelativeLayout.setViewPen(mViewPen);
        mGLRelativeLayout.invalidate();
        
        ViewGroup.LayoutParams  params=mGLRelativeLayout.getLayoutParams();
        params.height=mViewPen.getHeight();  //因为布局时, 宽度一致, 这里调整高度,让他们一致.
        
        mGLRelativeLayout.setLayoutParams(params);
    }
    //DrawPad完成时的回调.
    private class DrawPadCompleted implements onDrawPadCompletedListener
    {

		@Override
		public void onCompleted(DrawPad v) {
			// TODO Auto-generated method stub
			
			if(isDestorying==false){
				if(SDKFileUtils.fileExist(dstPath)){
					//可以在这里利用VideoEditor.java类来增加声音等.
					
//					VideoEditor editor=new VideoEditor();
					//videoFile源文件(dstPath); audioFile音频文件, dstFile生成的目标文件.
//					editor.executeVideoMergeAudio(videoFile, audioFile, dstFile);
					
			
					
			    	findViewById(R.id.id_viewPen_saveplay).setVisibility(View.VISIBLE);
				}
				toastStop();
			}
		}
    }

    private void toastStop()
    {
    	Toast.makeText(getApplicationContext(), "录制已停止!!", Toast.LENGTH_SHORT).show();
    }
    
    boolean isDestorying=false;  //是否正在销毁, 因为销毁会停止DrawPad
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	
    	
    	isDestorying=true;
    	if(mPlayView!=null){
    		mPlayView.stopDrawPad();
    		mPlayView=null;        		   
    	}
    	
    	if(SDKFileUtils.fileExist(dstPath)){
    		SDKFileUtils.deleteFile(dstPath);
        }
    	gaoBaiChapter=MAX_CHAPTER+1; //不在让画面更新.
    }
//  /-----------------------------
    private int gaoBaiChapter=0;
	private int MAX_CHAPTER=6;
	private void playGaoBai()
	{
		gaoBaiChapter++;
		if(gaoBaiChapter>MAX_CHAPTER){
			
			//TextSurface 先调用endlisnter,再开始绘制,这样最后一帧有可能没有绘制出来,这里演示折中一下.
			new Handler().postDelayed(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					if(mPlayView!=null)
						mPlayView.stopDrawPad();
				}
			},500);
			return ;
		}
		textSurface.reset();
		switch (gaoBaiChapter) {
			case 1:
				LanSongLoveText.play(getApplicationContext(),textSurface, getAssets(),this);
				break;
			case 2:
				LanSongLoveText.play2(getApplicationContext(),textSurface, getAssets(),this);
				break;
			case 3:
				LanSongLoveText.play3(getApplicationContext(),textSurface, getAssets(),this);
				break;
			case 4:
				LanSongLoveText.play4(getApplicationContext(),textSurface, getAssets(),this);
				break;
			case 5:
				LanSongLoveText.play5(getApplicationContext(),textSurface, getAssets(),this);
				break;
			case 6:
				LanSongLoveText.play6(getApplicationContext(),textSurface, getAssets(),this);
				break;
		default:
			break;
		}
	}
	//su.levenetc.android.textsurface.interfaces.IEndListener的监听.
	@Override
	public void onAnimationEnd(ISurfaceAnimation animation) {
		// TODO Auto-generated method stub
		playGaoBai();
	}
}
