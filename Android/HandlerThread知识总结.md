# 1、HandlerThread介绍
## 1.1、HandlerThread概述
一个继承自Thread的类，本质上就是一个Thread。与普通Thread的差别在于：它内部直接实现了Looper的封装，这是Handler消息机制必不可少的，有了自己的Looper，可以让我们在自己的线程中分发和处理消息，如果不用HandlerThread的话，需要手动去调用Looper.prepare()和Looper.loop()这些方法。
## 1.2、HandlerThread使用
1. 创建HandlerThread的实例对象，该参数表示线程的名字，可以随便选择。
```java
HandlerThread handlerThread = new HandlerThread("myHandlerThread");
```
2. 启动我们创建的HandlerThread线程
```java
handlerThread.start();
```
3. 将我们的handlerThread与Handler绑定在一起。 
```java
mThreadHandler = new Handler(mHandlerThread.getLooper()) {
    @Override
    public void handleMessage(Message msg) {
        checkForUpdate();
        if(isUpdate){
            mThreadHandler.sendEmptyMessage(MSG_UPDATE_INFO);
        }
    }
};
```
完整测试代码如下
```java
public class MainActivity extends AppCompatActivity {
    private static final int MSG_UPDATE_INFO = 0x100;
    Handler mMainHandler = new Handler();
    private TextView mTv;
    private Handler mThreadHandler;
    private HandlerThread mHandlerThread;
    private boolean isUpdate = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTv = (TextView) findViewById(R.id.tv);
        initHandlerThread();
    }

    private void initHandlerThread() {
        mHandlerThread = new HandlerThread("handlerThread");
        mHandlerThread.start();
        mThreadHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                checkForUpdate();
                if (isUpdate) {
                    mThreadHandler.sendEmptyMessage(MSG_UPDATE_INFO);
                }
            }
        };
    }

    /**
     * 模拟从服务器解析数据
     */
    private void checkForUpdate() {
        try {
            //模拟耗时
            Thread.sleep(1200);
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    String result = "实时更新中，当前股票行情：<font color='red'>%d</font>";
                    result = String.format(result, (int) (Math.random() * 5000 + 1000));
                    mTv.setText(Html.fromHtml(result));
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        isUpdate = true;
        super.onResume();
        mThreadHandler.sendEmptyMessage(MSG_UPDATE_INFO);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isUpdate = false;
        mThreadHandler.removeMessages(MSG_UPDATE_INFO);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandlerThread.quit();
        mMainHandler.removeCallbacksAndMessages(null);
    }
}
```
![效果图](https://user-gold-cdn.xitu.io/2019/8/23/16cbdf0b5fb21d12?w=387&h=273&f=gif&s=14676)


## 1.3、HandlerThread特性
- HandlerThread 继承于 Thread，本身就是一个线程类
- HandlerThread 在内部维护了自己的 Looper 对象，所以可以进行 looper 循环
- 创建 HandlerThread 后需要先调用 HandlerThread.start() 方法再向其下发任务，通过 run() 方法来创建 Looper 对象
- 通过传递 HandlerThread 的 Looper 对象给 Handler 对象，从而可以通过 Handler 来向 HandlerThread 下发耗时任务。
- 通过HandlerThread我们不但可以实现UI线程与子线程的通信同样也可以实现子线程与子线程之间的通信；
- HandlerThread在不需要使用的时候需要手动的回收掉；

## 1.4、创建对象接受消息
- Handler的构造方法中传入了HandlerThread的Looper对象，所以Handler对象就相当于含有了HandlerThread线程中Looper对象的引用。

- 然后调用handler的sendMessage方法发送消息，在Handler的handleMessge方法中就可以接收到消息了。
```java
// 创建的Handler将会在mHandlerThread线程中执行
final Handler mHandler = new Handler(mHandlerThread.getLooper()) {
    @Override
    public void handleMessage(Message msg) {
        Log.i("tag", "接收到消息：" + msg.obj.toString());
    }
};
```
- 需要注意的是在不需要这个looper线程的时候需要手动停止掉；
```java
protected void onDestroy() {
    super.onDestroy();
    mHandlerThread.quit();
}
```



# 2、HandlerThread源码分析
## 2.1、HandlerThread构造方法
有两个构造方法，一个参数的和两个参数的，name代表当前线程的名称，priority为线程的优先级别
```java
public HandlerThread(String name) {
    super(name);
    mPriority = Process.THREAD_PRIORITY_DEFAULT;
}

public HandlerThread(String name, int priority) {
    super(name);
    mPriority = priority;
}
```

## 2.2、HandlerThread调用start方法
知道了HandlerThread类其实就是一个Thread，所以其start方法内部调用的肯定是Thread的run方法，发现其内部调用了Looper.prepate()方法和Loop.loop()方法。

通过run方法，可以知道在创建的HandlerThread线程中同时创建了该线程的Looper与MessageQueue。

需要注意的是其在调用Looper.loop()方法之前调用了一个空的实现方法：```onLooperPrepared()```,我们可以实现自己的```onLooperPrepared()```方法，做一些Looper的初始化操作。


```java
public void run() {
    mTid = Process.myTid();
    Looper.prepare();
    //持有锁机制来获得当前线程的Looper对象
    synchronized (this) {
        mLooper = Looper.myLooper();
        //发出通知，当前线程已经创建mLooper对象成功，这里主要是通知getLooper方法中的wait
        notifyAll();
    }
    //设置线程的优先级别
    Process.setThreadPriority(mPriority);
    //这里默认是空方法的实现，我们可以重写这个方法来做一些线程开始之前的准备，方便扩展
    onLooperPrepared();
    Looper.loop();
    mTid = -1;
}
```

## 2.3、quit方法和quitSafe方法
```java
//调用这个方法退出Looper消息循环，及退出线程
public boolean quit() {
    Looper looper = getLooper();
    if (looper != null) {
        looper.quit();
        return true;
    }
    return false;
}
//调用这个方法安全地退出线程
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public boolean quitSafely() {
    Looper looper = getLooper();
    if (looper != null) {
        looper.quitSafely();
        return true;
    }
    return false;
}
```
跟踪这两个方法容易知道只两个方法最终都会调用**MessageQueue**的```quit(boolean safe)```方法

```java
void quit(boolean safe) {
    if (!mQuitAllowed) {
        throw new IllegalStateException("Main thread not allowed to quit.");
    }
    synchronized (this) {
        if (mQuitting) {
            return;
        }
        mQuitting = true;
        //安全退出调用这个方法
        if (safe) {
            removeAllFutureMessagesLocked();
        } else {//不安全退出调用这个方法
            removeAllMessagesLocked();
        }
        // We can assume mPtr != 0 because mQuitting was previously false.
        nativeWake(mPtr);
    }
}
```


不安全的会调用removeAllMessagesLocked();这个方法，我们来看这个方法是怎样处理的，其实就是遍历Message链表，移除所有信息的回调，并重置为null。
```java
private void removeAllMessagesLocked() {
    Message p = mMessages;
    while (p != null) {
        Message n = p.next;
        p.recycleUnchecked();
        p = n;
    }
    mMessages = null;
}
```


安全地会调用```removeAllFutureMessagesLocked();```这个方法，它会根据**Message.when**这个属性，判断我们当前消息队列是否正在处理消息，没有正在处理消息的话，直接移除所有回调，正在处理的话，等待该消息处理处理完毕再退出该循环。因此说**quitSafe()是安全的**，而**quit()方法是不安全的**，因为quit方法不管是否正在处理消息，直接移除所有回调。
```java
private void removeAllFutureMessagesLocked() {
    final long now = SystemClock.uptimeMillis();
    Message p = mMessages;
    if (p != null) {
        //判断当前队列中的消息是否正在处理这个消息，没有的话，直接移除所有回调
        if (p.when > now) {
            removeAllMessagesLocked();
        } else {//正在处理的话，等待该消息处理处理完毕再退出该循环
            Message n;
            for (;;) {
                n = p.next;
                if (n == null) {
                    return;
                }
                if (n.when > now) {
                    break;
                }
                p = n;
            }
            p.next = null;
            do {
                p = n;
                n = p.next;
                p.recycleUnchecked();
            } while (n != null);
        }
    }
}
```