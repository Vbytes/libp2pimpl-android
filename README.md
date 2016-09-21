VbyteP2P Android SDK
===

**[jcenter链接][]**

**[demo下载][]**

## 依赖安装

### gradle编译（推荐）

如果您的项目是一个使用gradle编译的AndroidStudio项目，那么集成是非常简单的。

- 首先在[devcenter][]上注册帐号，创建应用，创建应用时要写对包名。然后得到app id,app key与app secret key
- 然后添加依赖，随后等gradle同步之后，即可使用该SDK的各种接口
```
dependencies {
    // 加入下面依赖
    compile 'cn.vbyte.p2p:libp2p:1.2.2'  
    compile 'cn.vbyte.p2p:libp2pimpl:1.2.7'  
}
```
- 在应用启动之初，启动VbyteP2PModule
```java
// 初始化VbyteP2PModule的相关变量，这是Android sample的样例
final String APP_ID = "577cdcabe0edd1325444c91f";
final String APP_KEY = "G9vjcbxMYZ5ybgxy";
final String APP_SECRET = "xdAEKlyF9XIjDnd9IwMw2b45b4Fq9Nq9";

protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // 初始化P2P模块
    try {
        VbyteP2PModule.create(this.getBaseContext(), APP_ID, APP_KEY, APP_SECRET);
    } catch (Exception e) {
        e.printStackTrace();
    }

    // ... 

}
```
- 启动一个频道的过程如下:
```java
try {
    LiveController.getInstance().load("your channel id", "UHD", new OnLoadedListener() {
        @Override
        public void onLoaded(Uri uri) {
            mVideoPath = uri.toString();
            mVideoView.setVideoURI(uri);
            mVideoView.start();
        }
    });
} catch (Exception e) {
    // 如果打印了此exception，说明load/unload没有成对出现
    e.printStackTrace();
}
```
- 然后就可以尽情地使用IJKPlayer和我们的P2P带来的便利功能吧

### 传统的Eclipse编译

如果您的项目是一个传统的Eclipse项目，那可能要稍微麻烦一点。
- 首先，下载[archive][]，将libs下的内容放在项目根目录的libs/armeabi-v7a/下面
- 添加到编译依赖libp2p.jar、libp2pimpl.jar
- 以上完成后，即可使用该SDK里面的API

## 文档

### 接口API

> VbyteP2PModule.create("app id", "app kkey", "app secret key");

此接口传入您申请的appId，appKey，appSecretKey，来完成P2P模块的载入和初始化

> VbyteP2PModule.setErrorHandler(errorHandler);  
> VbyteP2PModule.setEventHandler(eventHandler);

这2个接口设置一个P2P模块的错误处理器和一般事件处理器，其中errorHandler、eventHandler是一个普通的Handler，可以像下面这样实现
```java
public MyHandler extends Handler {
    public void handleMessage(Message msg) {   
        String instruction = (String) msg.obj;
        switch (msg.what) {   
            case VbyteP2PModule.INITED:  
                Log.d(TAG, instruction);
                break;   
            // ... something 
        }   
        super.handleMessage(msg);   
    }  
}
```

> VbyteP2PModule.version();  

 该接口获取P2P模块的版本号，返回一个一`v`开头的字符串，您可以看需使用。

> VbyteP2PModule.enableDebug();  
> VbyteP2PModule.disableDebug();

这2个接口是debug开关的接口，默认是打开的，在发布App时，应关闭debug。

> LiveCtroller.load(channel, resolution, startTime, listener);

该接口载入一个直播频道，频道为channel，分辨率为resolution的源，并使用P2P加速。该函数的最后一个参数是一个回调函数，会返回一个URI，一般使用该URI可直接给播放器打开并播放之。

> LiveCtroller.unload();

该接口与load相对应，用于关闭一个频道，应用同一时刻只能播放一个源，所以调用此函数会将上一个您加载的源关闭。该函数应该用在您想让播放器退出的时候。

> VodCtroller.load(channel, resolution, startTime, listener);

该接口载入一个点播视频，频道为channel，分辨率为resolution的源，startTime是从哪一时间开始播，并使用P2P加速。该函数的最后一个参数是一个回调函数，会返回一个URI，一般使用该URI可直接给播放器打开并播放之。

> VodCtroller.unload();

该接口与load相对应，用于关闭一个点播视频，应用同一时刻只能播放一个源，所以调用此函数会将上一个您加载的源关闭。该函数应该用在您想让播放器退出的时候。

### 事件

#### 正常事件

* **Event.INITED:** : 标志着P2P模块的创建成功
* **Event.START**: 标志着P2P成功加载频道
* **Event.STOP**: 表明P2P成功停止了上一个频道（上一个频道可能早被停止过了）
* **Event.EXIT**: 表明P2P模块收到了退出信号，即将退出
* **Event.DESTROY**: 标志着P2P模块成功销毁了自己
* **Event.STUN_SUCCESS**: 表明P2P模块成功获取到了自己的公网地址
* **Event.JOIN_SUCCESS**: 表明P2P模块在载入一个频道的过程中成功加入了P2P的大军
* **Event.HTBT_SUCCESS**: 表明此时当前程序实例没有掉队
* **Event.BYE_SUCCESS**: 表明当前程序实例要退出P2P了，这在播放器停止播放，程序调用unload之后会发生
* **Event.NEW_PARTNER**: 表明当前应用程序又获取了一个伙伴
* **Event.STREAM_READY**: 表明即将载入频道的数据流已经就绪，将会给播放器数据，在播放器有足够的缓冲后（这取决于播放器自己的设定），就会有画面呈现
* **Event.P2P_STABLE**: 表明当前程序实例的P2P效果很稳定
* **Event.BLOCK**: 表明在写数据时遇到了阻塞，这可能会造成播放器的卡顿
* **Event.REPORT**: 表明P2P模块将上传数据，要上传的数据在message里面，是一段json数据

**注意**: 请务必处理这些事件时不要执行耗时的操作，因为它跟Android ui主线程一样，如果耗时太久，将会阻止数据流的连续载入；如需要耗时的操作，请使用异步处理。

#### 异常和错误

* **Error.CONF_UNAVAILABLE**: 配置服务器不可用，将停止载入，不会播放！
* **Error.AUTH_FAILED**: 认证失败，此时您应确保您填入的app id，app key， app secret key都正确
* **Error.CONF_INVALID**: 配置不对，此时，应联系运营人员或者我们，及时修改
* **Error.CHANNEL_EMPTY**: 您在载入一个频道时没有传频道或者频道为空
* **Error.RESOLUTION_INVALID**: 该频道不存在这个分辨率，您填写的分辨率不合法或者超出的源本有的清晰度
* **Error.NO_SUCH_CHANNEL**: 不存在你想要播放的频道，请检查和确认你填写的频道是否正确，是否被下线等
* **Error.BAD_NETWORK**: 网络差，或者程序没有连接上网络，这个错误将会在P2P模块联网超时N次超时后抛出
* **Error.STUN_FAILED**: 获取自己的公网地址失败，此时应用程序将退化为和普通CDN一样拉去数据流，将没有P2P效果
* **Error.CDN_UNSTABLE**: 表明CDN不稳定，可能因网络造成，可能因源本身就不太稳定，P2P模块在连续N次获取数据失败后会抛出此错误，并停止加载，您的程序收到此错误后，可让用户刷新重试。
* **Error.JOIN_FAILED**: 加入P2P大军失败，后续会继续尝试
* **Error.HTBT_FAILED**: 表明应用程序已掉队，对P2P效果会减弱，并且可能会带来片刻的卡顿
* **Error.BYE_FAILED**: 退出P2P大军时失败，然而这不会影响当前应用程序从P2P大军中剔除
* **Error.REPORT_FAILED**: 应用程序上报统计数据失败
* **Error.UNKNOWN_PACKET**: 收到一个未知类型的包，将忽略
* **Error.INVALID_PACKET**: 收到一个数据不一致的包，将忽略
* **Error.INTERNAL**: 内部错误

[jcenter链接]: https://bintray.com/vbyte/maven/libp2pimpl
[demo下载]: http://data1.vbyte.cn/apk/vbyte-demo.20160921.apk
[archive]: http://data1.vbyte.cn/pkg/20160921.tar.gz
[devcenter]: http://devcenter.vbyte.cn
