依赖方法：
```groovy
allprojects {
    repositories {
        // JitPack 远程仓库：https://jitpack.io
        maven { url 'https://jitpack.io' }
    }
}
dependencies {
   implementation 'com.github.yulu1121.VideoPhotoPlay:videophoto:1.1.8'
}
```

1.xml中引入
```xml
  <com.github.videophotoplay.videoplay.AdvanceView
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:id="@+id/home_vp"
            android:clickable="false"
            />
```
2.Application onCreate中初始化VideoProxyManager,这个自己根据需求定义参数
```java
private void setCache(){
        File saveFile =  new File(getExternalFilesDir(null).toString()+File.separator+"videocache");
        if (!saveFile.exists()) {
            saveFile.mkdir();
        }
        VideoProxyCacheManager.Builder builder = new VideoProxyCacheManager.Builder().
                setFilePath(saveFile.getAbsolutePath()).    //缓存存储位置
                        setConnTimeOut(60 * 1000).                  //网络连接超时
                        setReadTimeOut(60 * 1000).                  //网络读超时
                        setExpireTime(2 * 24 * 60 * 60 * 1000).     //2天的过期时间
                        setMaxCacheSize(2L * 1024 * 1024 * 1024)
                .setIgnoreCert(true)
                .setUseOkHttp(true);    //2G的存储上限
        VideoProxyCacheManager.getInstance().initProxyConfig(builder.build());

    }
```
3.Activity中调用示例
```java
public class MainActivity extends AppCompatActivity {
    private AdvanceView mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StatusBarUtil.setTranslucentForImageView(this,0,null);
        initView();
        initData();
        //模拟刷新数据,自行放开
//         new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                data.clear();
//                Advance advance1= new Advance(toURLString("https://t7.baidu.com/it/u=1956604245,3662848045&fm=193&f=GIF").trim(), MediaType.IMAGE_TYPE,10);
//                data.add(advance1);
//                //https://t7.baidu.com/it/u=1956604245,3662848045&fm=193&f=GIF
//                Advance advance = new Advance(toURLString("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4").trim(),MediaType.VIDEO_TYPE,0);
//                data.add(advance);
//                mViewPager.setData(data);
//            }
//        },20000);
    }

    private final List<Advance> data = new ArrayList<>();
    private void initData(){
        data.clear();
        //https://t7.baidu.com/it/u=1956604245,3662848045&fm=193&f=GIF
        Advance advance = new Advance(toURLString("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4").trim(),MediaType.VIDEO_TYPE);
        data.add(advance);
        Advance advance1= new Advance(toURLString("https://t7.baidu.com/it/u=1956604245,3662848045&fm=193&f=GIF").trim(),MediaType.IMAGE_TYPE,10);
        data.add(advance1);
        Advance advance2 = new Advance("http://vjs.zencdn.net/v/oceans.mp4",MediaType.VIDEO_TYPE);
        data.add(advance2);
        Advance advance3 = new Advance(toURLString("https://t7.baidu.com/it/u=1415984692,3889465312&fm=193&f=GIF").trim(),MediaType.IMAGE_TYPE,5);
        data.add(advance3);
        //https://t7.baidu.com/it/u=2529476510,3041785782&fm=193&f=GIF
        Advance advance4 = new Advance(toURLString("https://t7.baidu.com/it/u=2529476510,3041785782&fm=193&f=GIF").trim(),MediaType.IMAGE_TYPE,8);
        data.add(advance4);
        mViewPager.setData(data);
    }
    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        mViewPager = findViewById(R.id.home_vp);
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

    }


    /**
     * @param str 解决中文路径
     * @return
     */
    public static String toURLString(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char charAt = str.charAt(i);
            if (charAt > 255) {
                try {
                    sb.append(URLEncoder.encode(String.valueOf(charAt), "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {
                sb.append(charAt);
            }
        }
        return sb.toString();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mViewPager.setResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        mViewPager.setPause();
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewPager.setDestroy();
    }
}
```
