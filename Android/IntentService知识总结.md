# 1、IntentService概述
IntentService是一种特殊的Service，它继承了Service并且它是一个抽象类，因此必须创建它的子类才能使用IntentService。

客户端startService发送请求，IntentService就被启动，然后会在一个工作线程中处理传递过来的Intent，当任务结束后就会自动停止服务。

IntentService是自己维护了一个线程，来执行耗时的操作，然后里面封装了HandlerThread，能够方便在子线程创建Handler。

# 2、IntentService特性
- IntentService 内部创建了一个工作线程，用于在子线程内执行传递给 onStartCommand() 的所有 Intent，开发者无须关心多线程问题
- IntentService 内部通过 HandlerThread 和 Handler 来实现异步操作
- IntentService 是以串行方式处理外部传递来的任务，即只有当上一个任务完成时，新的任务才会被执行
- 在处理完所有任务请求后会自动停止，因此不必手动调用 stopSelf() 方法
- 提供了 onBind() 的默认实现（返回 null）
- IntentService 是四大组件之一，拥有较高的优先级，不易被系统杀死，因此适合于执行一些高优先级的异步任务



# 3、IntentService使用场景
- IntentService不需要我们自己去关闭Service，它自己会在任务完成之后自行关闭，不过每次只能处理一个任务，所以不适用于高并发，适用于请求数较少的情况。
- 例如播放音乐、下载文件等，但 Service 默认是运行于 UI 线程的，如果想要依靠其来完成一些耗时任务，就需要自己来建立子线程，这相对比较繁琐，所以官方也为开发者提供了 IntentService 来解决这一问题
1. 类似于APP的版本检测更新，后台定位功能以及读取少量的IO操作。博客
2. 线程任务需按顺序、在后台执行，比如阿里云推送的服务service就是继承IntentSerVice
3. 将部分application初始化的逻辑放到intentService里面处理，可以提高application启动时间



# 4、IntentService使用案例
## 4.1 实现步骤
- 步骤1：定义IntentService的子类：传入线程名称、复写onHandleIntent()方法
- 步骤2：在Manifest.xml中注册服务
- 步骤3：在Activity中开启Service服务

## 4.2 具体实例
- 步骤1：定义IntentService的子类：传入线程名称、复写onHandleIntent()方法


```java
public class MyIntentService extends IntentService {

    /*构造函数*/
    public MyIntentService() {
        //调用父类的构造函数
        //构造函数参数=工作线程的名字
        super("MyIntentService");

    }

    /*复写onHandleIntent()方法*/
    //实现耗时任务的操作
    @Override
    protected void onHandleIntent(Intent intent) {
        //根据Intent的不同进行不同的事务处理
        String taskName = intent.getExtras().getString("taskName");
        switch (taskName) {
            case "task1":
                Log.i("MyIntentService", "do task1");
                break;
            case "task2":
                Log.i("MyIntentService", "do task2");
                break;
            default:
                break;
        }
    }


    @Override
    public void onCreate() {
        Log.i("MyIntentService", "onCreate");
        super.onCreate();
    }

    /*复写onStartCommand()方法*/
    //默认实现将请求的Intent添加到工作队列里
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("MyIntentService", "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.i("MyIntentService", "onDestroy");
        super.onDestroy();
    }
}
```
- 步骤2：在Manifest.xml中注册服务
```java
<service android:name=".MyIntentService">
	<intent-filter>
		<action android:name="com.freeman"/>
	</intent-filter>
</service>
```

- 步骤3：在Activity中开启Service服务
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

        //同一服务只会开启一个工作线程
        //在onHandleIntent函数里依次处理intent请求。

        Intent i = new Intent("com.freeman.service");
        Bundle bundle = new Bundle();
        bundle.putString("taskName", "task1");
        i.putExtras(bundle);
        startService(i);

        Intent i2 = new Intent("com.freeman.service");
        Bundle bundle2 = new Bundle();
        bundle2.putString("taskName", "task2");
        i2.putExtras(bundle2);
        startService(i2);

        startService(i);  //多次启动
    }
}
```


# 5、IntentService源码分析
IntentService实际上内部实例化了一个HandlerThread,并且封装了一个Handler，分析如下：
- 创建一个HandlerThread,开启HandlerThread来创建Looper
- 创建一个Handler,传入Looper，从而在子线程实例化Handler
- 在onStartCommand中获取到的Intent作为消息的obj发送出去
- 然后在onHandleIntent中处理这个消息，注意此时是在子线程
- 跟HandlerThread一样,IntentService内部是采用Handler来实现的，所以任务是串行执行的，不适用于大量耗时操作。

源码如下所示：

```java
public abstract class IntentService extends Service {

    //子线程中的Looper
    private volatile Looper mServiceLooper;
    //内部持有的一个mServiceHandler对象
    private volatile ServiceHandler mServiceHandler;
    //内部创建的线程名字
    private String mName;
    //服务被异常终止后重新创建调用onStartCommand是否回传Intent
    private boolean mRedelivery;

    /**
     * 内部创建了一个ServiceHandler，然后将传递过来的Intent封装成一个Message，
     * 然后再将Message封装成一个Intent，回调onHandleIntent，其实转换的目的就是
     * 将主线程的Intent切换到子线程中去执行了而已。
     */
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            //处理发送过来的消息,在子线程
            onHandleIntent((Intent)msg.obj);
            //处理完消息之后停止Service
            stopSelf(msg.arg1);
        }
    }

    /**
     * 工作线程的名字
     * @param name
     */
    public IntentService(String name) {
        super();
        mName = name;
    }

    public void setIntentRedelivery(boolean enabled) {
        mRedelivery = enabled;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //创建HandlerThread
        HandlerThread thread = new HandlerThread("IntentService[" + mName + "]");
        //开启线程创建子线程Looper
        thread.start();
        //获取子线程Looper
        mServiceLooper = thread.getLooper();
        //创建子线程Handler
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        //创建一个Message
        Message msg = mServiceHandler.obtainMessage();
        //消息标志，作为当前Service的标志
        msg.arg1 = startId;
        //携带Intent
        msg.obj = intent;
        //发送消息，此时将线程切换到子线程
        mServiceHandler.sendMessage(msg);
    }


    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        //调用onStart方法
        onStart(intent, startId);
        //根据mRedelivery的值来确定返回重传Intent的黏性广播还是非黏性广播
        return mRedelivery ? START_REDELIVER_INTENT : START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        //退出Looper
        mServiceLooper.quit();
    }


    @Override
    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*子类必须实现的抽象方法*/
    @WorkerThread
    protected abstract void onHandleIntent(@Nullable Intent intent);
}
```

从上面源码可以看出，IntentService本质是采用Handler & HandlerThread方式：

1. 通过HandlerThread单独开启一个名为IntentService的线程
2. 创建一个名叫ServiceHandler的内部Handler
3. 把内部Handler与HandlerThread所对应的子线程进行绑定
4. 通过onStartCommand()传递给服务intent，依次插入到工作队列中，并逐个发送给onHandleIntent()
5. 通过onHandleIntent()来依次处理所有Intent请求对象所对应的任务

因此我们通过复写方法onHandleIntent()，再在里面根据Intent的不同进行不同的线程操作就可以了

# 6、注意事项：
**工作任务队列是顺序执行的**
> 如果一个任务正在IntentService中执行，此时你再发送一个新的任务请求，这个新的任务会一直等待直到前面一个任务执行完毕才开始执行。

**原因：**

1. 由于onCreate() 方法只会调用一次，所以只会创建一个工作线程；
2. 当多次调用 startService(Intent) 时（onStartCommand也会调用多次）其实并不会创建新的工作线程，只是把消息加入消息队列中等待执行，所以，多次启动 IntentService 会按顺序执行事件；
3. 如果服务停止，会清除消息队列中的消息，后续的事件得不到执行。