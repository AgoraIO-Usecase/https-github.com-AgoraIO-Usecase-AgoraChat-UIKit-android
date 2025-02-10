package io.agora.chat.uikit.viewmodel

import androidx.lifecycle.ViewModel
import io.agora.chat.uikit.common.interfaces.IControlDataView

abstract class ChatUIKitBaseViewModel<V: IControlDataView>: ViewModel(), IAttachView {
    protected var view: V? = null

    override fun attachView(view: IControlDataView) {
        this.view = view as? V
    }
    override fun detachView() {
        this.view = null
    }
    override fun onCleared() {
        detachView()
        super.onCleared()
    }

}