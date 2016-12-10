Vbyte P2P Android SDK
===

vbyte云视频解决方案，可帮助用户直接使用经过大规模验证的直播流媒体分发服务,通过vbyte成熟的P2P技术大幅节省带宽，提供更优质的用户体验。开发者可通过SDK中简洁的接口快速同自有应用集成，实现Android设备上的视频P2P加速功能。

## 功能

- 直播、点播基本功能+P2P
- 直播时移支持
- HLS P2P原生支持
- 防盗播支持

## 依赖安装

#### gradle编译（推荐）

Android SDK托管于第三方android lib平台[jcenter][9]上，依赖部署是非常简单的。凭借这设计良好的接口，在使用上也非常方便。如果您的项目是一个使用gradle编译的AndroidStudio项目，那么集成是非常简单的。

添加如下依赖，随后等gradle同步之后，即可使用该SDK的各种接口:

```
dependencies {
    // 加入下面依赖
    compile 'cn.vbyte.p2p:libp2p:1.2.7'
    compile 'cn.vbyte.p2p:libp2pimpl:1.2.8'  
}
```

#### 传统的Eclipse编译

如果您的项目是一个传统的Eclipse项目，首先建议您转换到Android Studio上面去，否则可能要麻烦一点:

- 首先，下载[archive][7]，将libs下的内容放在项目根目录的libs/下面
- 添加到编译依赖libp2p.jar、libp2pimpl.jar
- 以上完成后，即可使用该SDK里面的API

## 开始使用

- 首先参考[资源管理][8]在[开发者中心][1]上注册帐号，创建应用，创建应用时要写对包名，并创建频道。然后得到app id,app key与app secret key
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

#### 使用直播

直播大家都很熟悉，观众一进来都是直接看到最新的直播内容，本是没有暂停、随机播放（回看）功能。但是应时移回看需求的增长，我们的SDK也提供了时移回看的方式，详细见[API文档][2]。

- 启动一个直播频道的过程如下，其中第2个参数是写死的，必须为UHD；未来多码率支持可能会有HD等更多参数选择:
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
- 退出当前直播播放频道，只需要调用unload即可
```java
    LiveController.getInstance().unload();
```

#### 使用点播

点播与直播最大的不同是点播视频是固定的，包括文件大小固定、视频时长固定，有暂停、恢复、随机播放等操作。

- 启动一个点播频道的过程如下:
```java
try {
    VodController.getInstance().load("your vod channel id", "UHD", 0, new OnLoadedListener() {
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
- 暂停和恢复当前点播节目:
```java
    // 暂停当前节目
    VodController.getInstance().pause();
    // 恢复播放当前节目
    VodController.getInstance().resume();
```
- 点播视频支持在不重启播放器的情况下随机位置播放，这在观众滑动进度条时发生，此时播放器会从进度条位置重新加载，而P2P模块能自动感知这样的seek行为，您不用为此做什么。
- 退出当前点播播放频道，只需要调用unload即可
```java
    VodController.getInstance().unload();
```

#### 高级功能

更多高级功能诸如开启debug开关、事件监听、直播时移等请参见[Android版API][2]文档，然后就可以尽情地使用P2P SDK带来的便利功能吧！

## 扩展链接

* **[Github][3]**: SDK的开源代码仓库
* **[AndroidSample][4]**: 一个使用ijkplayer的简单样例
* **[API Doc][2]**: 更加详细的API文档，其中包含如直播时移的高级功能
* **[jcenter][5]**: Android SDK在jcenter上的位置
* **[demo下载][6]**: 此即为AndroidSample已编译完成的版本，里面的内容属于[开发者中心][1]测试帐号的，欢迎试用

## 技术支持

感谢阅读本篇文档，希望能帮您尽快上手Android SDK的用法，再次欢迎您使用月光石P2P加速SDK！

*温馨提示*：如果你需要任何帮助，或有任何疑问，请[联系我们](mailto:contact@exatech.cn)。

[1]: http://devcenter.vbyte.cn
[2]: http://docs.vbyte.cn/api/android/
[3]: https://github.com/Vbytes/libp2pimpl-android
[4]: https://github.com/Vbytes/android-sample
[5]: https://bintray.com/vbyte/maven/libp2pimpl
[6]: http://data1.vbyte.cn/apk/vbyte-demo.20160921.apk
[7]: http://data1.vbyte.cn/pkg/20160921.tar.gz
[8]: http://docs.vbyte.cn/manage/base/
[9]: https://bintray.com/
