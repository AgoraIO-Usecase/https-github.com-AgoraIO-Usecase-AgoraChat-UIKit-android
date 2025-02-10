# Chat UIKit Readme

_English | [中文](README.zh.md)_

This guide gives a comprehensive overview into chat_uikit. The new chat_uikit is intended to provide developers with an efficient, plug-and-play, and highly customizable UI component library, helping you build complete and elegant IM applications that can easily satisfy most instant messaging scenarios. Please download the demo to try it out.

This guide provides an overview and usage examples of the chat_uikit framework in Android development, and presents various components and functions of this UIKit, giving developers a good understanding of how chat_uikit works and how to use it efficiently.

## Table of contents

- [Chat UIKit Readme](#chat-uikit-readme)
  - [Table of contents](#table-of-contents)
  - [Development Environment](#development-environment)
  - [Installation](#installation)
    - [Integrate with Gradle](#integrate-with-gradle)
      - [Gradle before 7.0](#gradle-before-70)
      - [Gradle later than 7.0](#gradle-later-than-70)
    - [Module remote dependency](#module-remote-dependency)
    - [Integrate with the Module source code](#integrate-with-the-module-source-code)
    - [Prevent code obfuscation](#prevent-code-obfuscation)
  - [Basic project structure of chat\_uikit](#basic-project-structure-of-chat_uikit)
  - [Permission requirements](#permission-requirements)
  - [Initialize and log in to the UIKit](#initialize-and-log-in-to-the-uikit)
    - [Initialize the UIKit](#initialize-the-uikit)
    - [Log in to the UIKit](#log-in-to-the-uikit)
    - [Log out of the UIKit](#log-out-of-the-uikit)
  - [Create pages](#create-pages)
    - [Create the chat page](#create-the-chat-page)
      - [UIKitChatActivity](#UIKitChatActivity)
      - [UIKitChatFragment](#UIKitChatFragment)
    - [Create the conversation list page](#create-the-conversation-list-page)
    - [Create the contact list page](#create-the-contact-list-page)
  - [Advanced customization](#advanced-customization)
    - [Chat page](#chat-page)
      - [Customize with UIKitChatFragment.Builder](#Customize-with-UIKitChatFragmentbuilder)
      - [Add a custom message layout](#add-a-custom-message-layout)
      - [Customize settings by inheriting UIKitChatFragment](#customize-settings-by-inheriting-UIKitChatFragment)
    - [Conversation list page](#conversation-list-page)
      - [Customize settings with ChatUIKitConversationListFragment.Builder](#customize-settings-with-ChatUIKitConversationListFragmentbuilder)
      - [Add a custom conversation layout](#add-a-custom-conversation-layout)
      - [Create a CustomConversationListFragment by inheriting ChatUIKitConversationListFragment](#create-a-customconversationlistfragment-by-inheriting-ChatUIKitConversationListFragment)
    - [Contact list page](#contact-list-page)
      - [Customize settings with ChatUIKitContactsListFragment.Builder](#customize-settings-with-ChatUIKitContactsListFragmentbuilder)
      - [Add a custom contact layout](#add-a-custom-contact-layout)
  - [Global configurations](#global-configurations)
  - [User information](#user-information)
    - [Information of the current login user](#information-of-the-current-login-user)
    - [User information providing](#user-information-providing)
    - [Group information providing](#group-information-providing)
    - [UIKit information processing logic](#uikit-information-processing-logic)
    - [Update information cached in UIKit](#update-information-cached-in-uikit)
  - [Support for dark and light themes](#support-for-dark-and-light-themes)


## Development Environment

- Android Studio Flamingo | 2022.2.1 or later
- Gradle 8.0 or later
- TargetVersion 26 or later
- Android SDK API 21 or later
- JDK 17 or later

## Installation

The UIKit can be integrated with Gradle and module source code.

### Integrate with Gradle

#### Gradle before 7.0

Add the Maven remote repository in `build.gradle` or `build.gradle.kts` in the root directory of the project.

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

#### Gradle later than 7.0

Add the Maven remote repository in `settings.gradle` or `settings.gradle.kts` in the root directory of the project.

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

### Module remote dependency

Add the following dependency to `build.gradle.kts` of the app project,where `x.y.z` indicates the [latest version](https://central.sonatype.com/artifact/io.agora.rtc/chat-uikit/versions):

```kotlin

implementation("io.agora.rtc:chat-uikit:x.y.z")

```

### Integrate with the Module source code

Acquire the Chat UIKit source code from the [GitHub repository](https://github.com/AgoraIO-Usecase/AgoraChat-UIKit-android/tree/dev-kotlin) and integrate it in the following way:

1. Add the following code in the `settings.gradle.kts` file Project/settings.gradle.kts(Project Settings) in the root directory.

```kotlin
include(":chat-uikit")
project(":chat-uikit").projectDir = File("../AgoraChat-UIKit-android/ease-im-kit")
```

2. Add the following code in `build.gradle.kts` app/build.gradle(Module: app).

```kotlin
//chat-uikit
implementation(project(mapOf("path" to ":chat-uikit")))
```

### Prevent code obfuscation

Add the following lines to `app/proguard-rules.pro` to prevent code obfuscation.

```kotlin
-keep class io.agora.** {*;}
-dontwarn  io.agora.**
```

## Basic project structure of chat_uikit

```
└── uikit
    ├── ChatUIKitClient                                   // UIKit SDK entry
    ├── ChatUIKitConfig                             // UIKit SDK configuration class
    ├── feature                                  // UIKit function module
    │   ├── chat                                   // Chat module
    │   │   ├── activities                            // Activity folder
    │   │   │   └── UIKitChatActivity                  // Chat page built in the UIKit
    │   │   ├── adapter                               // Adapter folder of the chat module
    │   │   │   └── ChatUIKitMessagesAdapter               // Message list adapter
    │   │   ├── controllers                           // Controller of all functions of the chat module
    │   │   ├── pin                                   // Message pinning
    │   │   ├── urlpreview                            // URL preview
    │   │   ├── reply                                 // Message reply
    │   │   ├── report                                // Message reporting
    │   │   ├── chathistory                           // Chat history
    │   │   ├── forward                               // Message forwarding
    │   │   ├── reaction                              // Message reaction
    │   │   ├── search                                // Message search
    │   │   ├── translation                           // Message translation
    │   │   ├── viewholders                           // Message type ViewHolder
    │   │   ├── widgets                               // Custom view of the chat module
    │   │   └── UIKitChatFragment                      // Chat fragment built in the UIKit
    │   ├── conversation                           // Conversation list module
    │   │   ├── adapter                               // Adapter folder
    │   │   │   └── ChatUIKitConversationListAdapter       // Conversation list adapter
    │   │   ├── viewholders                           // Conversation ViewHolder
    │   │   ├── widgets                               // Custom view of the conversation list module
    │   │   └── ChatUIKitConversationListFragment          // Conversation list fragment built in the UIKit
    │   ├── thread                                 // Message thread module
    │   │   ├── adapter                               // Adapter folder
    │   │   │   └── ChatUIKitThreadListAdapter         // Message thread list adapter
    │   │   ├── viewholder                            // Message thread ViewHolder 
    │   │   ├── widgets                               // Custom view of the message thread module
    │   │   └── ChatUIKitThreadActivity               // Thread chat page within the UIKit
    │   ├── contact                               // Contact list module
    │   │   ├── adapter                               // Contact list adapter folder 
    │   │   │   └── ChatUIKitContactListAdapter            // Contact list adapter
    │   │   ├── viewholders                           // Contact ViewHolder
    │   │   ├── widgets                               // Custom view of the contact list module
    │   │   └── ChatUIKitContactsListFragment              // Contact list fragment built in the UIKit
    │   └── group                                 // Group module
    │       ├── fragments                             // Group fragment
    │       ├── adapter                               // Adapter folder 
    │       │   └── ChatUIKitGroupListAdapter                // Group list adapter
    │       ├── viewholders                           // ViewHolder   Message ViewHolder
    │       └── ChatUIKitGroupListActivity                 // Group list UI built in the UIKit
    ├── repository                               // UIKit SDK data repository
    ├── viewmodel                                // UIKit SDK ViewModel
    ├── provider                                 // UIKit SDK Provider
    ├── common                                   // Public class of UIKit SDK
    ├── interfaces                               // API class of UIKit SDK
    └── widget                                   // Custom view of UIKit SDK
```

## Permission requirements

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

## Initialize and log in to the UIKit

### Initialize the UIKit

You need to initialize the UIKit before using it:

```kotlin
val options = ChatOptions()
options.appKey = "[Your appkey]"
ChatUIKitClient.init(this, options)
```
### Log in to the UIKit

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

### Log out of the UIKit

```kotlin
ChatUIKitClient.logout(unbindDeviceToken
    , onSuccess = {
        // Add success logic
    }, onError = { code, error ->
        // Add error logic
    }
)
```

## Create pages

### Create the chat page

#### UIKitChatActivity

The UIKit provides the `UIKitChatActivity` page. You can call the `UIKitChatActivity#actionStart` method to create the chat page.

```kotlin
// conversationId: 1v1 is peer's userID, group chat is groupID
// chatType can be ChatUIKitType#SINGLE_CHAT, ChatUIKitType#GROUP_CHAT
UIKitChatActivity.actionStart(mContext, conversationId, chatType)
```

The UIKitChatActivity page requests permissions, like camera permissions and voice permissions.

#### UIKitChatFragment

Alternatively, you can create the chat page with `UIKitChatFragment`:

```kotlin
class ChatActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        // conversationID: 1v1 is peer's userID, group chat is groupID
        // chatType can be ChatType#SINGLE_CHAT, ChatType#GROUP_CHAT
        UIKitChatFragment.Builder(conversationId, chatType)
                        .build()?.let { fragment ->
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.fl_fragment, fragment).commit()
                        }
    }
}
```

### Create the conversation list page

UIKit provides `ChatUIKitConversationListFragment`.  You can create the conversation list page by adding `ChatUIKitConversationListFragment` to the `Activity`.

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

### Create the contact list page

UIKit provides `ChatUIKitContactsListFragment`. You can create the contact list page by adding `ChatUIKitContactsListFragment` to the `Activity`.

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

## Advanced customization

### Chat page

#### Customize with UIKitChatFragment.Builder

UIKitChatFragment allows you to custom settings shown below with Builder:

UIKitChatFragment

```kotlin
// conversationID: 1v1 is peer's userID, group chat is groupID
// chatType: SINGLE_CHAT, GROUP_CHAT
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

UIKitChatFragment#Builder provides the following methods:

| Method                                 | Description                                                         |
| -------------------------------------- | ---------------------------------------------------- |
| useTitleBar()                          | Sets to use the default title bar (ChatUIKitTitleBar):<br/> - true: Yes <br/> - (Default) false: No        |
| setTitleBarTitle()                     | Sets the title of the title bar.                                       |
| setTitleBarSubTitle()                  | Sets the sub-title of the title bar.                                       |
| enableTitleBarPressBack()              | Sets whether to show the back button in the title bar: <br/> - true: Yes <br/> - (Default) false: No           |
| setTitleBarBackPressListener()         | Sets the event that occurs when clicking the the back button in the title bar.                           |
| setSearchMessageId()                   | Sets the message ID for search. If a match is found, UIKitChatFragment will display the target message as well as 10 messages following it.       |
| getHistoryMessageFromServerOrLocal()   | Sets whether to preferentially get messages from the server or local storage.                           |
| setOnChatExtendMenuItemClickListener() | Sets the listener for chat extension items.                            |
| setOnChatInputChangeListener()         | Sets the listener for text changes on the menu.                                   |
| setOnMessageItemClickListener()        | Sets the listener for item click events, like the click and long press events of a message cell and avatar. |
| setOnMessageSendCallback()             | Sets the message sending result callback.                                   |
| setOnWillSendMessageListener()         | Sets the listener for adding message extension attributes before sending a message.                      |
| setOnChatRecordTouchListener()         | Sets the recording button touch event.                                 |
| setOnMessageForwardCallback()          | Sets the message forwarding result callback.                                |
| setOnSendCombineMessageCallback()      | Sets the result callback of sending a combined message.                                 |
| setOnReactionMessageListener()         | Sets the listener for message Reaction operation result.                                |
| setOnModifyMessageListener()           | Sets the listener for message edit result.                                 |
| setOnReportMessageListener()           | Sets the listener for message reporting result.                                |
| setOnTranslationMessageListener()      | Sets the listener for message translation result.                                 |
| setMsgTimeTextColor()                  | Sets the color of the timeline.      |
| setMsgTimeTextSize()                   | Sets the font size of the timeline text.                                 |
| setReceivedMsgBubbleBackground()       | Sets the background color of the received message cell.                         |
| setSentBubbleBackground()              | Sets the background color of the sent message cell.                             |
| showNickname()                         | Sets whether to display the nickname: <br/> - true: Yes <br/> - (Default) false: No                                  |
| hideReceiverAvatar()                   | Sets to hide the recipient avatar. By default, the recipient avatar is displayed.   |
| hideSenderAvatar()                     | Sets to hide the sender avatar. By default, the sender avatar is displayed.                   |
| setChatBackground()                    | Sets the background of the chat list section.                                     |
| setChatInputMenuBackground()           | Sets the background of the input bar.                                        |
| setChatInputMenuHint()                 | Sets the hint in the text box in the input bar.                           |
| sendMessageByOriginalImage()           | Sets whether to send the original image for an image message: <br/> - true: Yes <br/> - (Default) false: No                   |
| setThreadMessage()                     | Sets whether to set the current conversation as a thread conversation: <br/> - true: Yes <br/> - (Default) false: No                      |
| setTargetTranslationList()             | Sets the list of target languages for translation. You need to enable the translation function before calling this method.                        |
| setEmptyLayout()                       | Sets the empty page for the chat list.                                    |
| setCustomAdapter()                     | Sets a custom adapter. By default, ChatUIKitMessagesAdapter is used.  |
| setCustomFragment()                    | Sets the custom chat fragment by inheriting UIKitChatFragment.         |

#### Add a custom message layout

You can create a CustomMessageAdapter, CustomChatTypeViewViewHolder, and CustomTypeChatRow by inheriting ChatUIKitMessagesAdapter, ChatUIKitRowViewHolder, and ChatUIKitRow, and then set CustomMessageAdapter to UIKitChatFragment#Builder#setCustomAdapter.

(1) You can create a CustomMessageAdapter by inheriting ChatUIKitMessagesAdapter and overwrite `getViewHolder` and `getItemNotEmptyViewType` methods.

```kotlin
class CustomMessageAdapter: ChatUIKitMessagesAdapter() {

    override fun getItemNotEmptyViewType(position: Int): Int {
        // Set your own itemViewType by message type.
        // To use the default message type, return super.getItemNotEmptyViewType(position).
        return CUSTOM_YOUR_MESSAGE_TYPE
    }

    override fun getViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<ChatMessage> {
        // Return the ViewHolder for the returned viewType.
        // Return the custom ViewHolder or use the default super.getViewHolder(parent, viewType).
        return CUSTOM_VIEW_HOLDER()
    }
}
```

(2) Create a CustomTypeChatRow by inheriting ChatUIKitRow.

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

（3）Create a CustomChatTypeViewViewHolder by inheriting ChatUIKitRowViewHolder.

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

（4）Make improvement to CustomMessageAdapter.

```kotlin
class CustomMessageAdapter: ChatUIKitMessagesAdapter() {

    override fun getItemNotEmptyViewType(position: Int): Int {
        // Set your own itemViewType by message type.
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
        // If the default message type is used, return super.getItemNotEmptyViewType(position).
        return super.getItemNotEmptyViewType(position)
    }

    override fun getViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<ChatMessage> {
        // Return ViewHolder for the returned viewType. 
        if (viewType == VIEW_TYPE_MESSAGE_CUSTOM_VIEW_ME || viewType == VIEW_TYPE_MESSAGE_CUSTOM_VIEW_OTHER) {
            CustomChatTypeViewViewHolder(
                CustomTypeChatRow(parent.context, isSender = viewType == VIEW_TYPE_MESSAGE_CUSTOM_VIEW_ME)
            )
        }
        // Return the custom ViewHolder or use the default super.getViewHolder(parent, viewType).
        return super.getViewHolder(parent, viewType)
    }

    companion object {
        private const val CUSTOM_TYPE = "custom_type"
        private const val VIEW_TYPE_MESSAGE_CUSTOM_VIEW_ME = 1000
        private const val VIEW_TYPE_MESSAGE_CUSTOM_VIEW_OTHER = 1001
    }
}
```

（5）Add CustomMessageAdapter in UIKitChatFragment#Builder.

```kotlin
builder.setCustomAdapter(CustomMessageAdapter())
```

#### Customize settings by inheriting UIKitChatFragment

Create a CustomChatFragment by inheriting UIKitChatFragment and set it in UIKitChatFragment#Builder.

```kotlin
builder.setCustomFragment(customChatFragment)
```

(1) Set the functions of the list control

Get the `ChatUIKitMessageListLayout` object:

```kotlin
val chatMessageListLayout:ChatUIKitMessageListLayout? = binding?.layoutChat?.chatMessageListLayout
```

ChatUIKitMessageListLayout provides the following methods:

| Method                       | Description                                                        |
| ------------------------------ | ---------------------------------------------------- |
| setViewModel()              | UIKit provides ChatUIKitMessageListViewModel. You can add your own data logic by inheriting IChatMessageListRequest. |
| setMessagesAdapter()        | Sets the message list adapter that is a subclass of ChatUIKitMessagesAdapter.    |
| getMessagesAdapter()        | Returns the message list adapter.                                      |
| addHeaderAdapter()          | Adds the adapter of the header adapter of the message list.     |
| addFooterAdapter()          | Adds the adapter of the foot adapter of the message list.                                 |
| removeAdapter()             | Removes a specific adapter.                                              |
| addItemDecoration()         | Adds the decorator of the message list.                                     |
| removeItemDecoration()      | Removes the decorator of the message list.                                        |
| setAvatarDefaultSrc()       | Sets the default avatar of an item.                                        |
| setAvatarShapeType()        | Sets the avatar style: default style, round, and rectangular.  |
| showNickname()              | Sets whether to display the nickname of the item. Also, UIKitChatFragment#Builder provides the method for this function.  |
| setItemSenderBackground()   | Sets the background of the sender. Also, UIKitChatFragment#Builder provides the method for this function. |
| setItemReceiverBackground() | Sets the background of the recipient. Also, UIKitChatFragment#Builder provides a method for this function. |
| setItemTextSize()           | Sets the font size of the text message.                                       |
| setItemTextColor()          | Sets the font color of the text message.                                       |
| setTimeTextSize()           | Sets the font size of the timeline text. Also, UIKitChatFragment#Builder provides a method for this function. |
| setTimeTextColor()          | Sets the color of the timeline text. Also, UIKitChatFragment#Builder provides a method for this function. |
| setTimeBackground()         | Sets the background of the timeline.                                             |
| hideChatReceiveAvatar()     | Sets not to display the recipient's avatar. Also, UIKitChatFragment#Builder provides a method for this function.  |
| hideChatSendAvatar()        | Sets not to display the sender's avatar. Also, UIKitChatFragment#Builder provides a method for this function. |
| setOnChatErrorListener()    | Sets the error listener for sending a message. Also, UIKitChatFragment#Builder provides a method for this function. |

（2）Set extension functions

```kotlin
val chatExtendMenu: IChatExtendMenu? = binding?.layoutChat?.chatInputMenu?.chatExtendMenu
```

When getting the chatExtendMenu object, you can add, remove, and order extension functions and handle click events of these functions.

IChatExtendMenu provides the following methods:

| Method                                    | Description                                                 |
| -------------------------------------- | ---------------------------------------------------- |
| clear()            | Clears all extension menu items.   |
| setMenuOrder()     | Order a specific menu item.|
| registerMenuItem() | Add a new menu item.         |

- Listen for the extension item click event.

You can use UIKitChatFragment#Builder#setOnChatExtendMenuItemClickListener for listening for click events of extension items, or overwrite the onChatExtendMenuItemClick method in your custom fragment.

```kotlin
override fun onChatExtendMenuItemClick(view: View?, itemId: Int): Boolean {
    if(itemId == CUSTOM_YOUR_EXTEND_MENU_ID) {
        // Handle your own click event logic.
        // To consume click events, you need to return `true`. 
        return true
    }
    return super.onChatExtendMenuItemClick(view, itemId)
}
```

（3）Set menu items upon long press.

- Add custom menu items.

```kotlin
binding?.let {
    it.layoutChat.addItemMenu(menuId, menuOrder, menuTile)
}
```

ChatUIKitLayout provides the following method that can be called upon long press

| Method                               | Description                                                        |
| -------------------------------------- | ---------------------------------------------------- |
| clearMenu()                         | Clears menu items.      |
| addItemMenu()                       | Adds a new menu item.        |
| findItemVisible()                   | Sets whether a menu item is visible by itemId.     |
| setOnMenuChangeListener()           | Sets a listener for click events of menu items. This listener is already set in UIKitChatFragment.  |

- Handle events relating to the menu.
  Overwrite the following method in a custom fragment:

```kotlin
override fun onPreMenu(helper: ChatUIKitChatMenuHelper?, message: ChatMessage?) {
    // Callback event that occurs before the menu is displayed. Here you can set whether to display menu items by using the helper object.
}

override fun onMenuItemClick(item: ChatUIKitMenuItem?, message: ChatMessage?): Boolean {
    // If you want to intercept a certain event, you need to set to return `true`.
    return false
}

override fun onDismiss() {
    // You can handle the shortcut menu hiding event here.
}
```

(4) Set the properties of the input menu.

- Get the ChatUIKitInputMenu object.

```kotlin
val chatInputMenu: ChatUIKitInputMenu? = binding?.layoutChat?.chatInputMenu
```

ChatUIKitInputMenu provides the following methods:

| Method                      | Description                                                        |
| -------------------------- | ------------------------------------------------------------ |
| setCustomPrimaryMenu()     | Sets a custom menu item, via View or Fragment.        |
| setCustomEmojiconMenu()    | Sets a custom emoji, via View or Fragment         |
| setCustomExtendMenu()      | Sets a custom extension function, via View, Dialog, or Fragment.|
| setCustomTopExtendMenu()   | Sets a custom top layout of the menu, via View or Fragment. |
| hideExtendContainer()      | Hides the extension area, including the emoji area and extension function area.   |
| hideInputMenu()            | Hides the areas except the top area of the menu.                    |
| showEmojiconMenu()         | Displays the emoji function area.                                          |
| showExtendMenu()           | Displays the extension function area.                                          |
| showTopExtendMenu()        | Displays the top extension function area.                                        |
| setChatInputMenuListener() | Sets the input menu listener.                                           |
| chatPrimaryMenu            | Gets menu items.                                            |
| chatEmojiMenu              | Gets the emoji function menu.                                      |
| chatExtendMenu             | Gets the extension function.                                           |
| chatTopExtendMenu          | Gets the top extension function.                                          |

- Gets the menu item object.

```kotlin
val primaryMenu: IChatPrimaryMenu? = binding?.layoutChat?.chatInputMenu?.chatPrimaryMenu
```

IChatPrimaryMenu provides the following methods:

| Method                | Description                                      |
| ------------------- | ----------------------------------------- |
| onTextInsert()      | Inserts texts at the cursor position.                       |
| editText            | Gets the input box object of the menu.     |
| setMenuBackground() | Sets the background of the menu.                       |

- Gets the emoji menu object.

```kotlin
val emojiconMenu: IChatEmojiconMenu? = binding?.layoutChat?.chatInputMenu?.chatEmojiMenu
```

IChatEmojiconMenu provides the following methods:

| Method                | Description           |
| --------------------- | ------------------ |
| addEmojiconGroup()    | Adds a custom emoji group.     |
| removeEmojiconGroup() | Removes a emoji group.   |
| setTabBarVisibility() | Sets the visibility of TabBar. |

Add custom emojis:

```kotlin
binding?.let {
    it.layoutChat.chatInputMenu?.chatEmojiMenu?.addEmojiconGroup(EmojiconExampleGroupData.getData())
}
```

### Conversation list page

#### Customize settings with ChatUIKitConversationListFragment.Builder

ChatUIKitConversationListFragment allows you to customize settings shown below with Builder.

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

ChatUIKitConversationListFragment#Builder provides the following methods:

| Method                            | Description                                                         |
| -------------------------------- | ------------------------------------------------------------ |
| useTitleBar()                      | Sets whether to use the default title bar (ChatUIKitTitleBar): <br/> - true: Yes<br/> -(Default) false: No           |
| setTitleBarTitle()                 | Sets the title of the title bar.                                           |
| enableTitleBarPressBack()          | Sets whether to display the back button: <br/> - true: Yes<br/> -(Default) false: No |
| setTitleBarBackPressListener()     | Sets the listener for the click of the back button in the title bar.    |
| setItemClickListener()             | Sets the item click event listener.                                       |
| setOnItemLongClickListener()       | Sets the item long-pressing event listener.    |
| setOnMenuItemClickListener()       | Sets the item click event listener.    |
| setConversationChangeListener()    | Sets the conversation change listener.    |
| setEmptyLayout()                   | Sets a blank page.        |
| setCustomAdapter()                 | Sets a custom conversation list adapter by inheriting ChatUIKitConversationListAdapter.   |
| setCustomFragment()                | Sets a custom chat fragment by inheriting ChatUIKitConversationListFragment.  |

#### Add a custom conversation layout

You can add a CustomConversationListAdapter by inheriting ChatUIKitConversationListAdapter and set CustomConversationListAdapter to ChatUIKitConversationListFragment#Builder#setCustomAdapter.

(1) Create a CustomConversationListAdapter by inheriting ChatUIKitConversationListAdapter, and overwrite the getViewHolder and getItemNotEmptyViewType methods.

```kotlin
class CustomConversationListAdapter : ChatUIKitConversationListAdapter() {
    override fun getItemNotEmptyViewType(position: Int): Int {
        // Set a custom itemViewType by message type.
        // If the default itemViewTyp is used, return super.getItemNotEmptyViewType(position).
        return CUSTOM_YOUR_CONVERSATION_TYPE
    }

    override fun getViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<ChatUIKitConversation> {
        // Return the ViewHolder by reference to the returned viewType.
        // Return the custom ViewHolder or use the default super.getViewHolder(parent, viewType).
        return CUSTOM_YOUR_VIEW_HOLDER()
    }
}
```

（2）Add CustomConversationListAdapter in ChatUIKitConversationListFragment#Builder.

```kotlin
builder.setCustomAdapter(customConversationListAdapter);
```

#### Create a CustomConversationListFragment by inheriting ChatUIKitConversationListFragment

Create a CustomConversationListFragment by inheriting ChatUIKitConversationListFragment and set it to ChatUIKitConversationListFragment#Builder.

```kotlin
builder.setCustomFragment(customConversationListFragment);
```

You can gets the ChatUIKitConversationListLayout object from CustomConversationListFragment and configure custom settings.

ChatUIKitConversationListLayout provides the following methods:

| Method                                   | Description                                                            |
| -------------------------------------- | ---------------------------------------------------------------- |
| setViewModel()                    | UIKit provides the ChatUIKitConversationListViewModel. You can inherit IConversationListRequest and then add your own data logic. |
| setListAdapter()                  | Sets a custom conversation list adapter.    |
| getListAdapter()                  | Gets a conversation list adapter.     |
| getItem()                         | Gets the data at the specific position.     |
| makeConversionRead()              | Sets a conversation as read.     |
| makeConversationTop()             | Pins a conversation.  |
| cancelConversationTop()           | Unpins a conversation.  |
| deleteConversation()              | Deletes a pinned conversation.                                          |
| setOnConversationChangeListener() | Sets a conversation change listener. Also, ChatUIKitConversationListFragment#Builder provides a method to set the listener. |
| addHeaderAdapter()                | Adds an adapter for the header layout of the conversation list.     |
| addFooterAdapter()                | Adds an adapter for the footer layout of the conversation list.     |
| removeAdapter()                   | Removes an adapter.                                              |
| addItemDecoration()               | Adds a conversation list decorator.                                       |
| removeItemDecoration()            | Removes a conversation list decorator.                                         |
| setOnItemClickListener()          | Sets an item click listener. Also, ChatUIKitConversationListFragment#Builder provides a method to set the listener. |
| setOnItemLongClickListener()      | Sets an item long-press listener.                                   |
| setItemBackGround()               | Sets the item background.                                               |
| setItemHeight()                   | Sets the item height.                                               |
| setAvatarDefaultSrc()             | Sets the default item avatar.                                         |
| setAvatarSize()                   | Sets the size of the item avatar.                                           |
| setAvatarShapeType()              | Sets the item avatar style: default ImageView style, round, and rectangular.  |
| setAvatarRadius()                 | Sets the border radius of the item avatar. This setting is valid only for a rectangular avatar.    |
| setAvatarBorderWidth()            | Sets the width of the item avatar frame.                                     |
| setAvatarBorderColor()            | Sets the color of the item avatar frame.                                     |
| setNameTextSize()                 | Sets the font size of the conversation item title.                                   |
| setNameTextColor()                | Sets the text color of the conversation item title.                                  |
| setMessageTextSize()              | Sets the font size of message texts in the conversation item.                                  |
| setMessageTextColor()             | Sets the color of message texts in the conversation item.                                  |
| setDateTextSize()                 | Sets the font size of the conversation item date.                                 |
| setDateTextColor()                | Sets the text color of the conversation item date.                                  |
| clearMenu()                       | Clears menu items that appear when long pressing a conversation item. |
| addItemMenu()                     | Adds a menu item that appears when long pressing a conversation item.     |
| findItemVisible()                 | Sets whether a menu item is visible.  |

### Contact list page

#### Customize settings with ChatUIKitContactsListFragment.Builder

ChatUIKitContactsListFragment allows you to customize the settings shown below with Builder:

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
  .setDefaultMenuVisible(true)
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

ChatUIKitContactsListFragment#Builder provides the following methods:

| Method                              | Description                                                                                                  |
|----------------------------------|------------------------------------------------------------------------------------------------------|
| useTitleBar()                    | Whether to use the default title bar (ChatUIKitTitleBar): <br/> - true: Yes <br/> - (Default) false: No                                 |
| setTitleBarTitle()               | Sets the title in the title bar.   |
| enableTitleBarPressBack()        | Sets whether to display the back button: <br/> - true: Yes <br/> - (Default) false: No                                     |
| setTitleBarBackPressListener()   | Sets the listener for the click of the back button of the title bar.     |
| useSearchBar()                   | Sets whether to use the search bar: <br/> - true: Yes <br/> - (Default) false: No                                            |
| setSearchType()                  | Sets the search type ChatUIKitSearchType: <br/> - USER  <br/> - SELECT_USER <br/> - CONVERSATION                      |
| setListViewType()                | Sets the contact list type ChatUIKitListViewType <br/> - LIST_CONTACT (contact list by default) <br/> - LIST_SELECT_CONTACT (contact list with checkboxes) |
| setSideBarVisible()              | Sets whether to display the initial index toolbar <br/> - (Default) true: Yes <br/> - false: No                                   |
| setDefaultMenuVisible()          | Sets whether to show the default menu: <br/> - (Default) true: Yes  <br/> - false: No                                            |
| setHeaderItemVisible()           | Sets whether to show the contact list header layout: <br/> - true: Yes  <br/> - (Default) false: No                                 |
| setHeaderItemList()              | Sets the data object list of the header item of the contact list.     |
| setOnHeaderItemClickListener()   | Sets the click event listener for a header item of the contact list.      |
| setOnUserListItemClickListener() | Sets the contact click event listener.     |
| setOnItemLongClickListener()     | Sets the contact item long-press event listener.  |
| setOnContactSelectedListener()   | Sets the contact item selection event listener.   |
| setEmptyLayout()                 | Sets the blank page.    |
| setCustomAdapter()               | Sets a custom adapter by inheriting ChatUIKitContactListAdapter.     |
| setCustomFragment()              | Sets a custom chat fragment by inheriting ChatUIKitContactsListFragment.       |

#### Add a custom contact layout

You can create a CustomContactListAdapter by inheriting ChatUIKitContactListAdapter and set CustomContactListAdapter in  ChatUIKitContactsListFragment#Builder#setCustomAdapter.

(1) Create a CustomContactListAdapter by inheriting ChatUIKitContactListAdapter to overwrite getViewHolder and getItemNotEmptyViewType methods.

```kotlin
class CustomContactListAdapter : ChatUIKitContactListAdapter() {
    override fun getItemNotEmptyViewType(position: Int): Int {
        //Set a custom itemViewType by message type.
        // If the default itemViewTyp is used, return super.getItemNotEmptyViewType(position).
        return CUSTOM_YOUR_CONTACT_TYPE
    }

    override fun getViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<ChatUIKitUser> {
        // Return ViewHolder by reference to the returned viewType. 
        // Return the custom ViewHolder or use the default super.getViewHolder(parent, viewType)
        return CUSTOM_YOUR_VIEW_HOLDER()
    }
}
```

(2) Add CustomContactListAdapter to ChatUIKitContactsListFragment#Builder.

```kotlin
builder.setCustomAdapter(CustomContactListAdapter)
```

You can get the ChatUIKitContactsListFragment object from CustomContactListFragment and configure specific custom settings.

ChatUIKitContactListLayout provides the following methods:

| Method                                  | Description                                                           |
| -------------------------------------- | ---------------------------------------------------------------- |
| setViewModel()                    | The UIKit provides ChatUIKitContactListViewModel. You can inherit IConversationListRequest to add your own data logic. |
| setListAdapter()                  | Sets a custom contact list adapter.                                    |
| getListAdapter()                  | Gets the contact list adapter.                                         |
| getItem()                         | Gets the data at a specific location.      |
| addHeaderAdapter()                | Adds the header layout adapter for the contact list.    |
| addFooterAdapter()                | Adds the footer layout adapter for the contact list.   |
| removeAdapter()                   | Removes an adapter.                                            |
| addItemDecoration()               | Adds a contact list decorator.                                        |
| removeItemDecoration()            | Removes a contact list decorator.                                       |
| setOnItemClickListener()          | Sets the contact item click listener. Also, ChatUIKitContactsListFragment#Builder provides the method to set the listener. |
| setOnItemLongClickListener()      | Sets the item long-pressing listener.                                  |

## Global configurations

The UIKit provides global configurations which can be set during the initialization:

```kotlin
val avatarConfig = ChatUIKitAvatarConfig()
// Set the avatars are round shape
avatarConfig.avatarShape = ChatUIKitImageView.ShapeType.ROUND
val config = ChatUIKitConfig(avatarConfig = avatarConfig)
ChatUIKitClient.init(this, options, config)
```

ChatUIKitAvatarConfig provides the following properties:

| Property                                  | Description                                                             |
| -------------------------------------- | ---------------------------------------------------------------- |
| avatarShape                            | The avatar style: default style, round, and rectangular.                    |
| avatarRadius                           | The border radius of the avatar. This property is valid only for a rectangular avatar.                           |
| avatarBorderColor                      | The color of the avatar frame.                                                   |
| avatarBorderWidth                      | The width of the avatar frame.                                                   |


ChatUIKitConfig provides the following properties:

| Property                                  | Description                                                          |
| -------------------------------------- | ---------------------------------------------------------------- |
| enableReplyMessage                     | Whether to enable the message reply function: <br/> - (Default) true： Yes  <br/> - false： No  |
| enableModifyMessageAfterSent           | Whether to enable the message edit function: <br/> - (Default) true： Yes  <br/> - false： No   |
| timePeriodCanRecallMessage             | The message recall duration, which is 2 minutes by default. |


ChatUIKitDateFormatConfig provides the following properties:

| Property                                 | Description                                                            |
| -------------------------------------- | ---------------------------------------------------------------- |
| convTodayFormat                       | The date format of the current day on the conversation list. The default format is "HH:mm" in the English context.    |
| convOtherDayFormat                    | The date format of other dates than of the current day on the conversation list. The default format is "HH:mm" in the English context.   |
| convOtherYearFormat                   | The date format of other years than this year on the conversation list. The default format is "MMM dd, yyyy" in the English context.    |


ChatUIKitSystemMsgConfig provides the following property:

| Property                                    | Description                                                           |
| -------------------------------------- | ---------------------------------------------------------------- |
| useDefaultContactInvitedSystemMsg      | Whether to enable the system message function: <br/> (Default) - true： Yes  <br/> - false： No                                        |


ChatUIKitMultiDeviceEventConfig provides the following properties:

| Property                                    | Description            |
|--------------------------------------|-------------------|
| useDefaultMultiDeviceContactEvent    | Whether to enable the default multi-device contact event processing：<br/> (Default) - true： Yes  <br/> - false： No |
| useDefaultMultiDeviceGroupEvent      | Whether to enable the default multi-device group event processing：<br/> (Default) - true： Yes  <br/> - false： No|

## User information

User information is used in many places in UIKit and needs to be provided by developers. This section describes how developers provide user information to UIKit.

### Information of the current login user

During a call to the login API `ChatUIKitClient.login`, the user needs to pass in an `ChatUIKitProfile` object. This object contains the following attributes:
- `id`: The user ID. This parameter is required.
- `name` and `avatar`: Used to display the nickname and avatar of the current user. When sending a message, you can set the two parameters to the `ext` field of the message to allow other users to present the two parameters. If you fail to pass in the two parameters during login, you can call `ChatUIKitClient.updateCurrentUser` to update the current user's information after login.

### User information providing

UIKit provides `ChatUIKitClient.setUserProfileProvider` to provide user information.

`ChatUIKitUserProfileProvider` API is as follows:

```kotlin
interface ChatUIKitUserProfileProvider {
    // Gets user information synchronously
    fun getUser(userId: String?): ChatUIKitProfile?

    // Gets user information asynchronously
    fun fetchUsers(userIds: List<String>, onValueSuccess: OnValueSuccess<List<ChatUIKitProfile>>)
}
```

This API is used as follows:

```kotlin
ChatUIKitClient.setUserProfileProvider(object : ChatUIKitUserProfileProvider {
    // Gets user information synchronously
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

### Group information providing

UIKit provides the `ChatUIKitClient.setGroupProfileProvider` to provide the user information.

`ChatUIKitGroupProfileProvider` is as follows:

```kotlin
interface ChatUIKitGroupProfileProvider {
    // Gets user information synchronously
    fun getGroup(userId: String?): ChatUIKitGroupProfile?

    // Gets user information asynchronously
    fun fetchGroups(userIds: List<String>, onValueSuccess: OnValueSuccess<List<ChatUIKitGroupProfile>>)
}
```

This API is used as follows:

```kotlin
ChatUIKitClient.setGroupProfileProvider(object : ChatUIKitGroupProfileProvider {
    // Gets group information synchronously
    override fun getGroup(groupId: String?): ChatUIKitGroupProfile? {
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

### UIKit information processing logic

- Step 1: If the information has been cached in the memory, when information needs to be presented on pages, the UIKit will first retrieve the cached data from the memory and render the page. If no information is cached, proceed to step 2.
- Step 2. UIKit calls the provider synchronization method to obtain information locally from the application. Developers can obtain and provide the related information from the application's local database or memory. After the information is obtained, UIKit renders the page, while caching the information.
- Step 3. If the data obtained by the synchronization method is empty, when the list page stops sliding, UIKit will return the information required for the items visible on the current page through the asynchronous method provided by the provider after excluding cache and data provided by the synchronization method. After obtaining the corresponding information from the server, the developer provides it to UIKit through `onValueSuccess`. The UIKit refreshes the list and updates the corresponding data when receiving the data.

### Update information cached in UIKit

As information is cached in the UIKit, the UIKit will update the cached information via the `update` methods if the user information is changed.

```kotlin
// First call ChatUIKitClient.getCache().getUser ｜ ChatUIKitClient.getCache().getGroup to get the local cached object, and then call the update methods:
// Updates current user information  user: ChatUIKitProfile
ChatUIKitClient.updateCurrentUser(user)
// Updates user information in batches  list: List<ChatUIKitProfile>
ChatUIKitClient.updateUsersInfo(list)
// Updates group information in batches  groups: List<ChatUIKitGroupProfile>
ChatUIKitClient.updateGroupInfo(groups)
```

## Support for dark and light themes

UIKit supports both light and dark themes, with the theme colors changing with the system theme. To adjust the theme colors, you can create a new `values-night` folder in the app module, copy `uikit_colors.xml` to this folder, and then modify the basic colors in it. Under the dark theme, the corresponding colors will also be changed.