package com.nanodatacenter.monitorwebview;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.BounceInterpolator;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    NestedScrollView scrollView;
    RelativeLayout progressBar;
    private MediaPlayer mediaPlayer;
    List<Integer> imageViewIds = Arrays.asList(
            R.id.rack_info, //0
            R.id.node_info, //1
            R.id.onboarding, //2
            R.id.switch_40,  //3
            R.id.node_miner, //4
            R.id.post_worker, //5
            R.id.pc2_1, //6
            R.id.pc2_2,  //7
            R.id.pc2_3, //8
            R.id.storage_6, //9
            R.id.upscontroller, //10
            R.id.storage_1, //11
            R.id.storage_2, //12
            R.id.storage_3, //13
            R.id.storage_4, //14
            R.id.storage_5,//15
            R.id.logo_zetacube //16
    );
    List<Integer> webViewIds = Arrays.asList(
            R.id.rack_info_web, //0
            R.id.node_info_web,
            R.id.onboarding_web,
            R.id.switch_40_web,
            R.id.node_miner_web,
            R.id.post_worker_web,
            R.id.pc2_1_web,
            R.id.pc2_2_web,  //15
            R.id.pc2_3_web,
            R.id.storage_6_web,
            R.id.upscontroller_web,
            R.id.storage_1_web,
            R.id.storage_2_web,
            R.id.storage_3_web,
            R.id.storage_4_web,
            R.id.storage_5_web,//20
            R.id.logo_zetacube_web
    );
    String domain = "121.172.116.208";
    List<String> webViewLinks = Arrays.asList(
            "http://"+domain+":8070/monitor_rackInfo", //RackStatus
            "http://"+domain+":8070/monitor_nodeInfo?minerId=f03091958",//Node INFO
            "http://"+domain+":8070/monitor_boostInfo?minerId=f03091958", //Boost
            "http://"+domain+":8070/monitor_switchInfo", //Switch
            "http://"+domain+":8070/monitor_hardwareInfo?minerId=f01695888&source_link=112.160.8.138:9100/metrics",//PC1
            "http://"+domain+":8070/monitor_hardwareInfo?minerId=f01695888&source_link=112.160.8.138:9100/metrics",//PC2
            "http://"+domain+":8070/monitor_hardwareInfo?minerId=f01695888&source_link=112.160.8.138:9100/metrics",//PC3
            "http://"+domain+":8070/monitor_hardwareInfo?minerId=f01695888&source_link=112.160.8.138:9100/metrics",//PC4
            "http://"+domain+":8070/monitor_hardwareInfo?minerId=f01695888&source_link=112.160.8.138:9100/metrics",//PC5
            "http://"+domain+"+:8070/monitor_upsController",
            "http://"+domain+":8070/monitor_upsController",// ups
            "http://"+domain+":8070/monitor_storageInfo", // storage// homepage
            "http://"+domain+":8070/monitor_hardwareInfo?minerId=f03091958&source_link=112.160.8.138:9100/metrics",//Miner
            "http://"+domain+":8070/monitor_homepage",// PostWorker
            "http://"+domain+":8070/monitor_homepage",
            "http://"+domain+":8070/monitor_homepage",
            "http://"+domain+":8070/monitor_homepage",// pc2
            "http://"+domain+":8070/monitor_upsController", // ups
            "http://"+domain+":8070/monitor_storageInfo", // storage
            "http://"+domain+":8070/monitor_storageInfo", // storage
            "http://"+domain+":8070/monitor_storageInfo", // storage
            "http://"+domain+":8070/monitor_homepage"   // homepage
            );
    List<Integer> buttonLayouts = Arrays.asList(
            R.id.operations1,
            R.id.operations2,
            R.id.operations3,
            R.id.operations4,
            R.id.operations13,
            R.id.operations14,
            R.id.operations15,
            R.id.operations16,
            R.id.operations17,
            R.id.operations25,
            R.id.operations18,
            R.id.operations19,
            R.id.operations20,
            R.id.operations21,
            R.id.operations23,
            R.id.operations24,
            R.id.operations22
    );
    List<Boolean> existHardWareButton = Arrays.asList(
            false,
            false,
            false,
            false,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            false
    );
    List<Integer> rebootButtonsIds = Arrays.asList(
            R.id.reboot1,
            R.id.reboot2,
            R.id.reboot3,
            R.id.reboot4,
            R.id.reboot13,
            R.id.reboot14,
            R.id.reboot15,
            R.id.reboot16,
            R.id.reboot17,
            R.id.reboot18,
            R.id.reboot19,
            R.id.reboot20,
            R.id.reboot21,
            R.id.reboot22
    );
    List<Integer> shutdownButtonsIds = Arrays.asList(
            R.id.shutdown1,
            R.id.shutdown2,
            R.id.shutdown3,
            R.id.shutdown4,
            R.id.shutdown13,
            R.id.shutdown14,
            R.id.shutdown15,
            R.id.shutdown16,
            R.id.shutdown17,
            R.id.shutdown18,
            R.id.shutdown19,
            R.id.shutdown20,
            R.id.shutdown21,
            R.id.shutdown22
    );
    List<Integer> imageViewsScrollLocation;
    int mainOpening = R.raw.door;
    int sideOpening = R.raw.short_door4;
    List<WebView> webViews;

    private final Handler mHandler = new Handler();
    private final Runnable mRunnable = this::close_down_all;
    int webLoadCnt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        progressBar = findViewById(R.id.progress_bar);
        scrollView = findViewById(R.id.scroll_view);

        webViews = new ArrayList<>();
        imageViewsScrollLocation = new ArrayList<>();
        serverButtonsInitializing();
        webViewInitializing();

        imageViewInitializing();
    }

    public void playSound(int soundResId) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = MediaPlayer.create(this, soundResId);
        mediaPlayer.start();
    }

    public void webViewInitializing() {
        for (int i = 0; i < webViewIds.size(); i++) {
            WebView webView = findViewById(webViewIds.get(i));
            WebSettings settings = webView.getSettings();
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            settings.setDomStorageEnabled(true);
            settings.setJavaScriptEnabled(true);
            settings.setUseWideViewPort(true);
            TextView textView = findViewById(R.id.text_box);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    webLoadCnt++;
                    if (webLoadCnt >= existHardWareButton.size()) {
                        close_down_all();
                        progressBar.setVisibility(View.GONE);
                        scrollView.setVisibility(View.VISIBLE);

                        //mHandler.postDelayed(mRunnable, 60000); // 60 초있음 자동 닫히는 기능
                    } else {
                        textView.setText("App is Loading... \n" + webLoadCnt + " out of " + existHardWareButton.size() + " loaded complete");
                    }
                }
            });
            webView.loadUrl(webViewLinks.get(i));
            webViews.add(webView);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public void imageViewInitializing() {
        for (int i = 0; i < imageViewIds.size(); i++) {
            ImageView imageView = findViewById(imageViewIds.get(i));
            imageViewsScrollLocation.add(imageView.getTop());
            int index = i;
            // 7번, 8번, 11번, 12번 버튼에 대한 이벤트 리스너 설정을 건너뜁니다.
            if (index == 7 || index == 8 ||index == 9  || index == 13 || index == 14 || index == 15) {
                continue;
            }
            imageView.setOnTouchListener(new View.OnTouchListener() {
                private final Handler handler = new Handler();
                private boolean longPressDetected = false;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                /*mHandler.removeCallbacks(mRunnable);
                mHandler.postDelayed(mRunnable, 60000);*/ // 60 초있음 자동 닫히는 기능
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            imageView.setAlpha(0.5f);
                            handler.postDelayed(longPressRunnable, 1000);
                            break;
                        case MotionEvent.ACTION_UP:
                            handler.removeCallbacks(longPressRunnable);
                            imageView.setAlpha(1.0f);
                            if (!longPressDetected) {
                                button_up(imageView, false);
                                playSound(mainOpening);
                            }
                            applyElasticEffect(v);
                            longPressDetected = false;
                            break;
                        case MotionEvent.ACTION_CANCEL:
                            handler.removeCallbacks(longPressRunnable);
                            imageView.setAlpha(1.0f);
                            break;
                    }
                    return true;
                }

                private final Runnable longPressRunnable = () -> {
                    longPressDetected = true;
                    if (existHardWareButton.get(index)) {
                        button_up(imageView, true);
                        playSound(sideOpening);
                    } else {
                        button_up(imageView, false);
                        playSound(mainOpening);
                    }
                };
            });
        }
    }


    public void applyElasticEffect(View view) {
        ImageView imageView = (ImageView) view;

        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(imageView, View.SCALE_X, 1.2f, 1.0f);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(imageView, View.SCALE_Y, 0.8f, 1.0f);

        scaleXAnimator.setDuration(1000);
        scaleYAnimator.setDuration(1000);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleXAnimator, scaleYAnimator);

        animatorSet.setInterpolator(new BounceInterpolator());

        animatorSet.start();
    }

    @SuppressLint("ClickableViewAccessibility")
    public void serverButtonsInitializing() {
        for (int i = 0; i < rebootButtonsIds.size(); i++) {
            ImageView rebootButton = findViewById(rebootButtonsIds.get(i));
            ImageView shutdownButton = findViewById(shutdownButtonsIds.get(i));
            rebootButton.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        rebootButton.setAlpha(0.5f);
                        break;
                    case MotionEvent.ACTION_UP:
                        rebootButton.setAlpha(1.0f);
                        playSound(sideOpening);
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        rebootButton.setAlpha(1.0f);
                        break;
                }
                return true;
            });
            shutdownButton.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        shutdownButton.setAlpha(0.5f);
                        break;
                    case MotionEvent.ACTION_UP:
                        shutdownButton.setAlpha(1.0f);
                        playSound(sideOpening);
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        shutdownButton.setAlpha(1.0f);
                        break;
                }
                return true;
            });
        }
    }

    private void handleLongPress(ImageView picked_imageView, boolean isLong, int index) {
        ImageView imageView = findViewById(imageViewIds.get(index));
        LinearLayout buttonLayout = findViewById(buttonLayouts.get(index));
        if (isLong && buttonLayout.getVisibility() == View.GONE && imageView == picked_imageView) {
            buttonLayout.setVisibility(View.VISIBLE);
        } else {
            buttonLayout.setVisibility(View.GONE);
        }
    }

    public void close_down_all() {
        for (int i = 0; i < webViewIds.size(); i++) {
            WebView webView = webViews.get(i);
            ViewGroup.LayoutParams layoutParams = webView.getLayoutParams();
            layoutParams.height = 0;
            webView.setLayoutParams(layoutParams);
            webView.setVisibility(View.GONE);
            LinearLayout buttonLayout = findViewById(buttonLayouts.get(i));
            buttonLayout.setVisibility(View.GONE);
        }
    }

    public void button_up(ImageView picked_imageView, boolean isLong) {
        for (int i = 0; i < webViews.size(); i++) {
            ImageView imageView = findViewById(imageViewIds.get(i));
            WebView webView = webViews.get(i);
            handleLongPress(picked_imageView, isLong, i);
            ViewGroup.LayoutParams layoutParams = webView.getLayoutParams();
            int targetHeight;
            if (!isLong && webView.getVisibility() == View.GONE && imageView == picked_imageView) {
                webView.setVisibility(View.VISIBLE);
                targetHeight = webView.getContentHeight();
            } else {
                webView.setVisibility(View.GONE);
                targetHeight = 0;
            }

            ValueAnimator webViewAnimator = ValueAnimator.ofInt(layoutParams.height, targetHeight);
            webViewAnimator.addUpdateListener(animation -> {
                layoutParams.height = (int) animation.getAnimatedValue();
                webView.setLayoutParams(layoutParams);
            });

            ValueAnimator scrollViewAnimator;
            if (picked_imageView == imageView) {
                scrollViewAnimator = ValueAnimator.ofInt(scrollView.getScrollY(), picked_imageView.getTop());
            } else {
                scrollViewAnimator = ValueAnimator.ofInt(scrollView.getScrollY(), picked_imageView.getTop() - 500); // 예시로 500만큼 위로 스크롤
            }
            scrollViewAnimator.addUpdateListener(animation ->
                    scrollView.scrollTo(0, (int) animation.getAnimatedValue())
            );

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.play(webViewAnimator);
            animatorSet.playTogether(webViewAnimator, scrollViewAnimator);
            animatorSet.setDuration(200);
            animatorSet.start();
        }
    }

}