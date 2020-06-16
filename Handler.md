## 1、消息机制Hander作用？有哪些要素？流程是怎样的？
- **作用：**
跨线程通信。当子线程中进行耗时操作后需要更新UI时，通过Handler将有关UI的操作切换到主线程中执行。

- **四要素：**
     1. Message（消息）：需要被传递的消息，其中包含了消息ID，消息处理对象以及处理的数据等，由MessageQueue统一列队，最终由Handler处理。
     2. MessageQueue（消息队列）：用来存放Handler发送过来的消息，内部通过单链表的数据结构来维护消息列表，等待Looper的抽取。
    3. Handler（处理者）：负责Message的发送及处理。通过 Handler.sendMessage() 向消息池发送各种消息事件；通过 Handler.handleMessage() 处理相应的消息事件。
    4. Looper（消息泵）：通过Looper.loop()不断地从MessageQueue中抽取Message，按分发机制将消息分发给目标处理者。
- **具体流程**
    1. Handler.sendMessage()发送消息时，会通过MessageQueue.enqueueMessage()向MessageQueue中添加一条消息；
    2. 通过Looper.loop()开启循环后，不断轮询调用MessageQueue.next()；
    3. 调用目标Handler.dispatchMessage()去传递消息，目标Handler收到消息后调用Handler.handlerMessage()处理消息。

## 2、为什么一个线程只有一个Looper和一个MessageQueue，可以有多个Handler？
在创建Looper时需使用Looper的prepare方法，Looper.prepare()。
Android中一个线程最多仅仅能有一个Looper，若在已有Looper的线程中调用Looper.prepare()会抛出异常，所以一个线程只有一个Looper。
```java
public static void prepare() {
    prepare(true);
}

private static void prepare(boolean quitAllowed) {
    if (sThreadLocal.get() != null) {
        throw new RuntimeException("Only one Looper may be created per thread");
    }
    sThreadLocal.set(new Looper(quitAllowed));
}

```
Looper有一个MessageQueue，在Looper的构造方法时创建MessageQueue，可以处理来自多个Handler的Message；MessageQueue有一组待处理的Message，这些Message可来自不同的Handler；Message中记录了负责发送和处理消息的Handler；Handler中有Looper和MessageQueue。
```java
private Looper(boolean quitAllowed) {
    mQueue = new MessageQueue(quitAllowed);
    mThread = Thread.currentThread();
}
```
## 3、可以在子线程直接new一个Handler吗？会出现什么问题，那该怎么做？
不能在子线程直接new一个Handler。因为Handler的工作是依赖于Looper的，而Looper（与消息队列）又是属于某一个线程，其他线程不能访问。因此要使用Handler必须要保证Handler所创建的线程中有Looper对象并且启动循环。因为子线程中默认是没有Looper的，所以会报错。
正确的使用方法是：
```java
handler = null;
new Thread(new Runnable() {
   private Looper mLooper;
   @Override
   public void run() {
       Looper.prepare();
       handler = new Handler();
       //获取Looper对象
       mLooper = Looper.myLooper();
       //启动消息循环
       Looper.loop();
       //在适当的时候退出Looper的消息循环，防止内存泄漏
       mLooper.quit();
   }
}).start();

```
## 4、Looper.prepare()能否调用两次或者多次，会出现什么情况？
如果运行，则会报错，并提示prepare中的Excetion信息。由此可以得出在每个线程中Looper.prepare()能且只能调用一次
```java
public static void prepare() {
    prepare(true);
}

private static void prepare(boolean quitAllowed) {
    if (sThreadLocal.get() != null) {
        throw new RuntimeException("Only one Looper may be created per thread");
    }
    sThreadLocal.set(new Looper(quitAllowed));
}

```
## 5、为什么系统不建议在子线程访问UI，不对UI控件的访问加上锁机制的原因？
系统不建议在子线程访问UI的原因是：UI控件非线程安全，在多线程中并发访问可能会导致UI控件处于不可预期的状态。
不对UI控件的访问加上锁机制的原因是：上锁会让UI控件变得复杂和低效，上锁后会阻塞某些进程的执行。


## 6、如何获取当前线程的Looper？是怎么实现的？
调用 Looper.myLooper() 方法就可以获取。内部就是通过ThreadLoacl的get()方法获取Looper实例。
```java
public static @Nullable Looper myLooper() {
        return sThreadLocal.get();
}
```

## 7、Looper.loop是一个死循环，拿不到需要处理的Message就会阻塞，那在UI线程中为什么不会导致ANR？
[https://www.zhihu.com/question/34652589](https://www.zhihu.com/question/34652589)

[https://mp.weixin.qq.com/s/6nmpkl-Ots9rQZnQKXvjiA](https://mp.weixin.qq.com/s/6nmpkl-Ots9rQZnQKXvjiA)




## 8、Handler.sendMessageDelayed()怎么实现延迟的？
以handler.postDelayed()为例：它的调用逻辑是这样的：
```java
public final boolean postDelayed(Runnable r, long delayMillis){
        return sendMessageDelayed(getPostMessage(r), delayMillis);
}
```
```java
public final boolean sendMessageDelayed(Message msg, long delayMillis){
        if (delayMillis < 0) {
            delayMillis = 0;
        }
        return sendMessageAtTime(msg, SystemClock.uptimeMillis() + delayMillis);
}
```
```java
public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
        MessageQueue queue = mQueue;
        if (queue == null) {
            RuntimeException e = new RuntimeException(
                    this + " sendMessageAtTime() called with no mQueue");
            Log.w("Looper", e.getMessage(), e);
            return false;
        }
        return enqueueMessage(queue, msg, uptimeMillis);
}
```
```java
private boolean enqueueMessage(MessageQueue queue, Message msg, long uptimeMillis) {
        msg.target = this;
        if (mAsynchronous) {
            msg.setAsynchronous(true);
        }
        return queue.enqueueMessage(msg, uptimeMillis);
}
```
进入到了MessageQueue的enqueueMessage()方法：
```java
boolean enqueueMessage(Message msg, long when) {
       ...
        synchronized (this) {
            if (mQuitting) {             
                msg.recycle();
                return false;
            }
            msg.markInUse();
            msg.when = when;
            Message p = mMessages;
            boolean needWake;
            if (p == null || when == 0 || when < p.when) {
                msg.next = p;
                mMessages = msg;
                needWake = mBlocked;
            } else {
                needWake = mBlocked && p.target == null && msg.isAsynchronous();
                Message prev;
                for (;;) {
                    prev = p;
                    p = p.next;
                    if (p == null || when < p.when) {
                        break;
                    }
                    if (needWake && p.isAsynchronous()) {
                        needWake = false;
                    }
                }
                msg.next = p; // invariant: p == prev.next
                prev.next = msg;
            }
            if (needWake) {
                nativeWake(mPtr);
            }
        }
        return true;
    }
```
在上面的enqueueMessage()方法中会将延迟的时间when放到msg上，并加入到messageQueue中。我们知道Looper会在不断的进行loop操作，loop()是在一个死循环中不断的从MessageQueue中取message，然后交给handler进行处理消息。此时如果MessageQueue中存在延时的message看看要怎么处理？
进入queue.next()方法中看看做了什么：
```java
Message next() {
        ...
        for (;;) {
           ...
            nativePollOnce(ptr, nextPollTimeoutMillis);   // native 函数

            synchronized (this) {
                final long now = SystemClock.uptimeMillis();
                Message prevMsg = null;
                Message msg = mMessages;
                ...
                if (msg != null) {
                    if (now < msg.when) {
                        nextPollTimeoutMillis = (int) Math.min(msg.when - now, Integer.MAX_VALUE);
                    } else {
                       ...
                    }
                } else {
                    nextPollTimeoutMillis = -1;
                }              
                ...
            }
            ...
            nextPollTimeoutMillis = 0;
        }
    }
```
可以看到，在这个方法内，如果头部的这个Message是有延迟而且延迟时间没到的（now < msg.when），会计算一下时间（保存为变量nextPollTimeoutMillis），然后在循环开始的时候判断如果这个Message有延迟，就调用nativePollOnce(ptr, nextPollTimeoutMillis)进行阻塞。nativePollOnce()的作用类似与object.wait()，只不过是使用了Native的方法对这个线程精确时间的唤醒。

1. postDelay()一个10秒钟的Runnable A、消息进队，MessageQueue调用next()方法中的```nativePollOnce()```阻塞，Looper阻塞；
2. 紧接着post()一个Runnable B、消息进队，判断现在A时间还没到、正在阻塞，把B插入消息队列的头部（A的前面），然后调用MessageQueue的enqueueMessage()方法中的```nativeWake()```方法唤醒线程；
3. MessageQueue.next()方法被唤醒后，重新开始读取消息链表，第一个消息B无延时，直接返回给Looper；
4. Looper处理完这个消息再次调用next()方法，MessageQueue继续读取消息链表，第二个消息A还没到时间，计算一下剩余时间（假如还剩9秒）继续调用```nativePollOnce()```阻塞；直到阻塞时间到或者下一次有Message进队；



## 9、Message可以如何创建？哪种效果更好，为什么？
创建Message对象的三种方式：
- Message msg = new Message();
- Message msg = Message.obtain();
- Message msg = handler1.obtainMessage();

后两种方法都是从整个Messge池中返回一个新的Message实例，能有效避免重复Message创建对象，因此更鼓励这种方式创建Message




## 10、 ThreadLocal有什么作用？
ThreadLocal类可实现线程本地存储的功能，把共享数据的可见范围限制在同一个线程之内，无须同步就能保证线程之间不出现数据争用的问题，这里可理解为ThreadLocal帮助Handler找到本线程的Looper。

每个线程的Thread对象中都有一个ThreadLocalMap对象，它存储了一组以ThreadLocal.threadLocalHashCode为key、以本地线程变量为value的键值对，而ThreadLocal对象就是当前线程的ThreadLocalMap的访问入口，也就包含了一个独一无二的threadLocalHashCode值，通过这个值就可以在线程键值值对中找回对应的本地线程变量。


## 11、为什么主线程不用调用 Looper.prepare() ？
主线程的 loop()方法是在 ActivityThread#main()方法中被调用的，那么看看 main() 方法：
```java
//ActivityThread.java 删减部分代码
public static void main(String[] args) {
    Looper.prepareMainLooper();
    Looper.loop();
}
```
到这里就能明白了，在App启动的时候系统默认启动了一个主线程的 Looper,prepareMainLooper()也是调用了 prepare()方法，里面会创建一个不可退出的 Looper,并 set 到 sThreadLocal对象当中。



## 12、Handler 里藏着的 Callback 能干什么？
来看看 Handler.dispatchMessage(msg)  方法：
```java
public void dispatchMessage(Message msg) {
   //这里的 callback 是 Runnable 
   if (msg.callback != null) {
        handleCallback(msg);
    } else {
       // 如果 callback 处理了该 msg 并且返回 true， 就不会再回调 handleMessage
        if (mCallback != null) {
            if (mCallback.handleMessage(msg)) {
                return;
            }
        }
        handleMessage(msg);
    }
}
```
可以看到 Handler.Callback 有优先处理消息的权利 ，当一条消息被 Callback 处理并拦截（返回 true），那么 Handler 的 handleMessage(msg) 方法就不会被调用了；如果 Callback 处理了消息，但是并没有拦截，那么就意味着一个消息可以同时被 Callback 以及 Handler 处理。

这个就很有意思了，这有什么作用呢？我们可以利用 Callback 这个拦截机制来拦截 Handler 的消息！

场景：Hook ActivityThread.mH ， 在 ActivityThread 中有个成员变量 mH ，它是个 Handler，又是个极其重要的类，几乎所有的插件化框架都使用了这个方法。



## 13、 子线程里弹 Toast 的正确姿势
当我们尝试在子线程里直接去弹 Toast 的时候，会 crash ：
java.lang.RuntimeException: Can't create handler inside thread that has not called Looper.prepare() 
本质上是因为 Toast 的实现依赖于 Handler，按子线程使用 Handler 的要求修改即可，同理的还有 Dialog。
正确示例代码如下：
```java
new Thread(new Runnable() {
    @Override
    public void run() {
       Looper.prepare();
       Toast.makeText(HandlerActivity.this, "不会崩溃啦！", 
       Toast.LENGTH_SHORT).show();
       Looper.loop();
    }
});
```





## 14、妙用 Looper 机制
我们可以利用 Looper 的机制来帮助我们做一些事情：
将 Runnable post 到主线程执行，利用 Looper 判断当前线程是否是主线程。
```java
public final class MainThread {
     private MainThread() {}

     private static final Handler HANDLER = new Handler(Looper.getMainLooper());

     public static void run(@NonNull Runnable runnable) {
          if (isMainThread()) {
              runnable.run();
          }else{
              HANDLER.post(runnable);
          }
      }

      public static boolean isMainThread() {
         return Looper.myLooper() == Looper.getMainLooper();
     }
}
```



## 15、为什么 Handler 会造成内存泄漏？
Handler 允许我们发送延时消息，如果在延时期间用户关闭了 Activity，那么该 Activity 会泄露。

这个泄露是因为 Message 会持有 Handler，而又因为 Java 的特性，内部类会持有外部类，使得 Activity 会被 Handler 持有，这样最终就导致 Activity 泄露。

解决该问题的最有效的方法是：将 Handler 定义成静态的内部类，在内部持有 Activity 的弱引用，并及时移除所有消息。
示例代码如下：
```java
public class MainActivity extends AppCompatActivity {

    public TextView textView;

    static class WeakRefHandler extends Handler {

        //弱引用
        private WeakReference<MainActivity> reference;

        public WeakRefHandler(MainActivity mainActivity) {
            this.reference = new WeakReference<MainActivity>(mainActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = reference.get();
            if (activity != null) {
                activity.textView.setText("XXXX");
            }
        }
    }
}

```
并且再在 Activity. onPause() 前移除消息：
```java
@Override
protected void onPause() {    
     super.onPause();
     weakRefHandler.removeCallbacksAndMessages(null);
}
```
注意：单纯的在 onDestroy 移除消息并不保险，因为 onDestroy 并不一定执行。

## 16、主线程中有多个handler的情况
[https://www.cnblogs.com/transmuse/archive/2011/05/16/2048073.html](https://www.cnblogs.com/transmuse/archive/2011/05/16/2048073.html)



## 17、系统怎么知道把消息发送给哪个Handler？
其实就是在enqueueMessage()中系统给msg设置了target从而确定了其目标Handler，所以只要通过msg.target.dispatchMessage(msg)就可以将消息派发至对应的Handler了。
## 18、Handler.post(Runnable)、 Activity.runOnUiThread()、View.post(Runnable r)方法是运行在新的线程吗？
我们在开发中可能会做如上的操作：在主线程中创建Handler，然后在子线程里利用handler.post(Runnable runnable)执行某些操作甚至是耗时的操作。可是这么做合适么？我们来看看主线程的ID和在Runnable的run()方法里获取到的线程ID，输出日志如下：
```
主线程的线程ID=1 
在post(Runnable r)里的run()获取到线程ID=1
```
在这里我们发现在两处获得的线程ID是同一个值，也就是说Runnable的run()方法并不是在一个新线程中执行的，而是在主线程中执行的。 
为什么明明把handler.post(Runnable runnable)放入到子线程中了但是Runnable的run()却在主线程中执行呢？ 
其实，这个问题在之前的分析中已经提到了：调用handler.post(Runnable runnable)时，该runnable会被系统封装为Message的callback。所以，handler.post(Runnable runnable)和handler.sendMessage(Message message)这两个不同的方法在本质上是相同的——Handler发送了一条消息。在该示例中handler是在主线程中创建的，所以它当然会在主线程中处理消息；如此以来该Runnable亦会在主线程中执行；所以，在Runnable的run()方法中执行耗时的操作是不可取的容易导致应用程序无响应。

那么，调用view.post(Runnable runnable)会在子线程中执行还是主线程中执行呢？ 
我们来瞅瞅它的实现：
```java
public boolean post(Runnable action) {
    final AttachInfo attachInfo = mAttachInfo;
    if (attachInfo != null) {
        return attachInfo.mHandler.post(action);
    }
    getRunQueue().post(action);
    return true;
}
```
看到这段源码就无需再做过多的解释了，它依然是在主线程中执行的，原理同上。

那么，调用Activity.runOnUiThread(Runnable runnable)方法会在子线程中执行还是主线程中执行呢？
```java
public final void runOnUiThread(Runnable action) {
    if (Thread.currentThread() != mUiThread) {
        mHandler.post(action);
    } else {
        action.run();
    }
}
```
嗯哼，这段源码就更简单了。如果当前线程是UI线程，那么该Runnable会立即执行；如果当前线程不是UI线程，则使用handler的post()方法将其放入UI线程的消息队列中。

总结： 
```handler.post(Runnable runnable)```
```view.post(Runnable runnable)```
```Activity.runOnUiThread(Runnable runnable)```
的runnable均会在主线程中执行，所以切勿在其run()方法中执行耗时的操作。

## 19、Handler(Callback) 跟 Handler() 这两个构造方法的区别在哪？ 
Callback.handleMessage() 的优先级比 Handler.handleMessage()要高 。如果存在Callback,并且Callback#handleMessage() 返回了 true ,那么Handler#handleMessage()将不会调用。