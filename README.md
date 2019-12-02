# NetworkMonitor
网络状态监听

How to use?
--------
* 1.Application中初始化<br>
* 2.适应的位置调用NetStateMonitor.getInstance().register(this)<br>
* 3.方法上添加@NetMonitor注解,方法必须为public且有一个类型为NetState的参数<br>
* 4.配置相关权限

```
public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @NetMonitor(stateValue = {NetState.GPRS})
    public void showNetGPRS(NetState netState)
    {
        Log.e(TAG, "showNetGPRS: ");
    }

    @NetMonitor(stateValue = {NetState.WIFI})
    public void showNetWIFI(NetState netState)
    {
        Log.e(TAG, "showNetWIFI: ");
    }

    @NetMonitor(stateValue = {NetState.NONE})
    public void showNetNONE(NetState netState)
    {
        Log.e(TAG, "showNetNONE: ");
    }

    @NetMonitor
    public void showNet(NetState netState)
    {
        Log.e(TAG, "showNet: ");
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        NetStateMonitor.getInstance().register(this);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        NetStateMonitor.getInstance().unregister(this);
    }
}

```



Download
--------

```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
dependencies {
	implementation 'com.github.Wudelin:NetworkMonitor:1.0.0'
}
```


