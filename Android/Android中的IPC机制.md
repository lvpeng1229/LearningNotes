# 1、IPC简介
IPC是 Inter-Process Communication 的缩写，含义为**进程间通信**，是指两个进程之间进行数据交换的过程。

一般情况下，在 Android 系统中一个应用就只享有一个进程，在最简单的情况下一个进程可以只包含有一个线程（当然，一般情况下是不可能的），即主线程，也称为 UI 线程。

有时候应用因为某些原因需要采用多进程模式，此时如果要在应用内的不同进程间进行通信，就需要使用到 IPC 机制。或者是两个不同的应用需要进行数据交换，此时也一样需要依靠 Android 系统提供的 IPC 方案

IPC 不是 Android 所独有的，任何一个操作系统都有对应的IPC机制。Windows 上通过剪切板、管道、油槽等进行进程间通讯。Linux 上通过命名空间、共享内容、信号量等进行进程间通讯。

# 2、开启多进程

同一个应用，通过在 AndroidMenifest 中给**四大组件**指定 **android:process** 属性，就可以开启多进程模式。

进程名以 `:` 开头的属于当前应用的私有进程，其他应用的组件不可以和他跑在同一个进程里面。

而进程名**不以**`:`开头的进程属于全局进程，其他应用通过ShareUID方式可以和它跑在同一个进程中。两个应用可以通过ShareUID跑在同一个进程并且签名相同，他们可以共享data目录、组件信息、共享内存数据。

**以** `:` 开头

```java
<service
    android:name=".MyService"
    android:process=":remote" />
```

**不以**`:`开头

```java
<service
    android:name=".MyService"
    android:process="com.freeman.remote" />
```






# 3、多进程影响
1. **静态成员和单例模式完全失效。**

2. **线程同步机制完全失效。**

3. **SharedPreferences的可靠性下降**

4. **Application会多次创建**


- 问题1、2原因是因为进程不同，已经不是同一块内存了；

- 问题3是因为SharedPreferences不支持两个进程同事进行读写操作，有一定几率导致数据丢失；


- 问题4是当一个组件跑在一个新的进程中，系统会为他创建新的进程同时分配独立的虚拟机，所有这个过程其实就是启动一个应用的过程，因此相当于系统又把这个应用重新启动了一遍，Application 也是新建了。


为了解决多进程带来的问题，系统提供了很多的跨进程通信方式，比如：
1. **Intent**
2. **文件共享**
3. **SharedPreferences**
4. **Messenger**
5. **AIDL**
6. **ContentProvider**
7. **Socket**


# 4、IPC之序列化
跨进程通信的目的就是为了进行数据交换，但并不是所有的数据类型都能被传递，除了基本数据类型外，还必须是实现了序列化和反序列化的数据类型才可以，即实现了 **Serializable** 接口或 **Parcelable** 接口的数据类型

## 4.1、Serializable
**Serializable** 是Java提供的一个序列化接口（ 空接口） ，为对象提供标准的序列化和反序列化操作。只需要一个类去实现 Serializable 接口并声明一个 `serialVersionUID` 即可实现序列化。

此外，为了辅助系统完成对象的序列化和反序列化过程，还可以声明一个`long`型数据`serivalVersionUID`


```java
private static final long serivalVersionUID = 123456578689L;
```

序列化时系统会把对象的信息以及 serivalVersionUID 一起保存到某种介质中（例如文件或内存中），当反序列化时就会把介质中的 serivalVersionUID 与类中声明的 serivalVersionUID 进行对比，如果两者相同则说明序列化的类与当前类的版本是相同的，则可以序列化成功。如果两者不相等，则说明当前类的版本已经变化（可能是新增或删减了某个方法），则会导致序列化失败。

如果没有手动声明 serivalVersionUID ，编译工具则会根据当前类的结构自动去生成 serivalVersionUID ，这样在反序列化时只有类的结构完全保持一致才能反序列化成功。

为了当类的结构没有发生结构性变化时依然能够反序列化成功，一般是手动为 serivalVersionUID 指定一个固定的值。这样即使类增删了某个变量或方法体时，依然能够最大程度地恢复数据。当然，类的结构不能发生太大变化，否则依然会导致反序列化失败。

静态成员变量属于类不属于对象，所以不会参与序列化过程，用 `transient` 关键字标记的成员变量也不会参与序列化过程。

通过重写writeObject和readObject方法可以改变系统默认的序列化过程。

## 4.2、Parcelable
Parcelable 接口是 Android 中特有的序列化方式，效率相对Serializable更高，占用内存相对也更少，但使用起来稍微麻烦点。官方也推荐使用 Parcelable 进行序列化操作，Bundle 、 Intent 和 Bitmap 等都实现了 Parcelable 接口。

实现 Parcelable 接口需要实现四个方法，用于进行序列化、反序列化和内容描述。一般我们也不需要手动实现 Parcelable 接口，可以通过 Android Studio的一个插件：Android Parcelable code generator 来自动完成。

Parcelable的一个实现例子如下：

```java

import android.os.Parcel;
import android.os.Parcelable;

public class Person implements Parcelable {
    private int id;
    private String name;
    private float price;

    public Person() {
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // 写数据进行保存
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeFloat(this.price);
    }

    // 用来创建自定义的Parcelable的对象
    public static final Parcelable.Creator<Person> CREATOR = new Parcelable.Creator<Person>() {
        @Override
        public Person createFromParcel(Parcel source) {
            return new Person(source);
        }

        @Override
        public Person[] newArray(int size) {
            return new Person[size];
        }
    };

    // 读数据进行恢复
    protected Person(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.price = in.readFloat();
    }
}

```
实现一个Parcelable接口，需要实现以下几个方法：
1. 构造函数：从序列化后的对象中创建原始对象
2. describeContents：接口内容的描述，一般默认返回0即可
3. writeToParcel：序列化的方法，将类的数据写到parcel容器中
4. 静态的parcelable.Creator接口，这个接口包含两个方法
    - createFormParcel：反序列化的方法，将Parcel还原成Java对象
    - newArray：提供给外部类反序列化这个数组使用。

从上面我们可以看出 Parcel 的写入和读出顺序是一致的。如果元素是list读出时需要先 new 一个 ArrayList 传入，否则会报空指针异常。如下：


```java
list = new ArrayList<String>();
in.readStringList(list);
```



# 5、IPC方式
## 5.1、使用Intent
1. Activity，Service，BroadcastReceiver 都支持在 Intent 中传递 Bundle 数据，而 Bundle 实现了 Parcelable 接口，可以在不同的进程间进行传输。


2. 在一个进程中启动了另一个进程的 Activity，Service 和 BroadcastReceiver ，可以在 Bundle 中附加要传递的数据通过 Intent 发送出去。



## 5.2、使用文件共享
1. Windows 上，一个文件如果被加了排斥锁会导致其他线程无法对其进行访问，包括读和写；而 Android 系统基于 Linux ，使得其并发读取文件没有限制地进行，甚至允许两个线程同时对一个文件进行读写操作，尽管这样可能会出问题。
2. 可以在一个进程中序列化一个对象到文件系统中，在另一个进程中反序列化恢复这个对象（注意：并不是同一个对象，只是内容相同）。
3. SharedPreferences 是个特例，系统对它的读 / 写有一定的缓存策略，即内存中会有一份 ShardPreferences 文件的缓存，系统对他的读 / 写就变得不可靠，当面对高并发的读写访问，SharedPreferences 有很多大的几率丢失数据。因此，IPC 不建议采用 SharedPreferences。





## 5.3、使用 Messenger
Messenger 是一种轻量级的 IPC 方案，它的底层实现是 AIDL ，可以在不同进程中传递 Message 对象，它一次只处理一个请求，在服务端不需要考虑线程同步的问题，服务端不存在并发执行的情形。

- **服务端进程**：服务端创建一个 Service 来处理客户端请求，同时通过一个 Handler 对象来实例化一个 Messenger 对象，然后在 Service 的 onBind 中返回这个 Messenger 对象底层的 Binder 即可。

```java
public class MessengerService extends Service {

    private static final String TAG = MessengerService.class.getSimpleName();

    private class MessengerHandler extends Handler {

        /**
         * @param msg
         */
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case Constants.MSG_FROM_CLIENT:
                    Log.d(TAG, "receive msg from client: msg = [" + msg.getData().getString(Constants.MSG_KEY) + "]");
                    Toast.makeText(MessengerService.this, "receive msg from client: msg = [" + msg.getData().getString(Constants.MSG_KEY) + "]", Toast.LENGTH_SHORT).show();
                    Messenger client = msg.replyTo;
                    Message replyMsg = Message.obtain(null, Constants.MSG_FROM_SERVICE);
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.MSG_KEY, "我已经收到你的消息，稍后回复你！");
                    replyMsg.setData(bundle);
                    try {
                        client.send(replyMsg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private Messenger mMessenger = new Messenger(new MessengerHandler());


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }
}

```

- **客户端进程**：首先绑定服务端 Service ，绑定成功之后用服务端的 IBinder 对象创建一个 Messenger ，通过这个 Messenger 就可以向服务端发送消息了，消息类型是 Message 。如果需要服务端响应，则需要创建一个 Handler 并通过它来创建一个 Messenger（和服务端一样），并通过 Message 的 replyTo 参数传递给服务端。服务端通过 Message 的 replyTo 参数就可以回应客户端了。

```java
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private Messenger mGetReplyMessenger = new Messenger(new MessageHandler());
    private Messenger mService;

    private class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MSG_FROM_SERVICE:
                    Log.d(TAG, "received msg form service: msg = [" + msg.getData().getString(Constants.MSG_KEY) + "]");
                    Toast.makeText(MainActivity.this, "received msg form service: msg = [" + msg.getData().getString(Constants.MSG_KEY) + "]", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void bindService(View v) {
        Intent mIntent = new Intent(this, MessengerService.class);
        bindService(mIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void sendMessage(View v) {
        Message msg = Message.obtain(null,Constants.MSG_FROM_CLIENT);
        Bundle data = new Bundle();
        data.putString(Constants.MSG_KEY, "Hello! This is client.");
        msg.setData(data);
        msg.replyTo = mGetReplyMessenger;
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        unbindService(mServiceConnection);
        super.onDestroy();
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
            Message msg = Message.obtain(null,Constants.MSG_FROM_CLIENT);
            Bundle data = new Bundle();
            data.putString(Constants.MSG_KEY, "Hello! This is client.");
            msg.setData(data);
            msg.replyTo = mGetReplyMessenger;
            try {
                mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

 
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
}

```
**注意**：客户端和服务端是通过拿到对方的 Messenger 来发送 Message 的。只不过客户端通过 bindService onServiceConnected，而服务端通过 message.replyTo 来获得对方的 Messenger。

Messenger 中有一个 Hanlder 以串行的方式处理队列中的消息。不存在并发执行，因此我们不用考虑线程同步的问题。

## 5.4、使用 AIDL
### 5.4.1、什么是AIDL
- AIDL 意思即 Android Interface Definition Language，翻译过来就是Android接口定义语言，是用于定义服务端和客户端通信接口的一种描述语言，可以拿来生成用于 IPC 的代码。从某种意义上说 AIDL 其实是一个模板，因为在使用过程中，实际起作用的并不是 AIDL 文件，而是据此而生成的一个 IInterface 的实例代码，AIDL 其实是为了避免我们重复编写代码而出现的一个模板
- AIDL文件以 .aidl 为后缀名
- Messenger 是以串行的方式处理客户端发来的消息，如果大量消息同时发送到服务端，服务端只能一个一个处理，所以大量并发请求就不适合用 Messenger ，而且 Messenger 只适合传递消息，不能跨进程调用服务端的方法。**AIDL 可以解决并发和跨进程调用方法的问题**，要知道 Messenger 本质上也是 AIDL ，只不过系统做了封装方便上层的调用而已。

### 5.4.2、AIDL作用
- 用于不同进程中的通信，在 Android 系统中，每个进程都运行在一块独立的内存中，在其中完成自己的各项活动，与其他进程都分隔开来。可是有时候我们又有应用间进行互动的需求，比较传递数据或者任务委托等，AIDL 就是为了满足这种需求而诞生的。通过 AIDL，可以在一个进程中获取另一个进程的数据和调用其暴露出来的方法，从而满足进程间通信的需求。

- 通常，暴露方法给其他应用进行调用的应用称为服务端，调用其他应用的方法的应用称为客户端，客户端通过绑定服务端的Service来进行交互

### 5.4.3、AIDL 文件支持的数据类型
1. 基本数据类型；（short除外）
2. String 和 CharSequence ；
3. ArrayList ，里面的元素必须能够被 AIDL 支持；
4. HashMap ，里面的元素必须能够被 AIDL 支持；
5. Parcelable ，实现 Parcelable 接口的对象； 注意：如果 AIDL 文件中用到了自定义的 Parcelable 对象，必须新建一个和它同名的 AIDL 文件。
6. AIDL ，AIDL 接口本身也可以在 AIDL 文件中使用。

### 5.4.4、服务端和客户端

**服务端**

注意：服务端就是你要连接的进程。服务端创建一个 Service 用来监听客户端的连接请求，然后创建一个 AIDL 文件，将暴露给客户端的接口在这个 AIDL 文件中声明，最后在 Service 中实现这个 AIDL 接口即可。

**客户端**

绑定服务端的 Service ，绑定成功后，将服务端返回的 Binder 对象转成 AIDL 接口所属的类型，然后就可以调用 AIDL 中的方法了。客户端调用远程服务的方法，被调用的方法运行在服务端的 Binder 线程池中，同时客户端的线程会被挂起，如果服务端方法执行比较耗时，就会导致客户端线程长时间阻塞，导致 ANR 。客户端的 onServiceConnected 和 onServiceDisconnected 方法都在 UI 线程中。

### 5.4.5、设置aidl的权限，需要通过权限才能调用

- 使用 Permission 验证，在 manifest 中声明，加入权限验证功能

```java
<permission android:name="com.freeman.ipc.ACCESS_BOOK_SERVICE"
    android:protectionLevel="normal"/>
```

- 在服务端的 onBinder 方法中验证权限
```java
public IBinder onBind(Intent intent) {
    //Permission 权限验证
    int check = checkCallingOrSelfPermission("com.freeman.ipc.ACCESS_BOOK_SERVICE");
    if (check == PackageManager.PERMISSION_DENIED) {
        return null;
    }
    return mBinder;
}
```

### 5.4.6、AIDL不要做耗时操作
- 客户端在调用远程服务的方法时，被调用的方法是运行在服务端的 Binder 线程池中，同时客户端线程会被挂起，这时如果服务端方法执行比较耗时，就会导致客户端线程被堵塞。
- 比如使线程休眠了五秒，当点击按钮时就可以明显看到按钮有一种被“卡住了”的反馈效果，这就是因为 UI 线程被堵塞了，这可能会导致 ANR。所以如果确定远程方法是耗时的，就要避免在 UI 线程中去调用远程方法。
- 客户端的 **ServiceConnection** 对象的 `onServiceConnected` 和 `onServiceDisconnected`都是运行在 UI 线程中，所以也不能用于调用耗时的远程方法。
- 由于服务端的方法本身就运行在服务端的 Binder 线程池中，所以服务端方法本身就可以用于执行耗时方法，不必再在服务端方法中开线程去执行异步任务。

### 5.4.7、客户端在子线程中发起通信访问问题
当客户端发起远程请求时，客户端会挂起，一直等到服务端处理完并返回数据，所以远程通信是很耗时的，所以不能在子线程发起访问。由于服务端的Binder方法运行在Binder线程池中，所以应采取同步的方式去实现，因为它已经运行在一个线程中呢。

### 5.4.8、什么情况下会导致远程调用失败
Binder是会意外死亡的。如果服务端的进程由于某种原因异常终止，会导致远程调用失败，如果我们不知道Binder连接已经断裂， 那么客户端就会受到影响。不用担心，Android贴心的为我们提供了连个配对的方法linkToDeath和unlinkToDeath，通过linkToDeath我们可以给Binder设置一个死亡代理，当Binder死亡时，我们就会收到通知。

```java
// 在创建ServiceConnection的匿名类中的onServiceConnected方法中
// 设置死亡代理
messageCenter.asBinder().linkToDeath(deathRecipient, 0);


/**
 * 给binder设置死亡代理
 */
private IBinder.DeathRecipient deathRecipient = new IBinder.DeathRecipient() {

    @Override
    public void binderDied() {
        if(messageCenter == null){
            return;
        }
        messageCenter.asBinder().unlinkToDeath(deathRecipient, 0);
        messageCenter = null;
        //这里重新绑定服务
        attemptToBindService();
    }
};
```


### 5.4.9、**案例详细代码**：

```java
// Book.aidl
package com.freeman.ipc.aidl;

parcelable Book;
```


```java
// IBookManager.aidl
package com.freeman.ipc.aidl;

import com.freeman.ipc.aidl.Book;
import com.freeman.ipc.aidl.INewBookArrivedListener;

// AIDL 接口中只支持方法，不支持静态常量，区别于传统的接口
interface IBookManager {
    List<Book> getBookList();

    // AIDL 中除了基本数据类型，其他数据类型必须标上方向,in,out 或者 inout
    // in 表示输入型参数
    // out 表示输出型参数
    // inout 表示输入输出型参数

    void addBook(in Book book);

    void registerListener(INewBookArrivedListener listener);
    void unregisterListener(INewBookArrivedListener listener);

}
```


```java
// INewBookArrivedListener.aidl
package com.freeman.ipc.aidl;
import com.freeman.ipc.aidl.Book;

// 提醒客户端新书到来

interface INewBookArrivedListener {
    void onNewBookArrived(in Book newBook);
}
```


```java
public class BookManagerActivity extends AppCompatActivity {
    private static final String TAG = BookManagerActivity.class.getSimpleName();
    private static final int MSG_NEW_BOOK_ARRIVED = 0x10;
    private Button getBookListBtn,addBookBtn;
    private TextView displayTextView;
    private IBookManager bookManager;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_NEW_BOOK_ARRIVED:
                    Log.d(TAG, "handleMessage: new book arrived " + msg.obj);
                    Toast.makeText(BookManagerActivity.this, "new book arrived " + msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    super.handleMessage(msg);
            }

        }
    };

    private ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bookManager = IBookManager.Stub.asInterface(service);
            try {
                bookManager.registerListener(listener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private INewBookArrivedListener listener = new INewBookArrivedListener.Stub() {
        @Override
        public void onNewBookArrived(Book newBook) throws RemoteException {
            mHandler.obtainMessage(MSG_NEW_BOOK_ARRIVED, newBook).sendToTarget();

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_manager);
        displayTextView = (TextView) findViewById(R.id.displayTextView);
        Intent intent = new Intent(this, BookManagerService.class);
        bindService(intent, mServiceConn, BIND_AUTO_CREATE);

    }


    public void getBookList(View view) {
        try {
            List<Book> list = bookManager.getBookList();
            Log.d(TAG, "getBookList: " + list.toString());
            displayTextView.setText(list.toString());
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    public void addBook(View view) {
        try {
            bookManager.addBook(new Book(3, "天龙八部"));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        if (bookManager != null && bookManager.asBinder().isBinderAlive()) {
            Log.d(TAG, "unregister listener " + listener);
            try {
                bookManager.unregisterListener(listener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        unbindService(mServiceConn);
        super.onDestroy();
    }
}
```


```java
public class BookManagerService extends Service {
    private static final String TAG = BookManagerService.class.getSimpleName();

    // CopyOnWriteArrayList 支持并发读写，实现自动线程同步，他不是继承自 ArrayList
    private CopyOnWriteArrayList<Book> mBookList = new CopyOnWriteArrayList<Book>();
    //对象是不能跨进程传输的，对象的跨进程传输本质都是反序列化的过程，Binder 会把客户端传递过来的对象重新转化生成一个新的对象
    //RemoteCallbackList 是系统专门提供的用于删除系统跨进程 listener 的接口，利用底层的 Binder 对象是同一个
    //RemoteCallbackList 会在客户端进程终止后，自动溢出客户端注册的 listener ，内部自动实现了线程同步功能。
    private RemoteCallbackList<INewBookArrivedListener> mListeners = new RemoteCallbackList<>();
    private AtomicBoolean isServiceDestroied = new AtomicBoolean(false);


    private Binder mBinder = new IBookManager.Stub() {

        @Override
        public List<Book> getBookList() throws RemoteException {
            return mBookList;
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            Log.d(TAG, "addBook: " + book.toString());
            mBookList.add(book);

        }

        @Override
        public void registerListener(INewBookArrivedListener listener) throws RemoteException {
            mListeners.register(listener);
        }

        @Override
        public void unregisterListener(INewBookArrivedListener listener) throws RemoteException {
            mListeners.unregister(listener);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mBookList.add(new Book(1, "射雕英雄传"));
        mBookList.add(new Book(2, "倚天屠龙记"));
        new Thread(new ServiceWorker()).start();
    }

    private void onNewBookArrived(Book book) throws RemoteException {
        mBookList.add(book);

        int count = mListeners.beginBroadcast();

        for (int i = 0; i < count; i++) {
            INewBookArrivedListener listener = mListeners.getBroadcastItem(i);
            if (listener != null) {
                listener.onNewBookArrived(book);
            }
        }

        mListeners.finishBroadcast();

    }

    private class ServiceWorker implements Runnable {
        @Override
        public void run() {
            while (!isServiceDestroied.get()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int bookId = mBookList.size() +1;
                Book newBook = new Book(bookId, "new book # " + bookId);
                try {
                    onNewBookArrived(newBook);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // Permission 权限验证
        int check = checkCallingOrSelfPermission("com.freeman.ipc.ACCESS_BOOK_SERVICE");
        if (check == PackageManager.PERMISSION_DENIED) {
            return null;
        }

        return mBinder;
    }

    @Override
    public void onDestroy() {
        isServiceDestroied.set(true);
        super.onDestroy();
    }
}
```

## 5.5、使用 ContentProvider

- 用于不同应用间数据共享，和 Messenger 底层实现同样是 Binder 和 AIDL，系统做了封装，使用简单。
- 系统预置了许多 ContentProvider ，如通讯录、日程表，需要跨进程访问。使用方法：继承 ContentProvider 类实现 6 个抽象方法，这六个方法均运行在 ContentProvider 进程中，除 onCreate 运行在主线程里，其他五个方法均由外界回调运行在 Binder 线程池中。ContentProvider 的底层数据，可以是 SQLite 数据库，可以是文件，也可以是内存中的数据。
- 详见代码：

```java
public class BookProvider extends ContentProvider {
    private static final String TAG = "BookProvider";
    public static final String AUTHORITY = "com.freeman.ipc.Book.Provider";

    public static final Uri BOOK_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/book");
    public static final Uri USER_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/user");

    public static final int BOOK_URI_CODE = 0;
    public static final int USER_URI_CODE = 1;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(AUTHORITY, "book", BOOK_URI_CODE);
        sUriMatcher.addURI(AUTHORITY, "user", USER_URI_CODE);
    }

    private Context mContext;
    private SQLiteDatabase mDB;

    @Override
    public boolean onCreate() {
        mContext = getContext();
        initProviderData();

        return true;
    }

    private void initProviderData() {
        //不建议在 UI 线程中执行耗时操作
        mDB = new DBOpenHelper(mContext).getWritableDatabase();
        mDB.execSQL("delete from " + DBOpenHelper.BOOK_TABLE_NAME);
        mDB.execSQL("delete from " + DBOpenHelper.USER_TABLE_NAME);
        mDB.execSQL("insert into book values(3,'Android');");
        mDB.execSQL("insert into book values(4,'iOS');");
        mDB.execSQL("insert into book values(5,'Kotlin');");
        mDB.execSQL("insert into user values(1,'Flutter',1);");
        mDB.execSQL("insert into user values(2,'Linux',0);");

    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.d(TAG, "query, current thread"+ Thread.currentThread());
        String table = getTableName(uri);
        if (table == null) {
            throw new IllegalArgumentException("Unsupported URI" + uri);
        }

        return mDB.query(table, projection, selection, selectionArgs, null, null, sortOrder, null);
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        Log.d(TAG, "getType");
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(TAG, "insert");
        String table = getTableName(uri);
        if (table == null) {
            throw new IllegalArgumentException("Unsupported URI" + uri);
        }
        mDB.insert(table, null, values);
        // 通知外界 ContentProvider 中的数据发生变化
        mContext.getContentResolver().notifyChange(uri, null);
        return uri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, "delete");
        String table = getTableName(uri);
        if (table == null) {
            throw new IllegalArgumentException("Unsupported URI" + uri);
        }
        int count = mDB.delete(table, selection, selectionArgs);
        if (count > 0) {
            mContext.getContentResolver().notifyChange(uri, null);
        }

        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.d(TAG, "update");
        String table = getTableName(uri);
        if (table == null) {
            throw new IllegalArgumentException("Unsupported URI" + uri);
        }
        int row = mDB.update(table, values, selection, selectionArgs);
        if (row > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return row;
    }

    private String getTableName(Uri uri) {
        String tableName = null;
        switch (sUriMatcher.match(uri)) {
            case BOOK_URI_CODE:
                tableName = DBOpenHelper.BOOK_TABLE_NAME;
                break;
            case USER_URI_CODE:
                tableName = DBOpenHelper.USER_TABLE_NAME;
                break;
            default:
                break;
        }

        return tableName;

    }
}
```


```java
public class DBOpenHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "book_provider.db";
    public static final String BOOK_TABLE_NAME = "book";
    public static final String USER_TABLE_NAME = "user";

    private static final int DB_VERSION = 1;

    private String CREATE_BOOK_TABLE = "CREATE TABLE IF NOT EXISTS "
            + BOOK_TABLE_NAME + "(_id INTEGER PRIMARY KEY," + "name TEXT)";

    private String CREATE_USER_TABLE = "CREATE TABLE IF NOT EXISTS "
            + USER_TABLE_NAME + "(_id INTEGER PRIMARY KEY," + "name TEXT,"
            + "sex INT)";



    public DBOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_BOOK_TABLE);
        db.execSQL(CREATE_USER_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
```


```java
public class ProviderActivity extends AppCompatActivity {
    private static final String TAG = ProviderActivity.class.getSimpleName();
    private TextView displayTextView;
    private Handler mHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider);
        displayTextView = (TextView) findViewById(R.id.displayTextView);
        mHandler = new Handler();

        getContentResolver().registerContentObserver(BookProvider.BOOK_CONTENT_URI, true, new ContentObserver(mHandler) {
            @Override
            public boolean deliverSelfNotifications() {
                return super.deliverSelfNotifications();
            }

            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
            }

            @Override
            public void onChange(boolean selfChange, Uri uri) {
                Toast.makeText(ProviderActivity.this, uri.toString(), Toast.LENGTH_SHORT).show();
                super.onChange(selfChange, uri);
            }
        });
    }

    public void insert(View v) {
        ContentValues values = new ContentValues();
        values.put("_id",1123);
        values.put("name", "三国演义");
        getContentResolver().insert(BookProvider.BOOK_CONTENT_URI, values);

    }
    public void delete(View v) {
        getContentResolver().delete(BookProvider.BOOK_CONTENT_URI, "_id = 4", null);


    }
    public void update(View v) {
        ContentValues values = new ContentValues();
        values.put("_id",1123);
        values.put("name", "三国演义新版");
        getContentResolver().update(BookProvider.BOOK_CONTENT_URI, values , "_id = 1123", null);


    }
    public void query(View v) {
        Cursor bookCursor = getContentResolver().query(BookProvider.BOOK_CONTENT_URI, new String[]{"_id", "name"}, null, null, null);
        StringBuilder sb = new StringBuilder();
        while (bookCursor.moveToNext()) {
            Book book = new Book(bookCursor.getInt(0),bookCursor.getString(1));
            sb.append(book.toString()).append("\n");
        }
        sb.append("--------------------------------").append("\n");
        bookCursor.close();

        Cursor userCursor = getContentResolver().query(BookProvider.USER_CONTENT_URI, new String[]{"_id", "name", "sex"}, null, null, null);
        while (userCursor.moveToNext()) {
            sb.append(userCursor.getInt(0))
                    .append(userCursor.getString(1)).append(" ,")
                    .append(userCursor.getInt(2)).append(" ,")
                    .append("\n");
        }
        sb.append("--------------------------------");
        userCursor.close();
        displayTextView.setText(sb.toString());
    }
}
```

## 5.6、使用 Socket
- Socket起源于 Unix，而 Unix 基本哲学之一就是“一切皆文件”，都可以用“打开 open –读写 write/read –关闭 close ”模式来操作。Socket 就是该模式的一个实现，网络的 Socket 数据传输是一种特殊的 I/O，Socket 也是一种文件描述符。Socket 也具有一个类似于打开文件的函数调用： Socket()，该函数返回一个整型的Socket 描述符，随后的连接建立、数据传输等操作都是通过该 Socket 实现的。

- 常用的 Socket 类型有两种：流式 Socket（SOCK_STREAM）和数据报式 Socket（SOCK_DGRAM）。流式是一种面向连接的 Socket，针对于面向连接的 TCP 服务应用；数据报式 Socket 是一种无连接的 Socket ，对应于无连接的 UDP 服务应用。

Socket 本身可以传输任意字节流。
谈到Socket，就必须要说一说 TCP/IP 五层网络模型：

- 应用层：规定应用程序的数据格式，主要的协议 HTTP，FTP，WebSocket，POP3 等；
- 传输层：建立“端口到端口” 的通信，主要的协议：TCP，UDP；
- 网络层：建立”主机到主机”的通信，主要的协议：IP，ARP ，IP 协议的主要作用：一个是为每一台计算机分配 IP 地址，另一个是确定哪些地址在同一子网；
- 数据链路层：确定电信号的分组方式，主要的协议：以太网协议；
- 物理层：负责电信号的传输。

**Socket 是连接应用层与传输层之间接口（API）。**

### 5.6.1、Client 端代码：

```java
public class TCPClientActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "TCPClientActivity";
    public static final int MSG_RECEIVED = 0x10;
    public static final int MSG_READY = 0x11;
    private EditText editText;
    private TextView textView;
    private PrintWriter mPrintWriter;
    private Socket mClientSocket;
    private Button sendBtn;
    private StringBuilder stringBuilder;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_READY:
                    sendBtn.setEnabled(true);
                    break;
                case MSG_RECEIVED:
                    stringBuilder.append(msg.obj).append("\n");
                    textView.setText(stringBuilder.toString());
                    break;
                default:
                    super.handleMessage(msg);
            }

    }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tcp_client_activity);
        editText = (EditText) findViewById(R.id.editText);
        textView = (TextView) findViewById(R.id.displayTextView);
        sendBtn = (Button) findViewById(R.id.sendBtn);
        sendBtn.setOnClickListener(this);
        sendBtn.setEnabled(false);
        stringBuilder = new StringBuilder();

        Intent intent = new Intent(TCPClientActivity.this, TCPServerService.class);
        startService(intent);

        new Thread(){
            @Override
            public void run() {
                connectTcpServer();
            }
        }.start();
    }


    private String formatDateTime(long time) {
        return new SimpleDateFormat("(HH:mm:ss)").format(new Date(time));
    }

    private void connectTcpServer() {
        Socket socket = null;
        while (socket == null) {
            try {
                socket = new Socket("localhost", 8888);
                mClientSocket = socket;
                mPrintWriter = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())
                ), true);
                mHandler.sendEmptyMessage(MSG_READY);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // receive message
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (!isFinishing()) {
            try {
                String msg = bufferedReader.readLine();
                if (msg != null) {
                    String time = formatDateTime(System.currentTimeMillis());
                    String showedMsg = "server " + time + ":" + msg
                            + "\n";
                    mHandler.obtainMessage(MSG_RECEIVED, showedMsg).sendToTarget();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onClick(View v) {
        if (mPrintWriter != null) {
            String msg = editText.getText().toString();
            mPrintWriter.println(msg);
            editText.setText("");
            String time = formatDateTime(System.currentTimeMillis());
            String showedMsg = "self " + time + ":" + msg + "\n";
            stringBuilder.append(showedMsg);

        }

    }

    @Override
    protected void onDestroy() {
        if (mClientSocket != null) {
            try {
                mClientSocket.shutdownInput();
                mClientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }
}
```

### 5.6.2、Server 端代码

```java
public class TCPServerService extends Service {
    private static final String TAG = "TCPServerService";
    private boolean isServiceDestroyed = false;
    private String[] mMessages = new String[]{
            "Hello! Body!",
            "用户不在线！请稍后再联系！",
            "请问你叫什么名字呀？",
            "厉害了，我的哥！",
            "Google 不需要科学上网是真的吗？",
            "扎心了，老铁！！！"
    };


    @Override
    public void onCreate() {
        new Thread(new TCPServer()).start();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        isServiceDestroyed = true;
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class TCPServer implements Runnable {

        @Override
        public void run() {
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(8888);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            while (!isServiceDestroyed) {
                // receive request from client
                try {
                    final Socket client = serverSocket.accept();
                    Log.d(TAG, "=============== accept ==================");
                    new Thread(){
                        @Override
                        public void run() {
                            try {
                                responseClient(client);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }


    private void responseClient(Socket client) throws IOException {
        //receive message
        BufferedReader in = new BufferedReader(
                new InputStreamReader(client.getInputStream()));
        //send message
        PrintWriter out = new PrintWriter(
                new BufferedWriter(
                        new OutputStreamWriter(
                                client.getOutputStream())),true);
        out.println("欢迎来到聊天室！");

        while (!isServiceDestroyed) {
            String str = in.readLine();
            Log.d(TAG, "message from client: " + str);
            if (str == null) {
                return;
            }
            Random random = new Random();
            int index = random.nextInt(mMessages.length);
            String msg = mMessages[index];
            out.println(msg);
            Log.d(TAG, "send Message: " + msg);
        }
        out.close();
        in.close();
        client.close();

    }
}
```

# 5.7、选用合适的IPC方式

![](https://user-gold-cdn.xitu.io/2019/8/28/16cd785711e0cc69?w=1716&h=734&f=png&s=92937)

