# 单群聊 UIKit

_[English](README.md) | 中文_

单群聊 UIKit，是基于Agora IM SDK 的一款 UI 组件库，它提供了一些通用的 UI 组件，例如‘会话列表’、‘聊天界面’和‘联系人列表’等，开发者可根据实际业务需求通过该组件库快速地搭建自定义 IM 应用。单群聊 UIKit 中的组件在实现 UI 功能的同时，调用 IM SDK 相应的接口实现 IM 相关逻辑和数据的处理，因而开发者在 UIKit 时只需关注自身业务或个性化扩展即可。

本指南提供了 UIKit 框架在Android开发中的概述和使用示例，并介绍了此 UIKit 的各种组件和功能，让开发人员很好地了解 UIKit 的工作原理以及如何有效地使用它。

## 目录

- [单群聊 UIKit](#单群聊-uikit)
  - [目录](#目录)
  - [开发环境](#开发环境)
  - [集成安装](#集成安装)
    - [Gradle 接入集成](#gradle-接入集成)
      - [Gradle 7.0 之前](#gradle-70-之前)
      - [Gradle 7.0 之后](#gradle-70-之后)
    - [Module 远程依赖](#module-远程依赖)
    - [Module 源码集成](#module-源码集成)
    - [防止代码混淆](#防止代码混淆)
  - [UIKit 基本项目结构](#uikit-基本项目结构)
  - [权限要求](#权限要求)
  - [初始化及登录单群聊 UIKit](#初始化及登录单群聊-uikit)
    - [初始化](#初始化)
    - [登录](#登录)
    - [退出登录](#退出登录)
  - [快速搭建](#快速搭建)
    - [快速创建聊天页面](#快速创建聊天页面)
      - [使用 UIKitChatActivity](#使用-UIKitChatActivity)
      - [使用 UIKitChatFragment](#使用-UIKitChatFragment)
    - [快速创建会话列表页面](#快速创建会话列表页面)
    - [快速创建联系人列表页面](#快速创建联系人列表页面)
  - [高级定制](#高级定制)
    - [聊天页面相关](#聊天页面相关)
      - [通过 UIKitChatFragment.Builder 自定义设置](#通过-UIKitChatFragmentbuilder-自定义设置)
      - [添加自定义消息布局](#添加自定义消息布局)
      - [通过继承 UIKitChatFragment 进行自定义设置](#通过继承-UIKitChatFragment-进行自定义设置)
    - [会话列表页面相关](#会话列表页面相关)
      - [通过 ChatUIKitConversationListFragment.Builder 自定义设置](#通过-ChatUIKitConversationListFragmentbuilder-自定义设置)
      - [添加自定义会话布局](#添加自定义会话布局)
      - [通过继承 ChatUIKitConversationListFragment 进行自定义设置](#通过继承-ChatUIKitConversationListFragment-进行自定义设置)
    - [联系人列表页面相关](#联系人列表页面相关)
      - [通过 ChatUIKitContactsListFragment.Builder 自定义设置](#通过-ChatUIKitContactsListFragmentbuilder-自定义设置)
      - [添加自定义联系人布局](#添加自定义联系人布局)
  - [UIKit 提供的全局配置](#uikit-提供的全局配置)
  - [UIKit 用户信息相关](#uikit-用户信息相关)
    - [当前登录用户信息](#当前登录用户信息)
    - [联系人信息提供](#联系人信息提供)
    - [群组成员信息提供](#群组成员信息提供)
    - [UIKit 信息处理逻辑](#uikit-信息处理逻辑)
    - [更新 UIKit 缓存信息](#更新-uikit-缓存信息)
  - [UIKit 对明暗主题的支持](#uikit-对明暗主题的支持)

## 开发环境

- Android Studio Flamingo | 2022.2.1 及以上
- Gradle 8.0 及以上
- targetVersion 26 及以上
- Android SDK API 21 及以上
- JDK 17 及以上

## 集成安装

单群聊 UIKit 支持 Gradle 接入和 Module 源码集成。

### Gradle 接入集成

#### Gradle 7.0 之前

在项目根目录的 build.gradle 或者 build.gradle.kts 文件中添加 MavenCentral 远程仓库。

```kotlin
buildscript {
    repositories {
        ...
        mavenCentral()
    }
}
allprojects {
    repositories {
        ...
        mavenCentral()
    }
}
```

#### Gradle 7.0 之后

在项目根目录的 `settings.gradle` 或者 `settings.gradle.kts` 文件中检查并添加 MavenCentral 远程仓库。

```kotlin
pluginManagement {
    repositories {
        ...
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        ...
        mavenCentral()
    }
}
```

### Module 远程依赖

在 app 项目 build.gradle.kts 中添加以下依赖,其中 `x.y.z` 表示[最新版本号](https://central.sonatype.com/artifact/io.agora.rtc/chat-uikit/versions):

```kotlin

implementation("io.agora.rtc:chat-uikit:x.y.z")

```

### Module 源码集成

从 github 获取 [Chat UIKit](https://github.com/AgoraIO-Usecase/AgoraChat-UIKit-android/tree/dev-kotlin) 源码，按照下面的方式集成：

1. 在根目录 settings.gradle.kts 文件Project/settings.gradle.kts(Project Settings)中添加如下代码：

```kotlin
include(":agora-chat-kit")
project(":agora-chat-kit").projectDir = File("../AgoraChat-UIKit-android/ease-im-kit")
```

2. 在 `build.gradle.kts` 文件（app/build.gradle）中添加如下代码：

```kotlin
//chatuikit-android
implementation(project(mapOf("path" to ":agora-chat-kit")))
```

### 防止代码混淆

在 `app/proguard-rules.pro` 文件中添加如下行，防止代码混淆：

```kotlin
-keep class io.agora.** {*;}
-dontwarn  io.agora.**
```

## UIKit 基本项目结构

```
└── uikit
    ├── ChatUIKitClient                                   // UIKit SDK 入口
    ├── ChatUIKitConfig                             // UIKit SDK 配置类
    ├── feature                                  // UIKit 功能模块
    │   ├── chat                                   // 聊天功能模块
    │   │   ├── activities                            // 聊天功能模块的 Activity 文件夹
    │   │   │   └── UIKitChatActivity                    // UIKit内置的聊天界面
    │   │   ├── adapter                               // 聊天功能模块的适配器文件夹
    │   │   │   └── ChatUIKitMessagesAdapter                 // 聊天功能模块的消息列表适配器
    │   │   ├── reply                                 // 聊天功能模块的回复功能相关
    │   │   ├── report                                // 聊天功能模块的举报消息功能相关
    │   │   ├── chathistory                           // 聊天功能模块的消息历史功能相关
    │   │   ├── forward                               // 聊天功能模块的消息转发功能相关
    │   │   ├── reaction                              // 聊天功能模块的消息 Reaction 功能相关
    │   │   ├── search                                // 聊天功能模块的搜索消息功能相关
    │   │   ├── translation                           // 聊天功能模块的消息翻译功能相关
    │   │   ├── viewholders                           // 聊天功能模块的消息类型 ViewHolder
    │   │   ├── widgets                               // 聊天功能模块的自定义 View
    │   │   └── UIKitChatFragment                      // UIKit内提供的聊天 Fragment
    │   ├── conversation                           // 会话列表功能模块
    │   │   ├── adapter                               // 会话列表功能模块的适配器文件夹
    │   │   │   └── ChatUIKitConversationListAdapter         // 会话列表功能模块的会话列表适配器
    │   │   ├── viewholders                           // 会话列表功能模块的会话类型 ViewHolder
    │   │   ├── widgets                               // 会话列表功能模块的自定义 View
    │   │   └── ChatUIKitConversationListFragment          // UIKit内提供的会话列表 Fragment
    │   ├── thread                                 // 子区功能模块
    │   │   ├── adapter                               // 子区功能模块的适配器文件夹
    │   │   │   └── ChatUIKitThreadListAdapter           // 子区功能模块的子区列表适配器
    │   │   ├── viewholder                            // 子区列表功能模块的子区列表类型 ViewHolder
    │   │   ├── widgets                               // 子区列表功能模块的自定义 View
    │   │   └── ChatUIKitThreadActivity                // UIKit内提供的子区聊天页面
    │   ├── contact                                // 联系人列表功能模块
    │   │   ├── adapter                               // 联系人列表功能模块的适配器文件夹
    │   │   │   └── ChatUIKitContactListAdapter              // 联系人列表功能模块的联系人列表适配器
    │   │   ├── viewholders                           // 联系人列表功能模块的相关 ViewHolder
    │   │   ├── widgets                               // 联系人列表功能模块的自定义 View
    │   │   └── ChatUIKitContactsListFragment              // UIKit内提供的联系人列表 Fragment
    │   └── group                                  // 群组功能模块
    ├── repository                               // UIKit SDK 数据仓库
    ├── viewmodel                                // UIKit SDK ViewModel
    ├── provider                                 // UIKit SDK Provider
    ├── common                                   // UIKit SDK 公共类
    ├── interfaces                               // UIKit SDK 接口类
    └── widget                                   // UIKit SDK 自定义 View
```

## 权限要求

```xml
<!-- IM SDK required start -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<!-- IM SDK required end -->
<!-- IM UIKit required start -->
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:maxSdkVersion="32" />
<!-- Android 13 to replace READ_EXTERNAL_STORAGE permission -->
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES"/>
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO"/>
<!-- Android 14 is used to grant partial access to photos and videos -->
<uses-permission android:name="android.permission.READ_MEDIA_VISUAL_USER_SELECTED"/>
<!-- IM UIKit required end -->
```

## 初始化及登录单群聊 UIKit

### 初始化

使用单群聊 UIKit 需要进行初始化，示例代码如下：
```kotlin
val options = ChatOptions()
options.appKey = "[Your appkey]"
ChatUIKitClient.init(this, options)
```

### 登录

```kotlin
val user = ChatUIKitProfile(userName, nickname, avatarUrl)
ChatUIKitClient.login(user, token
    , onSuccess = {
        // Add success logic
    }, onError = { code, error ->
        // Add error logic
    }
)
```

### 退出登录

```kotlin
ChatUIKitClient.logout(unbindDeviceToken
    , onSuccess = {
        // Add success logic
    }, onError = { code, error ->
        // Add error logic
    }
)
```

## 快速搭建

### 快速创建聊天页面

#### 使用 UIKitChatActivity

单群聊 UIKit 提供了 UIKitChatActivity 页面，调用 UIKitChatActivity#actionStart 方法即可，示例代码如下：

```kotlin
// conversationId: 1v1 is peer's userID, group chat is groupID
// chatType can be ChatUIKitType#SINGLE_CHAT, ChatUIKitType#GROUP_CHAT
UIKitChatActivity.actionStart(mContext, conversationId, chatType)
```
UIKitChatActivity 页面主要进行了权限的请求，比如相机权限，语音权限等。

#### 使用 UIKitChatFragment

开发者也可以使用 UIKit 提供的 UIKitChatFragment 创建聊天页面，示例代码如下：
```kotlin
class ChatActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        // conversationID: 1v1 is peer's userID, group chat is groupID
        // chatType can be ChatUIKitType#SINGLE_CHAT, ChatUIKitType#GROUP_CHAT
        UIKitChatFragment.Builder(conversationId, chatType)
                        .build()?.let { fragment ->
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.fl_fragment, fragment).commit()
                        }
    }
}
```

### 快速创建会话列表页面

UIKit 提供了 `ChatUIKitConversationListFragment` ，添加到 Activity 中即可使用。

示例如下：

```kotlin
class ConversationListActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation_list)

        ChatUIKitConversationListFragment.Builder()
                        .build()?.let { fragment ->
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.fl_fragment, fragment).commit()
                        }
    }
}
```

### 快速创建联系人列表页面

UIKit 提供了 ChatUIKitContactsListFragment ，添加到 Activity 中即可使用。

示例如下：

```kotlin
class ContactListActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_list)

        ChatUIKitContactsListFragment.Builder()
                        .build()?.let { fragment ->
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.fl_fragment, fragment).commit()
                        }
    }
}
```

## 高级定制

### 聊天页面相关

#### 通过 UIKitChatFragment.Builder 自定义设置

UIKitChatFragment 提供了 Builder 构建方式，方便开发者进行一些自定义设置，目前提供的设置项如下：

```kotlin
// conversationID: 1v1 is peer's userID, group chat is groupID
// chatType: SINGLE_CHAT, GROUP_CHAT, CHATROOM
UIKitChatFragment.Builder(conversationID, chatType)
        .useTitleBar(true)
        .setTitleBarTitle("title")
        .setTitleBarSubTitle("subtitle")
        .enableTitleBarPressBack(true)
        .setTitleBarBackPressListener(onBackPressListener)
        .setSearchMessageId(searchMessageId)
        .getHistoryMessageFromServerOrLocal(false)
        .setOnChatExtendMenuItemClickListener(onChatExtendMenuItemClickListener)
        .setOnChatInputChangeListener(onChatInputChangeListener)
        .setOnMessageItemClickListener(onMessageItemClickListener)
        .setOnMessageSendCallback(onMessageSendCallback)
        .setOnWillSendMessageListener(willSendMessageListener)
        .setOnChatRecordTouchListener(onChatRecordTouchListener)
        .setOnMessageForwardCallback(onMessageForwardCallback)
        .setOnSendCombineMessageCallback(onSendCombineMessageCallback)
        .setOnReactionMessageListener(onReactionMessageListener)
        .setOnModifyMessageListener(onModifyMessageListener)
        .setOnReportMessageListener(onReportMessageListener)
        .setOnTranslationMessageListener(onTranslationMessageListener)
        .setMsgTimeTextColor(msgTimeTextColor)
        .setMsgTimeTextSize(msgTimeTextSize)
        .setReceivedMsgBubbleBackground(receivedMsgBubbleBackground)
        .setSentBubbleBackground(sentBubbleBackground)
        .showNickname(false)
        .hideReceiverAvatar(false)
        .hideSenderAvatar(true)
        .setChatBackground(chatBackground)
        .setChatInputMenuBackground(inputMenuBackground)
        .setChatInputMenuHint(inputMenuHint)
        .sendMessageByOriginalImage(true)
        .setThreadMessage(isChatThread)
        .setTargetTranslationList(targetTranslationList)
        .setEmptyLayout(R.layout.layout_chat_empty)
        .setCustomAdapter(customAdapter)
        .setCustomFragment(myChatFragment)
        .build()
```

`UIKitChatFragment#Builder` 提供的方法解释：

| 方法                                   | 说明                                                         |
| -------------------------------------- | ---------------------------------------------------- |
| useTitleBar()                          | 是否使用默认的标题栏（ChatUIKitTitleBar）。<br/> - true：是。 <br/> - (默认) false: 否。 |
| setTitleBarTitle()                     | 设置标题栏的标题。                                        |
| setTitleBarSubTitle()                  | 设置标题栏的子标题。                                       |
| enableTitleBarPressBack()              | 设置是否支持显示返回按钮。<br/> - true：是。 <br/> - (默认) false: 否。                 |
| setTitleBarBackPressListener()        | 设置点击标题栏返回按钮的监听事件。                          |
| setSearchMessageId()                  | 设置搜索消息 ID，聊天将标识为 ChatUIKitLoadDataType.SEARCH      |
| getHistoryMessageFromServerOrLocal()  | 设置优先从服务器还是本地获取消息。                          |
| setOnChatExtendMenuItemClickListener() | 设置扩展功能的条目点击事件监听。                             |
| setOnChatInputChangeListener()         | 设置菜单中文本变化的监听。                                   |
| setOnMessageItemClickListener()        | 设置消息条目的点击事件监听，包括气泡区域及头像的点击及长按事件。 |
| setOnMessageSendCallback()             | 设置发送消息的结果回调监听。                                   |
| setOnWillSendMessageListener()         | 设置发送消息前添加消息扩展属性的回调。                       |
| setOnChatRecordTouchListener()         | 设置录音按钮的触摸事件回调。                                 |
| setOnMessageForwardCallback()          | 设置消息转发的结果回调。                                 |
| setOnSendCombineMessageCallback()      | 设置合并消息发送的结果回调。                                 |
| setOnReactionMessageListener()         | 设置操作消息 Reaction 的结果回调。                                 |
| setOnModifyMessageListener()           | 设置编辑消息的结果回调监听。                                 |
| setOnReportMessageListener()           | 设置举报消息的结果回调监听。                                 |
| setOnTranslationMessageListener()      | 设置消息翻译的结果回调监听。                                 |
| setMsgTimeTextColor()                  | 设置时间线文本的颜色。                                        |
| setMsgTimeTextSize()                   | 设置时间线文本的字体大小。                                  |
| setReceivedMsgBubbleBackground()       | 设置接收消息气泡区域的背景。                            |
| setSentBubbleBackground()              | 设置发送消息气泡区域的背景。                              |
| showNickname()                         | 是否显示昵称。<br/> - true：是。 <br/> - (默认) false: 否。                                  |
| hideReceiverAvatar()                   | 设置不展示接收方头像，默认展示接收方头像。                     |
| hideSenderAvatar()                     | 设置不展示发送方头像，默认展示发送方头像。                    |
| setChatBackground()                    | 设置聊天列表区域的背景。                                      |
| setChatInputMenuBackground()           | 设置菜单区域的背景。                                        |
| setChatInputMenuHint()                 | 设置菜单区域输入文本框的提示文字。                            |
| sendMessageByOriginalImage()           | 设置图片消息是否发送原图。<br/> - true：是。 <br/> - (默认) false: 否。                      |
| setThreadMessage()                     | 设置当前会话是否是子区会话。<br/> - true：是。 <br/> - (默认) false: 否。                      |
| setTargetTranslationList()             | 设置翻译目标语言列表。需要开通消息翻译功能。                       |
| setEmptyLayout()                       | 设置聊天列表的空白页面。                                     |
| setCustomAdapter()                     | 设置自定义的适配器，默认为 ChatUIKitMessagesAdapter。               |
| setCustomFragment()                    | 设置自定义聊天 Fragment，需要继承自 UIKitChatFragment。         |

#### 添加自定义消息布局

开发者可以继承 ChatUIKitMessagesAdapter ， ChatUIKitRowViewHolder 和 ChatUIKitRow 实现自己的 CustomMessageAdapter ，CustomChatTypeViewViewHolder 和 CustomTypeChatRow ，然后将 CustomMessageAdapter 设置到 UIKitChatFragment#Builder#setCustomAdapter 中。

（1）创建自定义适配器 CustomMessageAdapter 继承自 ChatUIKitMessagesAdapter，重写 getViewHolder 和 getItemNotEmptyViewType 方法。

```kotlin
class CustomMessageAdapter: ChatUIKitMessagesAdapter() {

    override fun getItemNotEmptyViewType(position: Int): Int {
        // 根据消息类型设置自己的 itemViewType。
        // 如果要使用默认的，返回 super.getItemNotEmptyViewType(position) 即可。
        return CUSTOM_YOUR_MESSAGE_TYPE
    }

    override fun getViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<ChatMessage> {
        // 根据返回的 viewType 返回对应的 ViewHolder。
        // 返回自定义的 ViewHolder 或者 使用默认的 super.getViewHolder(parent, viewType)
        return CUSTOM_VIEW_HOLDER()
    }
}
```

（2）创建 CustomTypeChatRow ，继承自 ChatUIKitRow。

```kotlin
class CustomTypeChatRow(
    private val context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyle: Int = 0,
    isSender: Boolean = false
): ChatUIKitRow(context, attrs, defStyle, isSender) {

    override fun onInflateView() {
        inflater.inflate(if (!isSender) R.layout.layout_row_received_custom_type
        else R.layout.layout_row_sent_custom_type,
            this)
    }

    override fun onSetUpView() {
        (message?.getMessage()?.body as? ChatTextMessageBody)?.let { txtBody ->
            contentView.text = txtBody.message
        }
    }
}
```

（3）创建 CustomChatTypeViewViewHolder ，继承自 ChatUIKitRowViewHolder。

```kotlin
class CustomChatTypeViewViewHolder(
    itemView: View
): ChatUIKitRowViewHolder(itemView) {

    override fun onBubbleClick(message: ChatMessage?) {
        super.onBubbleClick(message)
        // Add click event
    }
}
```

（4）完善 CustomMessageAdapter。

```kotlin
class CustomMessageAdapter: ChatUIKitMessagesAdapter() {

    override fun getItemNotEmptyViewType(position: Int): Int {
        // 根据消息类型设置自己的 itemViewType。
        mData?.get(position)?.getMessage()?.let { msg ->
            msg.getStringAttribute("type", null)?.let { type ->
                if (type == CUSTOM_TYPE) {
                    return if (msg.direct() == ChatMessageDirection.SEND) {
                        VIEW_TYPE_MESSAGE_CUSTOM_VIEW_ME
                    } else {
                        VIEW_TYPE_MESSAGE_CUSTOM_VIEW_OTHER
                    }
                }
            }
        }
        // 如果要使用默认的，返回 super.getItemNotEmptyViewType(position) 即可。
        return super.getItemNotEmptyViewType(position)
    }

    override fun getViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<ChatMessage> {
        // 根据返回的 viewType 返回对应的 ViewHolder。
        if (viewType == VIEW_TYPE_MESSAGE_CUSTOM_VIEW_ME || viewType == VIEW_TYPE_MESSAGE_CUSTOM_VIEW_OTHER) {
            CustomChatTypeViewViewHolder(
                CustomTypeChatRow(parent.context, isSender = viewType == VIEW_TYPE_MESSAGE_CUSTOM_VIEW_ME)
            )
        }
        // 返回自定义的 ViewHolder 或者 使用默认的 super.getViewHolder(parent, viewType)
        return super.getViewHolder(parent, viewType)
    }

    companion object {
        private const val CUSTOM_TYPE = "custom_type"
        private const val VIEW_TYPE_MESSAGE_CUSTOM_VIEW_ME = 1000
        private const val VIEW_TYPE_MESSAGE_CUSTOM_VIEW_OTHER = 1001
    }
}
```

（5）添加 CustomMessageAdapter 到 UIKitChatFragment#Builder。

```kotlin
builder.setCustomAdapter(CustomMessageAdapter())
```

#### 通过继承 UIKitChatFragment 进行自定义设置

创建自定义 CustomChatFragment，继承自 UIKitChatFragment，并设置到 UIKitChatFragment#Builder 中。

```kotlin
builder.setCustomFragment(customChatFragment)
```

（1）列表控件相关功能设置

获取 `ChatUIKitMessageListLayout` 对象：

```kotlin
val chatMessageListLayout:ChatUIKitMessageListLayout? = binding?.layoutChat?.chatMessageListLayout
```

ChatUIKitMessageListLayout 提供了如下方法：

| 方法                        | 说明                                                         |
| ------------------------------ | ---------------------------------------------------- |
| setViewModel()              | UIKit 中提供了默认的实现 ChatUIKitMessageListViewModel，开发者可以继承 IChatMessageListRequest 添加自己的数据逻辑。 |
| setMessagesAdapter()        | 设置消息列表的适配器，需要是 ChatUIKitMessagesAdapter 的子类。                                         |
| getMessagesAdapter()        | 返回消息列表的适配器。                                         |
| addHeaderAdapter()          | 添加消息列表的头布局的适配器。                                |
| addFooterAdapter()          | 添加消息列表的尾布局的适配器。                                 |
| removeAdapter()             | 移除指定适配器。                                              |
| addItemDecoration()         | 添加消息列表的装饰器。                                        |
| removeItemDecoration()      | 移除消息列表的装饰器。                                        |
| setAvatarDefaultSrc()       | 设置条目的默认头像。                                         |
| setAvatarShapeType()        | 设置头像的样式，分为默认样式，圆形和矩形三种样式。   |
| showNickname()              | 是否展示条目的昵称，UIKitChatFragment#Builder 也提供了此功能的设置方法。 |
| setItemSenderBackground()   | 设置发送方的背景，UIKitChatFragment#Builder 也提供了此功能的设置方法。 |
| setItemReceiverBackground() | 设置接收方的背景，UIKitChatFragment#Builder 也提供了此功能的设置方法。 |
| setItemTextSize()           | 设置文本消息的字体大小。                                       |
| setItemTextColor()          | 设置文本消息的字体颜色。                                       |
| setTimeTextSize()           | 设置时间线文本的字体大小，UIKitChatFragment#Builder 也提供了此功能的设置方法。 |
| setTimeTextColor()          | 设置时间线文本的颜色，UIKitChatFragment#Builder 也提供了此功能的设置方法。 |
| setTimeBackground()         | 设置时间线的背景。                                             |
| hideChatReceiveAvatar()     | 不展示接收方头像，默认为展示，UIKitChatFragment#Builder 也提供了此功能的设置方法。 |
| hideChatSendAvatar()        | 不展示发送方头像，默认为展示，UIKitChatFragment#Builder 也提供了此功能的设置方法。 |
| setOnChatErrorListener()    | 设置发送消息时的错误回调，UIKitChatFragment#Builder 也提供了此功能的设置方法。 |

（2）扩展功能设置

```kotlin
val chatExtendMenu: IChatExtendMenu? = binding?.layoutChat?.chatInputMenu?.chatExtendMenu
```

拿到 chatExtendMenu 对象后，可以对于扩展功能可以进行添加，移除，排序以及处理扩展功能的点击事件等。

IChatExtendMenu 提供的方法解释：

| 方法                                    | 说明                                                 |
| -------------------------------------- | ---------------------------------------------------- |
| clear()            | 清除所有的扩展菜单项   |
| setMenuOrder()     | 对指定的菜单项进行排序 |
| registerMenuItem() | 添加新的菜单项         |

- 监听扩展条目点击事件

开发者可以 UIKitChatFragment#Builder#setOnChatExtendMenuItemClickListener 进行监听，也可以在自定义的 Fragment 中 重写 onChatExtendMenuItemClick 方法。

```kotlin
override fun onChatExtendMenuItemClick(view: View?, itemId: Int): Boolean {
    if(itemId == CUSTOM_YOUR_EXTEND_MENU_ID) {
        // 处理你自己的点击事件逻辑
        // 如果要消费点击事件需要返回 true
        return true
    }
    return super.onChatExtendMenuItemClick(view, itemId)
}
```

（3）长按菜单功能设置

- 增加自定义菜单条目

```kotlin
binding?.let {
    it.layoutChat.addItemMenu(menuId, menuOrder, menuTile)
}
```

ChatUIKitLayout 提供的长按菜单方法

| 方法                                | 说明                                                         |
| -------------------------------------- | ---------------------------------------------------- |
| clearMenu()                         | 清除菜单项。                                                  |
| addItemMenu()                       | 添加新的菜单项。                                               |
| findItemVisible()                   | 通过指定 itemId 设置菜单项的可见性。                           |
| setOnMenuChangeListener()           | 设置菜单项的点击事件监听，UIKitChatFragment 中已经设置此监听。  |

- 处理菜单的事件
  在自定义的 Fragment 中重写以下方法：

```kotlin
override fun onPreMenu(helper: ChatUIKitChatMenuHelper?, message: ChatMessage?) {
    // 菜单展示前的回调事件，可以通过 helper 对象在这里设置菜单条目是否展示。
}

override fun onMenuItemClick(item: ChatUIKitMenuItem?, message: ChatMessage?): Boolean {
    // 如果要拦截某个点击事件，需要设置返回 true。
    return false
}

override fun onDismiss() {
    // 可以在这里处理快捷菜单的隐藏事件。
}
```

（4）设置输入菜单相关属性

- 获取 ChatUIKitInputMenu 对象

```kotlin
val chatInputMenu: ChatUIKitInputMenu? = binding?.layoutChat?.chatInputMenu
```

ChatUIKitInputMenu 提供了如下方法：

| 方法                       | 说明                                                         |
| -------------------------- | ------------------------------------------------------------ |
| setCustomPrimaryMenu()     | 设置自定义的菜单项，支持 View 和 Fragment 两种方式           |
| setCustomEmojiconMenu()    | 设置自定义的表情功能，支持 View 和 Fragment 两种方式         |
| setCustomExtendMenu()      | 设置自定义的扩展功能，支持 View ，Dialog 和 Fragment 三种方式 |
| setCustomTopExtendMenu()   | 设置自定义的菜单顶部布局，支持 View ，Fragment 两种方式 |
| hideExtendContainer()      | 隐藏扩展区域，包括表情区域和扩展功能区域                     |
| hideInputMenu()            | 隐藏除了菜单顶部区域外的区域                     |
| showEmojiconMenu()         | 展示表情功能区域                                             |
| showExtendMenu()           | 展示扩展功能区域                                             |
| showTopExtendMenu()        | 展示顶部扩展功能区域                                          |
| setChatInputMenuListener() | 设置输入菜单监听                                             |
| chatPrimaryMenu           | 获取菜单项接口                                               |
| chatEmojiMenu             | 获取表情功能菜单接口                                         |
| chatExtendMenu            | 获取扩展功能接口                                             |
| chatTopExtendMenu        | 获取顶部扩展功能接口                                             |

- 获取菜单项对象

```kotlin
val primaryMenu: IChatPrimaryMenu? = binding?.layoutChat?.chatInputMenu?.chatPrimaryMenu
```

IChatPrimaryMenu 提供了如下方法：

| 方法                | 说明                                      |
| ------------------- | ----------------------------------------- |
| onTextInsert()      | 在光标处插入文本                          |
| editText            | 获取菜单输入框对象                        |
| setMenuBackground() | 设置菜单的背景                            |

- 获取表情菜单对象

```kotlin
val emojiconMenu: IChatEmojiconMenu? = binding?.layoutChat?.chatInputMenu?.chatEmojiMenu
```

IChatEmojiconMenu 提供了如下方法：

| 方法                  | 说明               |
| --------------------- | ------------------ |
| addEmojiconGroup()    | 添加自定义表情     |
| removeEmojiconGroup() | 移除指定的表情组   |
| setTabBarVisibility() | 设置 TabBar 可见性 |

添加自定义表情

```kotlin
binding?.let {
    it.layoutChat.chatInputMenu?.chatEmojiMenu?.addEmojiconGroup(EmojiconExampleGroupData.getData())
}
```

### 会话列表页面相关

#### 通过 ChatUIKitConversationListFragment.Builder 自定义设置

ChatUIKitConversationListFragment 提供了 Builder 构建方式，方便开发者进行一些自定义设置，目前提供的设置项如下：

```kotlin
ChatUIKitConversationListFragment.Builder()
    .useTitleBar(true)
    .setTitleBarTitle("title")
    .enableTitleBarPressBack(true)
    .setTitleBarBackPressListener(onBackPressListener)
    .useSearchBar(false)
    .setItemClickListener(onItemClickListener)
    .setOnItemLongClickListener(onItemLongClickListener)
    .setOnMenuItemClickListener(onMenuItemClickListener)
    .setConversationChangeListener(conversationChangeListener)
    .setEmptyLayout(R.layout.layout_conversation_empty)
    .setCustomAdapter(customAdapter)
    .setCustomFragment(myConversationListFragment)
    .build()
```

ChatUIKitConversationListFragment#Builder 提供的方法解释：

| 方法                             | 说明                                                         |
| -------------------------------- | ------------------------------------------------------------ |
| useTitleBar()                      | 是否使用默认的标题栏（ChatUIKitTitleBar）。<br/> - true：是。 <br/> - (默认) false: 否。           |
| setTitleBarTitle()                 | 设置标题栏的标题。                                            |
| enableTitleBarPressBack()          | 设置是否支持显示返回按钮，默认为不显示返回按钮。<br/> - true：是。 <br/> - (默认) false: 否。              |
| setTitleBarBackPressListener()    | 设置点击标题栏返回按钮的监听器。                               |
| setItemClickListener()          | 设置条目点击事件监听器。                                       |
| setOnItemLongClickListener()    | 设置条目长按事件监听器。                                       |
| setOnMenuItemClickListener()    | 设置条目菜单点击事件监听器。                                       |
| setConversationChangeListener() | 设置会话变化的监听器。                                        |
| setEmptyLayout()                | 设置会话列表的空白页面。                                       |
| setCustomAdapter()              | 设置自定义的适配器，默认为 ChatUIKitConversationListAdapter。       |
| setCustomFragment()             | 设置自定义聊天 Fragment，需要继承自 ChatUIKitConversationListFragment。 |

#### 添加自定义会话布局

开发者可以继承 ChatUIKitConversationListAdapter 实现自己的 CustomConversationListAdapter ，然后将 CustomConversationListAdapter 设置到 ChatUIKitConversationListFragment#Builder#setCustomAdapter 中。

（1）创建自定义适配器 CustomConversationListAdapter ，继承自 ChatUIKitConversationListAdapter ，重写 getViewHolder 和 getItemNotEmptyViewType 方法。

```kotlin
class CustomConversationListAdapter : ChatUIKitConversationListAdapter() {
    override fun getItemNotEmptyViewType(position: Int): Int {
        // 根据消息类型设置自定义 itemViewType。
        // 如果使用默认的 itemViewTyp，返回 super.getItemNotEmptyViewType(position) 即可。
        return CUSTOM_YOUR_CONVERSATION_TYPE
    }

    override fun getViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<ChatUIKitConversation> {
        // 根据返回的 viewType 返回对应的 ViewHolder。
        // 返回自定义的 ViewHolder 或者使用默认的 super.getViewHolder(parent, viewType)
        return CUSTOM_YOUR_VIEW_HOLDER()
    }
}
```

（2）添加 CustomConversationListAdapter 到 ChatUIKitConversationListFragment#Builder。

```kotlin
builder.setCustomAdapter(customConversationListAdapter);
```

#### 通过继承 ChatUIKitConversationListFragment 进行自定义设置

创建自定义 CustomConversationListFragment ，继承自 ChatUIKitConversationListFragment ，并设置到 ChatUIKitConversationListFragment#Builder 中。

```kotlin
builder.setCustomFragment(customConversationListFragment);
```

开发者可以通过在 CustomConversationListFragment 中获取到 ChatUIKitConversationListLayout 对象，可以进行更加细致的自定义设置。

ChatUIKitConversationListLayout 提供的方法解释：

| 方法                                    | 说明                                                             |
| -------------------------------------- | ---------------------------------------------------------------- |
| setViewModel()                    | UIKit 中提供了默认的实现 ChatUIKitConversationListViewModel，开发者可以继承 IConversationListRequest 添加自己的数据逻辑。 |
| setListAdapter()                  | 设置自定义会话列表适配器。                                    |
| getListAdapter()                  | 获取会话列表适配器。                                           |
| getItem()                         | 获取指定位置的数据。                                          |
| makeConversionRead()              | 将指定位置的会话置为已读。                                     |
| makeConversationTop()             | 将指定位置的会话置顶。                                      |
| cancelConversationTop()           | 取消指定位置的置顶操作。                                      |
| deleteConversation()              | 删除置顶位置的会话。                                           |
| setOnConversationChangeListener() | 设置会话变化的监听，ChatUIKitConversationListFragment#Builder 提供了相应的设置方法。 |
| addHeaderAdapter()                | 添加会话列表的头布局的适配器。                                |
| addFooterAdapter()                | 添加会话列表的尾布局的适配器。                                 |
| removeAdapter()                   | 移除指定适配器。                                              |
| addItemDecoration()               | 添加会话列表的装饰器。                                        |
| removeItemDecoration()            | 移除会话列表的装饰器。                                        |
| setOnItemClickListener()          | 设置会话列表的条目点击监听，ChatUIKitConversationListFragment#Builder 提供了相应的设置方法。 |
| setOnItemLongClickListener()      | 设置会话列表的条目长按监听。                                   |
| setItemBackGround()               | 设置条目的背景。                                               |
| setItemHeight()                   | 设置条目的高度。                                               |
| setAvatarDefaultSrc()             | 设置条目的默认头像。                                          |
| setAvatarSize()                   | 设置条目头像的大小。                                           |
| setAvatarShapeType()              | 设置条目头像的样式，分为默认 ImageView 样式，圆形和矩形三种样式。 |
| setAvatarRadius()                 | 设置条目头像的圆角半径，样式设置为矩形时有效。                 |
| setAvatarBorderWidth()            | 设置条目头像边框的宽度。                                      |
| setAvatarBorderColor()            | 设置条目头像边框的颜色。                                      |
| setNameTextSize()                | 设置会话条目标题的文字大小。                                   |
| setNameTextColor()               | 设置会话条目标题的文字颜色。                                   |
| setMessageTextSize()              | 设置会话条目内容的文字大小。                                  |
| setMessageTextColor()             | 设置会话条目内容的文字颜色。                                   |
| setDateTextSize()                 | 设置会话条目日期的文字大小。                                   |
| setDateTextColor()                | 设置会话条目日期的文字颜色。                                  |
| clearMenu()                       | 清除长按菜单项。                                              |
| addItemMenu()                     | 添加长按菜单项。                                              |
| findItemVisible()                 | 设置指定菜单项是否可见。                                     |

### 联系人列表页面相关

#### 通过 ChatUIKitContactsListFragment.Builder 自定义设置

ChatUIKitContactsListFragment 提供了 Builder 构建方式，方便开发者进行一些自定义设置，目前提供的设置项如下：

```kotlin
ChatUIKitContactsListFragment.Builder()
  .useTitleBar(true)
  .setTitleBarTitle("title")
  .enableTitleBarPressBack(true)
  .setTitleBarBackPressListener(onBackPressListener)
  .useSearchBar(false)
  .setSearchType(ChatUIKitSearchType.USER)
  .setListViewType(ChatUIKitListViewType.VIEW_TYPE_LIST_CONTACT)
  .setSideBarVisible(true)
  .setHeaderItemVisible(true)
  .setHeaderItemList(mutableListOf<ChatUIKitCustomHeaderItem>())
  .setOnHeaderItemClickListener(OnHeaderItemClickListener)
  .setOnUserListItemClickListener(OnUserListItemClickListener)
  .setOnItemLongClickListener(onItemLongClickListener)
  .setOnContactSelectedListener(OnContactSelectedListener)
  .setEmptyLayout(R.layout.layout_conversation_empty)
  .setCustomAdapter(customAdapter)
  .setCustomFragment(myContactsListFragment)
  .build()
```

ChatUIKitContactsListFragment#Builder 提供的方法解释：

| 方法                               | 说明                                                                                            |
|----------------------------------|-----------------------------------------------------------------------------------------------|
| useTitleBar()                    | 是否使用默认的标题栏（ChatUIKitTitleBar）。<br/> - true：是。 <br/> - (默认) false: 否。                               |
| setTitleBarTitle()               | 设置标题栏的标题。                                                                                     |
| enableTitleBarPressBack()        | 设置是否支持显示返回按钮，默认为不显示返回按钮。<br/> - true：是。 <br/> - (默认) false: 否。                                |
| setTitleBarBackPressListener()   | 设置点击标题栏返回按钮的监听器。                                                                              |
| useSearchBar()                   | 设置是否使用搜索栏   默认为不显示。<br/> - true：是。 <br/> - (默认) false: 否。                                     |
| setSearchType()                  | 设置搜索类型 ChatUIKitSearchType  <br/> - USER <br/> - SELECT_USER <br/> - CONVERSATION                              |
| setListViewType()                | 设置列表类型 ChatUIKitListViewType <br/> - LIST_CONTACT(默认 联系人列表) <br/> - LIST_SELECT_CONTACT (带checkbox的联系人列表) |
| setSideBarVisible()              | 设置是否显示首字母索引工具栏。   默认为显示工具栏。<br/> - (默认) true：是。 <br/> - false: 否。                             |
| setHeaderItemVisible()           | 设置是否显示列表头部布局。                                                                                 |
| setHeaderItemList()              | 设置列表头部Item数据对象列表。                                                                             |
| setOnHeaderItemClickListener()   | 设置列表头部Item点击事件。                                                                               |
| setOnUserListItemClickListener() | 设置列表条目点击事件。                                                                                   |
| setOnItemLongClickListener()     | 设置条目长按事件监听器。                                                                                  |
| setOnContactSelectedListener()   | 设置条目选中事件监听器。                                                                                  |
| setEmptyLayout()                 | 设置会话列表的空白页面。                                                                                  |
| setCustomAdapter()               | 设置自定义的适配器，默认为 ChatUIKitConversationListAdapter。                                                    |
| setCustomFragment()              | 设置自定义聊天 Fragment，需要继承自 ChatUIKitContactsListFragment。                                          |

#### 添加自定义联系人布局

开发者可以继承 ChatUIKitContactListAdapter 实现自己的 CustomContactListAdapter ，然后将 CustomContactListAdapter 设置到 ChatUIKitContactsListFragment#Builder#setCustomAdapter 中。

（1）创建自定义适配器 CustomContactListAdapter ，继承自 ChatUIKitContactListAdapter ，重写 getViewHolder 和 getItemNotEmptyViewType 方法。

```kotlin
class CustomContactListAdapter : ChatUIKitContactListAdapter() {
    override fun getItemNotEmptyViewType(position: Int): Int {
        // 根据消息类型设置自定义 itemViewType。
        // 如果使用默认的 itemViewTyp，返回 super.getItemNotEmptyViewType(position) 即可。
        return CUSTOM_YOUR_CONTACT_TYPE
    }

    override fun getViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<ChatUIKitUser> {
        // 根据返回的 viewType 返回对应的 ViewHolder。
        // 返回自定义的 ViewHolder 或者使用默认的 super.getViewHolder(parent, viewType)
        return CUSTOM_YOUR_VIEW_HOLDER()
    }
}
```

（2）添加 CustomContactListAdapter 到 ChatUIKitContactsListFragment#Builder。

```kotlin
builder.setCustomAdapter(CustomContactListAdapter)
```

开发者可以通过在 CustomContactListFragment 中获取到 ChatUIKitContactsListFragment 对象，可以进行更加细致的自定义设置。

ChatUIKitContactListLayout 提供的方法解释：

| 方法                                    | 说明                                                             |
| -------------------------------------- | ---------------------------------------------------------------- |
| setViewModel()                    | UIKit 中提供了默认的实现 ChatUIKitConversationListViewModel，开发者可以继承 IConversationListRequest 添加自己的数据逻辑。 |
| setListAdapter()                  | 设置自定义会话列表适配器。                                    |
| getListAdapter()                  | 获取会话列表适配器。                                           |
| getItem()                         | 获取指定位置的数据。                                          |
| addHeaderAdapter()                | 添加会话列表的头布局的适配器。                                |
| addFooterAdapter()                | 添加会话列表的尾布局的适配器。                                 |
| removeAdapter()                   | 移除指定适配器。                                              |
| addItemDecoration()               | 添加会话列表的装饰器。                                        |
| removeItemDecoration()            | 移除会话列表的装饰器。                                        |
| setOnItemClickListener()          | 设置会话列表的条目点击监听，ChatUIKitContactsListFragment#Builder 提供了相应的设置方法。 |
| setOnItemLongClickListener()      | 设置会话列表的条目长按监听。                                   |

## UIKit 提供的全局配置

单群聊 UIKit 提供了一些全局配置，可以在初始化时进行设置，示例代码如下：

```kotlin
val avatarConfig = ChatUIKitAvatarConfig()
// Set the avatars are round shape
avatarConfig.avatarShape = ChatUIKitImageView.ShapeType.ROUND
val config = ChatUIKitConfig(avatarConfig = avatarConfig)
ChatUIKitClient.init(this, options, config)
```

ChatUIKitAvatarConfig 提供的配置项解释：

| 属性                                    | 说明                                                             |
| -------------------------------------- | ---------------------------------------------------------------- |
| avatarShape                            | 头像样式，有默认，圆形和矩形三种样式，默认样式为默认。                    |
| avatarRadius                           | 头像圆角半径，仅在头像样式设置为矩形后有效。                            |
| avatarBorderColor                      | 头像边框的颜色。                                                    |
| avatarBorderWidth                      | 头像边框的宽度。                                                    |


ChatUIKitConfig 提供的配置项解释：

| 属性                                    | 说明                                                             |
| -------------------------------------- | ---------------------------------------------------------------- |
| enableReplyMessage                     | 消息回复功能是否可用，默认为可用。                                     |
| enableModifyMessageAfterSent           | 消息编辑功能是否可用，默认为可用。                                     |
| timePeriodCanRecallMessage             | 设置消息可撤回的时间，默认为2分钟。                                    |


ChatUIKitDateFormatConfig 提供的配置项解释：

| 属性                                    | 说明                                                             |
| -------------------------------------- | ---------------------------------------------------------------- |
| convTodayFormat                       | 会话列表当天日期格式，英文环境默认为："HH:mm"                            |
| convOtherDayFormat                    | 会话列表其他日期的格式，英文环境默认为： "MMM dd"                        |
| convOtherYearFormat                   | 会话列表其他年日期的格式，英文环境默认为： "MMM dd, yyyy"                |


ChatUIKitSystemMsgConfig 提供的配置项解释：

| 属性                                    | 说明                                                             |
| -------------------------------------- | ---------------------------------------------------------------- |
| useDefaultContactInvitedSystemMsg      | 是否启用系统消息功能，默认为启用。                                       |


ChatUIKitMultiDeviceEventConfig 提供的配置项解释：

| 属性                                   | 说明                |
|--------------------------------------|-------------------|
| useDefaultMultiDeviceContactEvent    | 是否启用默认的多设备联系人事件处理 |
| useDefaultMultiDeviceGroupEvent      | 是否启用默认的多设备群组事件处理  |

## UIKit 用户信息相关

UIKit 中多个地方用到用户信息，而这些用户信息需要开发者进行提供，本节将逐步介绍开发者如何提供给 UIKit 用户信息。

### 当前登录用户信息

用户调用登录接口 `ChatUIKitClient.login` 时需要传入一个 `ChatUIKitProfile` 的对象，包含 `id`, `name` 和 `avatar` 三个属性。`id` 是必须设置的参数，`name` 和 `avatar` 将用于当前用户昵称和头像的展示。并在发送消息时，将`name` 和 `avatar`属性设置到消息的`ext`中，方便其他用户进行展示。
如果登录时没有传入 `name` 和 `avatar` 属性，可以在登录后，调用 `ChatUIKitClient.updateCurrentUser` 对当前用户的信息进行更新。

### 联系人信息提供

UIKit 提供了接口 `ChatUIKitClient.setUserProfileProvider` 进行联系人信息的提供。
`ChatUIKitUserProfileProvider` 接口如下：
```kotlin
interface ChatUIKitUserProfileProvider {
    // 同步获取联系人信息
    fun getUser(userId: String?): ChatUIKitProfile?

    // 异步获取联系人信息
    fun fetchUsers(userIds: List<String>, onValueSuccess: OnValueSuccess<List<ChatUIKitProfile>>)
}
```
用法如下：
```kotlin
ChatUIKitClient.setUserProfileProvider(object : ChatUIKitUserProfileProvider {
    // 同步获取用户信息
    override fun getUser(userId: String?): ChatUIKitProfile? {
        return getLocalUserInfo(userId)
    }

    override fun fetchUsers(
        userIds: List<String>,
        onValueSuccess: OnValueSuccess<List<ChatUIKitProfile>>
    ) {
        fetchUserInfoFromServer(idsMap, onValueSuccess)
    }

})

```

### 群组成员信息提供

UIKit 提供了接口 `ChatUIKitClient.setGroupProfileProvider` 进行联系人信息的提供。
`ChatUIKitGroupProfileProvider` 接口如下：
```kotlin
interface ChatUIKitGroupProfileProvider {
    // 同步获取群成员信息
    fun getGroup(id: String?): ChatUIKitGroupProfile?

    // 异步获取群成员信息
    fun fetchGroups(groupIds: List<String>, onValueSuccess: OnValueSuccess<List<ChatUIKitGroupProfile>>)
}
```
用法如下：
```kotlin
ChatUIKitClient.setGroupProfileProvider(object : ChatUIKitGroupProfileProvider {
  
    override fun getGroup(id: String?): ChatUIKitGroupProfile? {
      ChatClient.getInstance().groupManager().getGroup(id)?.let {
        return ChatUIKitGroupProfile(it.groupId, it.groupName, it.extension)
      }
      return null
    }

    override fun fetchGroups(
      groupIds: List<String>,
      onValueSuccess: OnValueSuccess<List<ChatUIKitGroupProfile>>
    ) {
  
    }
})

```

### UIKit 信息处理逻辑

- 如果信息已经缓存到内存，当页面需要显示信息时，UIKit 会首先从内存中查询获取到缓存数据并进行页面的渲染。如果缓存没有，则进入下一步。
- UIKit 调用 provider 同步方法从应用本地获取信息，开发者可以从应用本地的数据库或者内存中获取并提供对应信息。UIKit获取到信息后进行页面的渲染。同时，对获取到的信息进行缓存。
- 如果同步方法获取数据为空，当列表页面停止滑动的时候，UIKit 会将当前页面可见的条目所需的信息，在排除缓存和同步方法提供的数据后，通过 provider 提供的异步方法返给开发者，开发者从服务器获取对应的信息后，通过 `onValueSuccess` 提供给 UIKit。UIKit 收到数据后，会对列表进行刷新并更新对应的数据。

### 更新 UIKit 缓存信息

因为 UIKit 会对信息进行缓存，如果用户的信息发生改变，可以通过 UIKit 提供的 update 方法对缓存信息进行更新。

```kotlin
// 通过 ChatUIKitClient.getCache().getUser ｜ ChatUIKitClient.getCache().getGroup 获取本地缓存对象进行更新赋值，之后调用update方法：
// 更新当前用户信息 user: ChatUIKitProfile
ChatUIKitClient.updateCurrentUser(user)
// 更新用户信息  list: List<ChatUIKitProfile>
ChatUIKitClient.updateUsersInfo(list)
// 更新群组信息 groups: List<ChatUIKitGroupProfile>
ChatUIKitClient.updateGroupInfo(groups)
```

## UIKit 对明暗主题的支持

UIKit 对明暗主题进行了支持，会随系统设置明暗主题进行对应的颜色变化。如果要修改对应的颜色变化，可以在app module中新建 `values-night` 文件夹，并复制 `uikit_colors.xml` 到文件夹中，然后对其中的基础颜色修改，在暗色主题下，对应的颜色也会被修改。