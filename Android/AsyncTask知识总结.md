# 1、AsyncTask简介
AsyncTask是一个抽象类，它是由Android封装的一个轻量级异步类（轻量体现在使用方便、代码简洁），它可以在线程池中执行后台任务，然后把执行的进度和最终结果传递给主线程并在主线程中更新UI。

AsyncTask的内部封装了两个线程池(**SerialExecutor**和**THREAD_POOL_EXECUTOR**)和一个**Handler(InternalHandler)**。

其中**SerialExecutor**线程池用于任务的排队，让需要执行的多个耗时任务，按顺序排列，**THREAD_POOL_EXECUTO**R线程池才真正地执行任务，**InternalHandler**用于从工作线程切换到主线程。

sHandler是一个静态的Handler对象，为了能够将执行环境切换到主线程，这就要求sHandler这个对象必须在主线程创建。由于静态成员会在加载类的时候进行初始化，因此这就变相要求AsyncTask的类必须在主线程中加载，否则同一个进程中的AsyncTask都将无法正常工作。

## 1.1 AsyncTask的泛型参数
AsyncTask的类声明如下：
```java
public abstract class AsyncTask<Params, Progress, Result>
```
AsyncTask是一个抽象泛型类。

其中，三个泛型类型参数的含义如下：

**Params：** 开始异步任务执行时传入的参数类型；

**Progress：** 异步任务执行过程中，返回下载进度值的类型；

**Result：** 异步任务执行完成后，返回的结果类型；

如果AsyncTask确定不需要传递具体参数，那么这三个泛型参数可以用 **Void** 来代替。

有了这三个参数类型之后，也就控制了这个AsyncTask子类各个阶段的返回类型，如果有不同业务，我们就需要再另写一个AsyncTask的子类进行处理。

## 1.2 AsyncTask的核心方法
**onPreExecute()**

这个方法会在 **后台任务开始执行之间调用，在主线程执行。** 用于进行一些界面上的初始化操作，比如显示一个进度条对话框等。

**doInBackground(Params...)**

这个方法中的所有代码都会在子线程中运行，我们应该在这里去处理所有的耗时任务。

任务一旦完成就可以通过return语句来将任务的执行结果进行返回，如果AsyncTask的第三个泛型参数指定的是Void，就可以不返回任务执行结果。注意，在这个方法中是不可以进行UI操作的，如果需要更新UI元素，比如说反馈当前任务的执行进度，可以调用```publishProgress(Progress...)```方法来完成。

**onProgressUpdate(Progress...)**

当在后台任务中调用了```publishProgress(Progress...)```方法后，这个方法就很快会被调用，方法中携带的参数就是在后台任务中传递过来的。在这个方法中可以对UI进行操作，在主线程中进行，利用参数中的数值就可以对界面元素进行相应的更新。

**onPostExecute(Result)**

当```doInBackground(Params...)```执行完毕并通过return语句进行返回时，这个方法就很快会被调用。返回的数据会作为参数传递到此方法中，可以利用返回的数据来进行一些UI操作，在主线程中进行，比如说提醒任务执行的结果，以及关闭掉进度条对话框等。

上面几个方法的调用顺序： **onPreExecute() --> doInBackground() --> publishProgress() --> onProgressUpdate() --> onPostExecute()**

如果不需要执行更新进度则为**onPreExecute() --> doInBackground() --> onPostExecute()**,

除了上面四个方法，AsyncTask还提供了```onCancelled()```方法，它同样在主线程中执行，当异步任务取消时，```onCancelled()```会被调用，这个时候```onPostExecute()```则不会被调用，但是要注意的是，AsyncTask中的cancel()方法并不是真正去取消任务，只是设置这个任务为取消状态，我们需要在doInBackground()判断终止任务。就好比想要终止一个线程，调用interrupt()方法，只是进行标记为中断，需要在线程内部进行标记判断然后中断线程。

## 1.3 使用AsyncTask的注意事项
1. 异步任务的实例必须在UI线程中创建，即AsyncTask对象必须在UI线程中创建。

2. execute(Params... params)方法必须在UI线程中调用。

3. 不要手动调用onPreExecute()，doInBackground(Params... params)，onProgressUpdate(Progress... values)，onPostExecute(Result result)这几个方法。

4. 不能在doInBackground(Params... params)中更改UI组件的信息。

5. 一个任务实例只能执行一次，如果执行第二次将会抛出异常。
6. 在1.6之前，AsyncTask是串行执行任务的，1.6的时候AsyncTask开始采用线程池里处理并行任务，但是从3.0开始，为了避免AsyncTask所带来的并发错误，AsyncTask又采用一个线程来串行执行任务

# 2、AsyncTask使用

```java
class DownloadTask extends AsyncTask<Void, Integer, Boolean> {  

    @Override  
    protected void onPreExecute() {  
        progressDialog.show();  
    }  

    @Override  
    protected Boolean doInBackground(Void... params) {  
        try {  
            while (true) {  
                int downloadPercent = doDownload();  
                publishProgress(downloadPercent);  
                if (downloadPercent >= 100) {  
                    break;  
                }  
            }  
        } catch (Exception e) {  
            return false;  
        }  
        return true;  
    }  

    @Override  
    protected void onProgressUpdate(Integer... values) {  
        progressDialog.setMessage("当前下载进度：" + values[0] + "%");  
    }  

    @Override  
    protected void onPostExecute(Boolean result) {  
        progressDialog.dismiss();  
        if (result) {  
            Toast.makeText(context, "下载成功", Toast.LENGTH_SHORT).show();  
        } else {  
            Toast.makeText(context, "下载失败", Toast.LENGTH_SHORT).show();  
        }  
    }  
}
```
这里我们模拟了一个下载任务，在doInBackground()方法中去执行具体的下载逻辑，在onProgressUpdate()方法中显示当前的下载进度，在onPostExecute()方法中来提示任务的执行结果。如果想要启动这个任务，只需要简单地调用以下代码即可：

```java
new DownloadTask().execute();
```



# 3、AsyncTask源码分析
先从初始化一个AsyncTask时，调用的构造函数开始分析。
```java
public AsyncTask() {
	mWorker = new WorkerRunnable<Params, Result>() {
		public Result call() throws Exception {
			mTaskInvoked.set(true);
			Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
			return postResult(doInBackground(mParams));
		}
	};
	mFuture = new FutureTask<Result>(mWorker) {
		@Override
		protected void done() {
			try {
				final Result result = get();
				postResultIfNotInvoked(result);
			} catch (InterruptedException e) {
				android.util.Log.w(LOG_TAG, e);
			} catch (ExecutionException e) {
				throw new RuntimeException("An error occured while executing doInBackground()",
						e.getCause());
			} catch (CancellationException e) {
				postResultIfNotInvoked(null);
			} catch (Throwable t) {
				throw new RuntimeException("An error occured while executing "
						+ "doInBackground()", t);
			}
		}
	};
}
```


这里面只是初始化了两个成员变量：mWorker和mFuture，并在初始化mFuture的时候将mWorker作为参数传入。mWorker是一个Callable对象，mFuture是一个FutureTask对象，这两个变量会暂时保存在内存中，稍后才会用到它们。

接着如果想要启动某一个任务，就需要调用该任务的execute()方法，因此现在我们来看一看execute()方法的源码，如下所示：
```java
@MainThread
public final AsyncTask<Params, Progress, Result> execute(Params... params) {
    return executeOnExecutor(sDefaultExecutor, params);
}
```
发现该方法中添加一个@MainThread的注解，通过该注解，可以知道我们在执行AsyncTask的execute方法时，只能在主线程中执行。

该方法仅是调用了executeOnExecutor()方法,具体执行逻辑在这个方法里面：
```java
public final AsyncTask<Params, Progress, Result> executeOnExecutor(Executor exec,
		Params... params) {
	if (mStatus != Status.PENDING) {
		switch (mStatus) {
			case RUNNING:
				throw new IllegalStateException("Cannot execute task:"
						+ " the task is already running.");
			case FINISHED:
				throw new IllegalStateException("Cannot execute task:"
						+ " the task has already been executed "
						+ "(a task can be executed only once)");
		}
	}
	mStatus = Status.RUNNING;
	onPreExecute();
	mWorker.mParams = params;
	exec.execute(mFuture);
	return this;
}
```
可以看出，先执行了onPreExecute()方法，然后具体执行耗时任务是在exec.execute(mFuture)，把构造函数中实例化的mFuture传递进去了。

exec具体是什么？

从上面可以看出具体是sDefaultExecutor，接着找一下这个sDefaultExecutor变量是在哪里定义的，具体源码如下：
```java
public static final Executor SERIAL_EXECUTOR = new SerialExecutor();
……
private static volatile Executor sDefaultExecutor = SERIAL_EXECUTOR;
```
可以看到，这里先new出了一个SERIAL_EXECUTOR常量，然后将sDefaultExecutor的值赋值为这个常量，也就是说明，刚才在executeOnExecutor()方法中调用的execute()方法，其实也就是调用的SerialExecutor类中的execute()方法。那么我们自然要去看看SerialExecutor的源码了，如下所示：
```java
private static class SerialExecutor implements Executor {
	final ArrayDeque<Runnable> mTasks = new ArrayDeque<Runnable>();
	Runnable mActive;
 
	public synchronized void execute(final Runnable r) {
		mTasks.offer(new Runnable() {
			public void run() {
				try {
					r.run();
				} finally {
					scheduleNext();
				}
			}
		});
		if (mActive == null) {
			scheduleNext();
		}
	}
 
	protected synchronized void scheduleNext() {
		if ((mActive = mTasks.poll()) != null) {
			THREAD_POOL_EXECUTOR.execute(mActive);
		}
	}
}
```

SerialExecutor 内部维持了一个队列，通过锁使得该队列保证AsyncTask中的任务是串行执行的，即多个任务需要一个个加到该队列中，然后执行完队列头部的再执行下一个，以此类推。

在这个方法中，有两个主要步骤。 
1. 向队列中加入一个新的任务，即之前实例化后的mFuture对象。
2. 调用 scheduleNext()方法，调用THREAD_POOL_EXECUTOR执行队列头部的任务。

THREAD_POOL_EXECUTOR实际是个线程池，开启了一定数量的核心线程和工作线程。然后调用线程池的execute()方法。执行具体的耗时任务，即开头构造函数中mWorker中call()方法的内容。先执行完doInBackground()方法，又执行postResult()方法，下面看该方法的具体内容：

SerialExecutor类中也有一个execute()方法，这个方法里的所有逻辑就是在子线程中执行的了，注意这个方法有一个Runnable参数，那么目前这个参数的值是什么呢？当然就是mFuture对象了，也就是调用的是FutureTask类的run()方法，而在这个方法里又会去调用Sync内部类的innerRun()方法，在该方法中又调用了callable的call()方法，那么这个callable对象是什么呢？其实就是在初始化mFuture对象时传入的mWorker对象了，此时调用的call()方法，也就是一开始在AsyncTask的构造函数中指定的，我们把它单独拿出来看一下，代码如下所示：
```java
public Result call() throws Exception {
	mTaskInvoked.set(true);
	Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
	return postResult(doInBackground(mParams));
}
```
在postResult()方法的参数里面，我们终于找到了doInBackground()方法的调用处，虽然经过了很多周转，但目前的代码仍然是运行在子线程当中的，所以这也就是为什么我们可以在doInBackground()方法中去处理耗时的逻辑。接着将doInBackground()方法返回的结果传递给了postResult()方法，这个方法的源码如下所示：
```java
private Result postResult(Result result) {
	Message message = sHandler.obtainMessage(MESSAGE_POST_RESULT,
			new AsyncTaskResult<Result>(this, result));
	message.sendToTarget();
	return result;
}
```
这里使用sHandler对象发出了一条消息，消息中携带了MESSAGE_POST_RESULT常量和一个表示任务执行结果的AsyncTaskResult对象。这个sHandler对象是InternalHandler类的一个实例，那么稍后这条消息肯定会在InternalHandler的handleMessage()方法中被处理。InternalHandler的源码如下所示：
```java
private static class InternalHandler extends Handler {
	@SuppressWarnings({"unchecked", "RawUseOfParameterizedType"})
	@Override
	public void handleMessage(Message msg) {
		AsyncTaskResult result = (AsyncTaskResult) msg.obj;
		switch (msg.what) {
			case MESSAGE_POST_RESULT:
				// There is only one result
				result.mTask.finish(result.mData[0]);
				break;
			case MESSAGE_POST_PROGRESS:
				result.mTask.onProgressUpdate(result.mData);
				break;
		}
	}
}
```


这里对消息的类型进行了判断，如果这是一条MESSAGE_POST_RESULT消息，就会去执行finish()方法，如果这是一条MESSAGE_POST_PROGRESS消息，就会去执行onProgressUpdate()方法。那么finish()方法的源码如下所示：
```java
private void finish(Result result) {
	if (isCancelled()) {
		onCancelled(result);
	} else {
		onPostExecute(result);
	}
	mStatus = Status.FINISHED;
}
```
可以看到，如果当前任务被取消掉了，就会调用onCancelled()方法，如果没有被取消，则调用onPostExecute()方法，这样当前任务的执行就全部结束了。


我们注意到，在刚才InternalHandler的handleMessage()方法里，还有一种MESSAGE_POST_PROGRESS的消息类型，这种消息是用于当前进度的，调用的正是onProgressUpdate()方法，那么什么时候才会发出这样一条消息呢？相信你已经猜到了，查看publishProgress()方法的源码，如下所示：
```java
protected final void publishProgress(Progress... values) {
	if (!isCancelled()) {
		sHandler.obtainMessage(MESSAGE_POST_PROGRESS,
				new AsyncTaskResult<Progress>(this, values)).sendToTarget();
	}
}
```
正因如此，在doInBackground()方法中调用publishProgress()方法才可以从子线程切换到UI线程，从而完成对UI元素的更新操作。其实也没有什么神秘的，因为说到底，AsyncTask也是使用的异步消息处理机制，只是做了非常好的封装而已。
# 4、AsyncTask的一些问题
1. **生命周期：** AsyncTask不与任何组件绑定生命周期，所以在Activity/或者Fragment中创建执行AsyncTask时，最好在Activity/Fragment的onDestory()调用 cancel(boolean)；

2. **内存泄漏：** 如果AsyncTask被声明为Activity的非静态的内部类，那么AsyncTask会保留一个对创建了AsyncTask的Activity的引用。如果Activity已经被销毁，AsyncTask的后台线程还在执行，它将继续在内存里保留这个引用，导致Activity无法被回收，引起内存泄露。

3. **结果丢失：** 屏幕旋转或Activity在后台被系统杀掉等情况会导致Activity的重新创建，之前运行的AsyncTask（非静态的内部类）会持有一个之前Activity的引用，这个引用已经无效，这时调用onPostExecute()再去更新界面将不再生效。

   1. **并行还是串行：** 在3.0以前，最大支持128个线程的并发，10个任务的等待，同时执行138个任务是没有问题的；而超过138会马上出现java.util.concurrent.RejectedExecutionException。在3.0以后，无论有多少任务，都会在其内部单线程执行；

5. **如果想让AsyncTask在3.0以上的系统中并行：**  调用AsyncTask的```executeOnExecutor```方法**而不是**```execute```方法
```java
new MyAsyncTask("AsyncTask#1").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"");
new MyAsyncTask("AsyncTask#2").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"");
new MyAsyncTask("AsyncTask#3").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"");
new MyAsyncTask("AsyncTask#4").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"");
new MyAsyncTask("AsyncTask#5").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"");
```